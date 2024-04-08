import React, { useState } from "react";
import "./LoginSingUp.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faUser } from "@fortawesome/free-solid-svg-icons";
import { faLock } from "@fortawesome/free-solid-svg-icons";
import { faEnvelope } from "@fortawesome/free-solid-svg-icons";
import { faUserSecret } from "@fortawesome/free-solid-svg-icons";
import axios from "axios";
const LoginSingUp = () => {
  const [action, setAction] = useState("");
  const [isToken, setIsToken] = useState(false);
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [token, setToken] = useState("");

  const registerLink = () => {
    setAction(" active");
    setIsToken(false);
  };
  const backToLogin = () => {
    setAction("");
    setIsToken(false);
  };
  const login = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.post(`http://localhost:8443/auth/login`, {
        username,
        password,
      });
      if (response.data.success) {
        setIsToken(true);
      }
      if (response.status === 200) {
        setIsToken(true);
      } else {
        alert("Invalid credentials");
      }
    } catch (error) {
      console.error("Error logging in:", error);
    }
  };
  const register = () => {};
  const handleToken = async (e) => {
    e.preventDefault();
    try {
      const response = await axios.post(`http://localhost:8443/auth/token`, {
        username,
        password,
        token,
      });

      if (response.status === 200) {
        console.log(response.data);
      } else {
        alert("Invalid credentials");
      }
    } catch (error) {
      console.error("Error logging in:", error);
    }
  };
  const returnToLogin = () => {
    setIsToken(false);
  };
  return (
    <div className={`wrapper${action}`}>
      {!isToken && (
        <div className="form-box login">
          <form action="">
            <h1>Login</h1>
            <div className="input-box">
              <FontAwesomeIcon icon={faUser} className="icon" />
              <input
                type="text"
                required
                value={username}
                onChange={(e) => setUsername(e.target.value)}
              />
              <label>Username</label>
            </div>
            <div className="input-box">
              <FontAwesomeIcon icon={faLock} className="icon" />
              <input
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
              <label>Password</label>
            </div>
            <button type="submit" onClick={login}>
              Login
            </button>

            <div className="register-link">
              <p>
                Don't have an account?{" "}
                <a href="#" onClick={registerLink}>
                  Register
                </a>
              </p>
            </div>
          </form>
        </div>
      )}

      {isToken && (
        <div className="form-box token">
          <form action="">
            <h1>Token</h1>
            <div className="input-box">
              <FontAwesomeIcon icon={faUserSecret} className="icon" />
              <input
                type="text"
                required
                value={token}
                onChange={(e) => setToken(e.target.value)}
              />
              <label>Token</label>
            </div>
            <button
              type="submit"
              onClick={handleToken}
              style={{ marginBottom: "10px" }}
            >
              Login with Token
            </button>

            <button onClick={returnToLogin}>Return to login</button>
            <div className="register-link">
              <p>
                Don't have an account?{" "}
                <a href="#" onClick={registerLink}>
                  Register
                </a>
              </p>
            </div>
          </form>
        </div>
      )}

      <div className="form-box register">
        <form action="">
          <h1>Registration</h1>
          <div className="input-box">
            <FontAwesomeIcon icon={faUser} className="icon" />
            <input type="text" placeholder="Username" required />
          </div>
          <div className="input-box">
            <FontAwesomeIcon icon={faEnvelope} className="icon" />
            <input type="email" placeholder="Email" required />
          </div>
          <div className="input-box">
            <FontAwesomeIcon icon={faLock} className="icon" />
            <input type="password" placeholder="Password" required />
          </div>
          <button type="submit" onClick={register}>
            Register
          </button>

          <div className="register-link">
            <p>
              Already have an account?{" "}
              <a href="#" onClick={backToLogin}>
                Login
              </a>
            </p>
          </div>
        </form>
      </div>
    </div>
  );
};

export default LoginSingUp;
