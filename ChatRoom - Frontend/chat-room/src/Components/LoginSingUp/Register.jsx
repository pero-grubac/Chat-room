import { useState } from "react";
import "./LoginSingUp.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faUser } from "@fortawesome/free-solid-svg-icons";
import { faLock } from "@fortawesome/free-solid-svg-icons";
import { faEnvelope } from "@fortawesome/free-solid-svg-icons";
import { message } from "antd";
import { Link, useNavigate } from "react-router-dom";
import { handleRegisterUser } from "../Error/ErrorMessage";
import { register } from "../../Services/login.service";
import { EMAIL_MAX_LENGTH, PASSWORD_MAX_LENGTH, USERNAME_MAX_LENGTH } from "../Util/limits";

// The rule the server enforces: 8 to 72 characters, lower and upper case, a digit and a special character
export const isStrongPassword = (password) =>
  password.length >= 8 &&
  password.length <= PASSWORD_MAX_LENGTH &&
  /[a-z]/.test(password) &&
  /[A-Z]/.test(password) &&
  /\d/.test(password) &&
  /[^A-Za-z0-9]/.test(password);

const Register = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [email, setEmail] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [messageApi, contextHolder] = message.useMessage();
  const navigate = useNavigate();

  const handleRegister = async (e) => {
    // Without this the browser submits the form itself and reloads the page mid-request
    e.preventDefault();
    if (!isStrongPassword(password)) {
      messageApi.error(
        "Password must have 8 to 72 characters, with uppercase and lowercase letters, a number and a special character."
      );
      return;
    }
    setSubmitting(true);
    try {
      await register(username.trim(), password, email.trim());
      message.success("You are registered. You can sign in once an administrator approves the account.");
      navigate("/login");
    } catch (error) {
      handleRegisterUser(error);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className={`wrapper active`}>
      {contextHolder}
      <div className="form-box register">
        <form onSubmit={handleRegister}>
          <h1>Registration</h1>
          <div className="input-box">
            <FontAwesomeIcon icon={faUser} className="icon" />
            <input
              type="text"
              placeholder="Username"
              required
              maxLength={USERNAME_MAX_LENGTH}
              value={username}
              onChange={(e) => setUsername(e.target.value)}
            />
          </div>
          <div className="input-box">
            <FontAwesomeIcon icon={faEnvelope} className="icon" />
            <input
              type="email"
              placeholder="Email"
              required
              maxLength={EMAIL_MAX_LENGTH}
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </div>
          <div className="input-box">
            <FontAwesomeIcon icon={faLock} className="icon" />
            <input
              type="password"
              placeholder="Password"
              required
              maxLength={PASSWORD_MAX_LENGTH}
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>
          <button type="submit" disabled={submitting}>
            Register
          </button>

          <div className="register-link">
            <p>
              Already have an account? <Link to="/login">Login</Link>
            </p>
          </div>
        </form>
      </div>
    </div>
  );
};
export default Register;
