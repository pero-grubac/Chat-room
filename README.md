# Chat room

This project is an internet forum where registered users can exchange opinions on various topics. The forum supports the following categories: Science, Culture, Sports, and Music.

## Features

### User Groups
- **Administrator**: Manages user accounts, approves or bans access, defines user groups, and sets permissions (adding, editing, deleting comments).
- **Moderator**: Can access and manage comments, approve new comments, or ban them.
- **Forum User**: Can access all forum areas, view the latest 20 comments in each category, and participate in discussions.

### Functionalities
- **Registration and Authentication**: New users can register and must be approved by an administrator. Two-factor authentication is used for logging in. Users receive a verification code via email after entering their username and password.
- **Comment Management**: Moderators and administrators can manage comments, including approving, adjusting, or banning them.
- **JWT Token**: Used for tracking user sessions after successful login.
- **Web Application Firewall (WAF)**: Scans traffic to the web server and only allows benign requests, blocking potentially malicious ones.
- **Certificate Management**: Manages the issuance, tracking, and revocation of digital certificates for all components.
- **SIEM Component**: Monitors and logs all security-sensitive requests.
- **OAuth2 Login**: Users can also log in using an external account (Google, GitHub, etc.) via OAuth2 framework.

## Technologies Used
- **Frontend**: React
- **Backend**: Spring Boot
- **Database**: MySQL
- **Authentication**: OAuth2 (Google account)
- **Security**: JWT, WAF, SIEM

## Architecture

### Components
1. **Access Controller**: Accepts user requests, communicates with other components, and returns responses.
2. **Authentication Controller**: Authenticates users and authorizes user requests.
3. **JWT Controller**: Issues and validates JWT tokens.
4. **WAF**: Scans traffic, blocks potentially malicious requests, and ensures user input compliance with defined rules.
5. **Certificate Controller**: Manages digital certificates for all components.
6. **SIEM**: Monitors and logs security-sensitive requests.

## Getting Started

### Prerequisites
- Node.js and npm installed
- Java Development Kit (JDK) installed
- Maven installed
- MySQL installed and running
- Git installed
