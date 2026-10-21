# Chat Room · web app

React 18 single-page app built with **Vite**. See the [main README](../../README.md) for the whole system.

## Scripts

| Command | What it does |
|---------|--------------|
| `npm run dev` | Dev server on http://localhost:3000; `/api` is forwarded to the backend on `localhost:8443` |
| `npm run build` | Production build into `build/` (served by Nginx in Docker) |
| `npm run preview` | Serves the production build locally |
| `npm test` | Vitest in watch mode |
| `npm run test:ci` | All tests once, with a coverage report in `coverage/` |
| `npm run lint` | ESLint; any warning fails |

Set `VITE_API_TARGET` to point the dev proxy at another backend, and `VITE_GOOGLE_CLIENT_ID` to use your own Google OAuth client.

## How it talks to the API

- Every call goes to `/api/...` on the same origin: the Vite proxy in development, Nginx in Docker. Both strip the prefix before forwarding to Spring Boot.
- The session is an **httpOnly cookie** set by `POST /auth/token`. The app never sees the JWT; on start it asks `GET /auth/me` who is signed in, and `POST /auth/logout` ends the session.
- Errors arrive as RFC 7807 problem JSON (`{ status, title, detail }`).

## Layout

```
src/
├── Components/      pages (login, forum rooms, comments, users), Redux store, error pages
├── Services/        one module per API area (login, users, forum rooms, comments)
├── Enviroments/     per-mode settings (API base URL, Google client ID)
└── *.test.js(x)     Vitest + React Testing Library
```
