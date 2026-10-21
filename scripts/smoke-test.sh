#!/usr/bin/env bash
# End-to-end smoke test of the running Docker stack (docker compose up -d).
# Signs in as the seeded admin through Nginx with the full 2FA flow:
#   password → code from the Mailpit inbox → session cookie → protected API → logout,
# then checks that TLS is enforced with the ChatRoom CA.
set -euo pipefail

BASE_URL="${BASE_URL:-https://localhost}"
# Behind Nginx the API is under /api; set API_PREFIX= for a backend started on its own
API_PREFIX="${API_PREFIX-/api}"
MAILPIT_URL="${MAILPIT_URL:-http://localhost:8025}"
ADMIN_USER="${ADMIN_USER:-admin}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-admin123}"
ATTEMPTS="${ATTEMPTS:-60}"
# Set CHECK_SPA=false to test a backend started without Nginx (e.g. BASE_URL=http://localhost:8443 API_PREFIX=)
CHECK_SPA="${CHECK_SPA:-true}"
# TLS checks need the whole Docker stack (Nginx, backend and MySQL with certificates from the CA)
if [ "$BASE_URL" = "https://localhost" ]; then CHECK_TLS="${CHECK_TLS:-true}"; else CHECK_TLS="${CHECK_TLS:-false}"; fi

API="$BASE_URL$API_PREFIX"
JAR="$(mktemp)"
trap 'rm -f "$JAR"' EXIT

curl_k() { curl -sk --max-time 10 -b "$JAR" -c "$JAR" "$@"; }
status() { curl_k -o /dev/null -w '%{http_code}' "$@" || true; }
fail() { echo "    $*" >&2; exit 1; }

echo "1/8 Waiting for the stack..."
for i in $(seq 1 "$ATTEMPTS"); do
  spa=200
  [ "$CHECK_SPA" = "true" ] && spa=$(status "$BASE_URL/")
  api=$(status "$API/forumrooms")
  # Nginx answers 502 while the backend is still starting
  if [ "$spa" = "200" ] && [ "$api" != "000" ] && [ "$api" -lt 500 ]; then
    echo "    up after $i attempt(s): spa=$spa api=$api"
    break
  fi
  [ "$i" = "$ATTEMPTS" ] && fail "stack did not come up (spa=$spa api=$api)"
  sleep 5
done

if [ "$CHECK_SPA" = "true" ]; then
  echo "2/8 SPA routes load the app, even those that share a name with the API..."
  for route in /users /forum_rooms /login; do
    body=$(curl_k "$BASE_URL$route")
    echo "$body" | grep -q '<div id="root">' || fail "$route did not return the app"
  done
else
  echo "2/8 (skipped: no Nginx)"
fi

echo "3/8 Unauthenticated API call gets a JSON 401..."
body=$(curl_k "$API/forumrooms")
echo "$body" | grep -q '"status":401' || fail "expected a 401 problem response, got: $body"

echo "4/8 Password step..."
curl_k -X DELETE "$MAILPIT_URL/api/v1/messages" > /dev/null || true
login=$(status -X POST "$API/auth/login" -H 'Content-Type: application/json' \
  -d "{\"username\":\"$ADMIN_USER\",\"password\":\"$ADMIN_PASSWORD\"}")
[ "$login" = "200" ] || fail "login returned $login"

echo "5/8 Code from Mailpit → session cookie..."
code=""
for i in $(seq 1 10); do
  code=$(curl_k "$MAILPIT_URL/api/v1/messages" \
    | grep -oE '[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}' | head -n 1 || true)
  [ -n "$code" ] && break
  sleep 1
done
[ -n "$code" ] || fail "no 2FA email arrived"

user=$(curl_k -X POST "$API/auth/token" -H 'Content-Type: application/json' \
  -d "{\"username\":\"$ADMIN_USER\",\"password\":\"$ADMIN_PASSWORD\",\"token\":\"$code\"}")
echo "$user" | grep -q "\"username\":\"$ADMIN_USER\"" || fail "token step failed: $user"
echo "$user" | grep -q '"token"' && fail "the JWT must not be in the response body"
grep -q CHATROOM_SESSION "$JAR" || fail "no session cookie was set"

echo "6/8 Authenticated API calls..."
me=$(curl_k "$API/auth/me")
echo "$me" | grep -q '"role":"ROLE_ADMIN"' || fail "unexpected /auth/me: $me"
rooms=$(curl_k "$API/forumrooms")
echo "$rooms" | grep -q '"name"' || fail "unexpected response: $rooms"
echo "    $rooms"

echo "7/8 Logout ends the session..."
[ "$(status -X POST "$API/auth/logout")" = "204" ] || fail "logout failed"
[ "$(status "$API/auth/me")" = "401" ] || fail "session still valid after logout"

if [ "$CHECK_TLS" = "true" ]; then
  echo "8/8 TLS: the site's certificate chains to our CA, MySQL refuses plaintext..."
  # openssl inside the Docker network: same result on every platform
  verify=$(MSYS_NO_PATHCONV=1 docker compose run --rm -q --entrypoint sh certificates -c     'echo | openssl s_client -connect frontend:443 -servername localhost -verify_hostname localhost -CAfile /ca/ca.crt -verify_return_error 2>&1' || true)
  echo "$verify" | grep -q "Verify return code: 0 (ok)" || fail "Nginx certificate does not verify against the ChatRoom CA"
  verify=$(MSYS_NO_PATHCONV=1 docker compose run --rm -q --entrypoint sh certificates -c     'echo | openssl s_client -connect backend:8443 -servername backend -verify_hostname backend -CAfile /ca/ca.crt -verify_return_error 2>&1' || true)
  echo "$verify" | grep -q "Verify return code: 0 (ok)" || fail "backend certificate does not verify against the ChatRoom CA"
  plain=$(docker compose exec -T mysql sh -c 'mysql --ssl-mode=DISABLED -h 127.0.0.1 -uroot -p"$MYSQL_ROOT_PASSWORD" -e "select 1" 2>&1' || true)
  echo "$plain" | grep -q "insecure transport are prohibited" || fail "MySQL accepted a plaintext connection"
fi

[ "$CHECK_TLS" = "true" ] || echo "8/8 (skipped: TLS checks need the Docker stack)"
echo "Smoke test passed"
