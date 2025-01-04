<!DOCTYPE html>
<html>
<body>
    <h1>💬 Chat Room</h1>
    <p>
        This project is an internet forum where registered users can exchange opinions on various topics. The forum supports the following categories: Science, Culture, Sports, and Music.
    </p>

  <h2>✨ Features</h2>

  <h3>👥 User Groups</h3>
    <ul>
        <li>🔑 <strong>Administrator:</strong> Manages user accounts, approves or bans access, defines user groups, and sets permissions (adding, editing, deleting comments).</li>
        <li>🛠️ <strong>Moderator:</strong> Can access and manage comments, approve new comments, or ban them.</li>
        <li>💻 <strong>Forum User:</strong> Can access all forum areas, view the latest 20 comments in each category, and participate in discussions.</li>
    </ul>

   <h3>🛠️ Functionalities</h3>
   <ul>
        <li>📝 <strong>Registration and Authentication:</strong> New users can register and must be approved by an administrator. Two-factor authentication is used for logging in. Users receive a verification code via email after entering their username and password.</li>
        <li>📋 <strong>Comment Management:</strong> Moderators and administrators can manage comments, including approving, adjusting, or banning them.</li>
        <li>🔐 <strong>JWT Token:</strong> Used for tracking user sessions after successful login.</li>
        <li>🛡️ <strong>Web Application Firewall (WAF):</strong> Scans traffic to the web server and only allows benign requests, blocking potentially malicious ones.</li>
        <li>🔏 <strong>Certificate Management:</strong> Manages the issuance, tracking, and revocation of digital certificates for all components.</li>
        <li>📊 <strong>SIEM Component:</strong> Monitors and logs all security-sensitive requests.</li>
        <li>🌐 <strong>OAuth2 Login:</strong> Users can also log in using an external account (Google, GitHub, etc.) via the OAuth2 framework.</li>
    </ul>

  <h2>🔧 Technologies Used</h2>
    <ul>
        <li>⚛️ <strong>Frontend:</strong> React</li>
        <li>☕ <strong>Backend:</strong> Spring Boot</li>
        <li>🗄️ <strong>Database:</strong> MySQL</li>
        <li>🔐 <strong>Authentication:</strong> OAuth2 (Google account)</li>
        <li>🛡️ <strong>Security:</strong> JWT, WAF, SIEM</li>
    </ul>

   <h2>📦 Architecture</h2>

  <h3>📋 Components</h3>
    <ul>
        <li>🔓 <strong>Access Controller:</strong> Accepts user requests, communicates with other components, and returns responses.</li>
        <li>🔑 <strong>Authentication Controller:</strong> Authenticates users and authorizes user requests.</li>
        <li>📜 <strong>JWT Controller:</strong> Issues and validates JWT tokens.</li>
        <li>🛡️ <strong>WAF:</strong> Scans traffic, blocks potentially malicious requests, and ensures user input compliance with defined rules.</li>
        <li>🔏 <strong>Certificate Controller:</strong> Manages digital certificates for all components.</li>
        <li>📊 <strong>SIEM:</strong> Monitors and logs security-sensitive requests.</li>
    </ul>

  <h2>🚀 Getting Started</h2>

  <h3>📋 Prerequisites</h3>
    <ul>
        <li>📦 Node.js and npm installed</li>
        <li>☕ Java Development Kit (JDK) installed</li>
        <li>🛠️ Maven installed</li>
        <li>🗄️ MySQL installed and running</li>
        <li>🐙 Git installed</li>
    </ul>
</body>
</html>
