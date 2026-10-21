#!/bin/sh
# Certificate Controller: a small internal CA built on OpenSSL.
# Issues, tracks and revokes the TLS certificates of every component.
#
#   bootstrap            create the CA if needed, issue missing certificates, refresh the CRL
#   issue <component>    issue (or replace) a component's certificate
#   revoke <component>   revoke a component's certificate and publish the new CRL
#   list                 every certificate the CA ever issued, with status and expiry
#   export-ca            print the CA certificate (import it into a browser to trust the site)
#
# Components: nginx, backend, mysql. Each gets its own directory under $PKI_DIR with only
# what it needs; the CA's private key stays in $CA_DIR and is never mounted elsewhere.
set -eu

CA_DIR="${CA_DIR:-/ca}"
PKI_DIR="${PKI_DIR:-/pki}"
TRUSTSTORE_PASSWORD="${TRUSTSTORE_PASSWORD:-changeit}"
CNF="$CA_DIR/openssl.cnf"
RENEW_BEFORE_SECONDS=$((30 * 24 * 3600))

# Owner (uid) of each component's private key inside its own container
key_owner() {
  case "$1" in
    mysql) echo 999 ;;  # mysql user in the official image
    *) echo 0 ;;
  esac
}

san_for() {
  case "$1" in
    nginx) echo "DNS:localhost,DNS:frontend,IP:127.0.0.1" ;;
    backend) echo "DNS:backend,DNS:localhost,IP:127.0.0.1" ;;
    mysql) echo "DNS:mysql,DNS:localhost,IP:127.0.0.1" ;;
    *) echo "unknown component: $1" >&2; exit 2 ;;
  esac
}

init_ca() {
  [ -f "$CA_DIR/ca.crt" ] && return
  echo "Creating the ChatRoom CA"
  mkdir -p "$CA_DIR/newcerts" "$CA_DIR/csr"
  touch "$CA_DIR/index.txt"
  echo 1000 > "$CA_DIR/serial"
  echo 1000 > "$CA_DIR/crlnumber"
  cat > "$CNF" <<EOF
[ ca ]
default_ca = chatroom_ca

[ chatroom_ca ]
dir              = $CA_DIR
database         = \$dir/index.txt
new_certs_dir    = \$dir/newcerts
certificate      = \$dir/ca.crt
private_key      = \$dir/ca.key
serial           = \$dir/serial
crlnumber        = \$dir/crlnumber
default_md       = sha256
default_days     = 825
default_crl_days = 3650
policy           = policy_any
copy_extensions  = copy
unique_subject   = no

[ policy_any ]
commonName       = supplied
organizationName = optional

[ server_cert ]
basicConstraints       = CA:FALSE
keyUsage               = critical, digitalSignature, keyEncipherment
extendedKeyUsage       = serverAuth
subjectKeyIdentifier   = hash
authorityKeyIdentifier = keyid,issuer

[ v3_ca ]
basicConstraints     = critical, CA:TRUE, pathlen:0
keyUsage             = critical, keyCertSign, cRLSign
subjectKeyIdentifier = hash
EOF
  openssl req -x509 -new -newkey rsa:4096 -nodes -sha256 -days 3650 \
    -keyout "$CA_DIR/ca.key" -out "$CA_DIR/ca.crt" \
    -subj "/CN=ChatRoom Internal CA/O=ChatRoom" -extensions v3_ca -config "$CNF" 2>/dev/null
  chmod 600 "$CA_DIR/ca.key"
}

publish_crl() {
  openssl ca -config "$CNF" -gencrl -out "$CA_DIR/crl.pem" 2>/dev/null
  publish_shared
}

