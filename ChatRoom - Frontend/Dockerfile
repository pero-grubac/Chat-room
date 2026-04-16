FROM node:18-alpine AS build

WORKDIR /app

# Kopiraj iz chat-room subfolder
COPY chat-room/package*.json ./
RUN npm install

COPY chat-room/ .

RUN echo '{ "baseServiceUrl": "", "REACT_APP_GOOGLE_API_TOKEN": "" }' \
    > src/Enviroments/config.production.json

RUN npm run build

FROM nginx:alpine

RUN apk add --no-cache openssl

RUN mkdir -p /etc/nginx/certs && \
    openssl req -x509 \
      -nodes \
      -days 3650 \
      -newkey rsa:2048 \
      -keyout /etc/nginx/certs/nginx.key \
      -out    /etc/nginx/certs/nginx.crt \
      -subj "/CN=localhost/O=ChatRoom/C=BA"

COPY --from=build /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80 443

CMD ["nginx", "-g", "daemon off;"]