FROM node:20-alpine AS build

WORKDIR /app

COPY chat-room/package*.json ./
RUN npm ci --no-audit --no-fund

COPY chat-room/ .

# Optional: a deployment's own Google OAuth client (falls back to src/Enviroments/*.json)
ARG VITE_GOOGLE_CLIENT_ID=
ENV VITE_GOOGLE_CLIENT_ID=$VITE_GOOGLE_CLIENT_ID

RUN npm run build

FROM nginx:alpine

# Certificate, key, CA and CRL come from the Certificate Controller, mounted at /etc/nginx/certs
COPY --from=build /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80 443

CMD ["nginx", "-g", "daemon off;"]
