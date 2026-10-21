FROM alpine:3.20

# OpenSSL 3.2+ is needed for Java truststores (-jdktrust)
RUN apk add --no-cache openssl

COPY certificate-controller.sh /usr/local/bin/certificate-controller
RUN chmod 755 /usr/local/bin/certificate-controller

ENTRYPOINT ["certificate-controller"]
CMD ["bootstrap"]
