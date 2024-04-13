import React, { useState } from "react";
import "./LoginSingUp.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faUser } from "@fortawesome/free-solid-svg-icons";
import { faLock } from "@fortawesome/free-solid-svg-icons";
import { faEnvelope } from "@fortawesome/free-solid-svg-icons";
import { message } from "antd";
import { useNavigate } from "react-router-dom";

const Register = () => {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [email, setEmail] = useState("");
  const [messageApi, contextHolder] = message.useMessage();
  const navigate = useNavigate();

  const showMessage = (type, content) => {
    messageApi.open({
      type: type,
      content: content,
    });
  };
// login uradi
  const handleRegister = () => {
    const strongPasswordRegex =
      /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()_+}{"':;?/>.<,])(?=.{8,})/;
    const isValidPassword = strongPasswordRegex.test(password);
    if (isValidPassword) {
      showMessage("success", "You have successfully registered");
    } else {
      showMessage(
        "error",
        "Password must contain at least 8 characters, including uppercase and lowercase letters, numbers, and special characters."
      );
    }
  };

  const backToLogin = () => {
    navigate("/login");
    clearFields()
  };
const clearFields=()=>{
    setEmail("")
    setPassword("")
    setUsername("")
}
  return (
    <div className={`wrapper active`}>
      {contextHolder}
      <div className="form-box register">
        <form action="">
          <h1>Registration</h1>
          <div className="input-box">
            <FontAwesomeIcon icon={faUser} className="icon" />
            <input
              type="text"
              placeholder="Username"
              required
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
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>
          <button type="submit" onClick={handleRegister}>
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
export default Register;