# Copies the public pieces (CA certificate, CRL, Java truststore) next to each component's certificate
publish_shared() {
  local component
  for component in nginx backend mysql; do
    mkdir -p "$PKI_DIR/$component"
    cp "$CA_DIR/ca.crt" "$PKI_DIR/$component/ca.crt"
    cp "$CA_DIR/crl.pem" "$PKI_DIR/$component/crl.pem"
  done
  openssl pkcs12 -export -nokeys -in "$CA_DIR/ca.crt" -jdktrust anyExtendedKeyUsage \
    -out "$PKI_DIR/backend/truststore.p12" -passout "pass:$TRUSTSTORE_PASSWORD"
  chmod 644 "$PKI_DIR"/*/ca.crt "$PKI_DIR"/*/crl.pem "$PKI_DIR/backend/truststore.p12"
}

serial_of() {
  openssl x509 -in "$1" -noout -serial | cut -d= -f2
}

# True if the component holds a certificate that is valid, not revoked and not about to expire
is_current() {
  local crt serial
  crt="$PKI_DIR/$1/$1.crt"
  [ -f "$crt" ] || return 1
  openssl x509 -in "$crt" -noout -checkend "$RENEW_BEFORE_SECONDS" >/dev/null || return 1
  serial=$(serial_of "$crt")
  grep -q "^V.*	$serial	" "$CA_DIR/index.txt"
}

revoke() {
  local component crt serial
  component="$1"
  crt="$PKI_DIR/$component/$component.crt"
  [ -f "$crt" ] || { echo "$component has no certificate" >&2; return 1; }
  serial=$(serial_of "$crt")
  if grep -q "^V.*	$serial	" "$CA_DIR/index.txt"; then
    openssl ca -config "$CNF" -revoke "$crt" -crl_reason "${2:-superseded}" 2>/dev/null
    echo "Revoked $component (serial $serial)"
  fi
  publish_crl
}

issue() {
  local component san dir
  component="$1"
  san=$(san_for "$component")
  dir="$PKI_DIR/$component"
  mkdir -p "$dir"
  # A replaced certificate is revoked, so only one valid certificate per component exists
  [ -f "$dir/$component.crt" ] && revoke "$component" superseded >/dev/null || true
  openssl req -new -newkey rsa:2048 -nodes -sha256 \
    -keyout "$dir/$component.key" -out "$CA_DIR/csr/$component.csr" \
    -subj "/CN=$component/O=ChatRoom" -addext "subjectAltName=$san" 2>/dev/null
  openssl ca -batch -notext -config "$CNF" -extensions server_cert \
    -in "$CA_DIR/csr/$component.csr" -out "$dir/$component.crt" 2>/dev/null
  chown "$(key_owner "$component")" "$dir/$component.key"
  chmod 600 "$dir/$component.key"
  chmod 644 "$dir/$component.crt"
  echo "Issued $component (serial $(serial_of "$dir/$component.crt"), $san)"
  publish_crl
}

list() {
  # index.txt columns: status, expiry, revocation date (empty while valid), serial, file, subject
  awk -F'\t' 'BEGIN { printf "%-8s %-6s %-16s %-16s %s\n", "STATUS", "SERIAL", "EXPIRES", "REVOKED", "SUBJECT" }
    { status = ($1 == "V") ? "valid" : ($1 == "R") ? "revoked" : ($1 == "E") ? "expired" : $1
      revoked = ($3 == "") ? "-" : substr($3, 1, 13)
      printf "%-8s %-6s %-16s %-16s %s\n", status, $4, $2, revoked, $6 }' "$CA_DIR/index.txt"
}

bootstrap() {
  local component
  init_ca
  for component in nginx backend mysql; do
    is_current "$component" || issue "$component"
  done
  publish_crl
  echo "PKI ready"
}

command="${1:-bootstrap}"
[ "$command" = bootstrap ] || [ -f "$CA_DIR/ca.crt" ] || init_ca
case "$command" in
  bootstrap) bootstrap ;;
  issue) issue "${2:?component}" ;;
  revoke) revoke "${2:?component}" "${3:-keyCompromise}" ;;
  list) list ;;
  export-ca) cat "$CA_DIR/ca.crt" ;;
  *) sed -n '2,13p' "$0"; exit 2 ;;
esac
