<div align="center">

# 💬 Chat Room

![React](https://img.shields.io/badge/React-Frontend-blue?logo=react)
![Redux](https://img.shields.io/badge/Redux-State_Management-764abc?logo=redux&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-Backend-success?logo=springboot)
![MySQL](https://img.shields.io/badge/MySQL-Database-blue?logo=mysql)
![REST API](https://img.shields.io/badge/REST_API-Available-brightgreen?logo=spring)
![Spring Security](https://img.shields.io/badge/Spring_Security-Enabled-darkgreen?logo=springsecurity)
![WAF](https://img.shields.io/badge/WAF-Protected-orange?logo=firewall)
![Docker](https://img.shields.io/badge/Docker-Containerized-2496ED?logo=docker&logoColor=white)
![Nginx](https://img.shields.io/badge/Nginx-Reverse_Proxy-009639?logo=nginx&logoColor=white)
![Chat Room Demo](images/chatroom.gif)

</div>

This project is an internet forum where registered users can exchange opinions on various topics. The forum supports the following categories: Science, Culture, Sports, and Music.

---

## ✨ Features

### 👥 User Groups

- 🔑 **Administrator:** Manages user accounts, approves or bans access, defines user groups, and sets permissions (adding, editing, deleting comments).
- 🛠️ **Moderator:** Can access and manage comments, approve new comments, or ban them.
- 💻 **Forum User:** Can access all forum areas, view the latest 20 comments in each category, and participate in discussions.

### 🛠️ Functionalities

- 📝 **Registration and Authentication:** New users can register and must be approved by an administrator. Two-factor authentication is used for logging in. Users receive a verification code via email after entering their username and password.
- 📋 **Comment Management:** Moderators and administrators can manage comments, including approving, adjusting, or banning them.
- 🔐 **JWT Token:** Used for tracking user sessions after successful login.
- 🛡️ **Web Application Firewall (WAF):** Scans traffic to the web server and only allows benign requests, blocking potentially malicious ones.
- 🔏 **Certificate Management:** Manages the issuance, tracking, and revocation of digital certificates for all components.
- 📊 **SIEM Component:** Monitors and logs all security-sensitive requests.
- 🌐 **OAuth2 Login:** Users can also log in using an external account (Google, GitHub, etc.) via the OAuth2 framework.

---

## 🔧 Technologies Used

| Layer            | Technology                 |
| ---------------- | -------------------------- |
| Frontend         | React, Redux               |
| Backend          | Spring Boot, Java 21       |
| Database         | MySQL 8                    |
| Authentication   | JWT, OAuth2 (Google)       |
| Security         | Spring Security, WAF, SIEM |
| Containerization | Docker, Docker Compose     |
| Reverse Proxy    | Nginx (SSL termination)    |

---

## 📦 Architecture

### Components

- 🔓 **Access Controller:** Accepts user requests, communicates with other components, and returns responses.
- 🔑 **Authentication Controller:** Authenticates users and authorizes user requests.
- 📜 **JWT Controller:** Issues and validates JWT tokens.
- 🛡️ **WAF:** Scans traffic, blocks potentially malicious requests, and ensures user input compliance with defined rules.
- 🔏 **Certificate Controller:** Manages digital certificates for all components.
- 📊 **SIEM:** Monitors and logs security-sensitive requests.

---

## 🐳 Docker Setup

### Prerequisites

- 🐳 Docker and Docker Compose installed
- 🐙 Git installed

### Architecture

```
Browser
  │
  ▼ https://localhost (443)
Nginx (SSL termination + reverse proxy)
  ├── /                → serves React SPA
  ├── /auth/**         ─┐
  ├── /users/**         ├─ proxy → Spring Boot (8443)
  ├── /comments/**      │
  └── /forumrooms/**   ─┘
  │
  ▼
Spring Boot (8443, HTTPS)
  │
  ▼
MySQL (3306)
```

### Running with Docker

**1. Clone the repository**

```bash
git clone https://github.com/pero-grubac/Chat-room.git
cd Chat-room
```

**2. Configure environment variables**

Edit the `.env` file:

```env
# Database
MYSQL_ROOT_PASSWORD=root
MYSQL_DATABASE=chatroom
DB_USERNAME=root
DB_PASSWORD=root

# SSL Certificate
SSL_KEY_ALIAS=chatroom
SSL_KEY_STORE_PASSWORD=chatroom123
SSL_KEY_PASSWORD=chatroom123

# JWT signing key (min 32 characters)
JWT_SIGNING_KEY=your-secret-key-here

# Gmail — use App Password, not your real password
# How to get one: Google Account → Security → 2-Step Verification → App passwords
MAIL_USERNAME=your.email@gmail.com
MAIL_PASSWORD=xxxx xxxx xxxx xxxx

# Google OAuth2 (optional — app works without it)
GOOGLE_CLIENT_ID=dummy-client-id
GOOGLE_CLIENT_SECRET=dummy-client-secret
```

**3. Start all services**

```bash
docker compose up --build -d
```

**4. Open in browser**

Navigate to **https://localhost**

> The browser will warn about a self-signed certificate — click **Advanced → Proceed to localhost**.

### 🔑 Default Admin Account

| Field    | Value    |
| -------- | -------- |
| Username | admin    |
| Password | admin123 |

> Login requires 2FA. The verification token will be sent to the admin email.
> If Gmail is not configured, the token appears in the backend logs:
>
> ```bash
> docker compose logs -f backend
> ```

### Useful Commands

```bash
# View logs
docker compose logs -f backend
docker compose logs -f frontend

# Stop all containers
docker compose down

# Stop and delete database (resets all data)
docker compose down -v

# Rebuild a single service
docker compose up --build backend
```
