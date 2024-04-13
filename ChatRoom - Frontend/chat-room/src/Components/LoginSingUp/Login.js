import React, { useState } from "react";
import "./LoginSingUp.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faUser } from "@fortawesome/free-solid-svg-icons";
import { faLock } from "@fortawesome/free-solid-svg-icons";
import { faUserSecret } from "@fortawesome/free-solid-svg-icons";
import { loginUser, verifyToken } from "../../Services/login.service";
import { message } from "antd";
import { useNavigate } from "react-router-dom";
import { useGoogleLogin } from "@react-oauth/google";
import axios from "axios";
import { useDispatch } from "react-redux";
import { handleLoginError, handleTokenError } from "../Error/ErrorMessage";
import { authState } from "../Redux/slices/userSlice"; 

const Login = () => {
  const [isToken, setIsToken] = useState(false);
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [token, setToken] = useState("");
  const [messageApi, contextHolder] = message.useMessage();
  const navigate = useNavigate();
  const dispatch = useDispatch();

  const clearFields = () => {
    setPassword("");
    setUsername("");
    setToken("");
  };

  const googleLogin = useGoogleLogin({
    onSuccess: async (tokenResponse) => {
      const userInfo = await axios
        .get("https://www.googleapis.com/oauth2/v3/userinfo", {
          headers: { Authorization: `Bearer ${tokenResponse.access_token}` },
        })
        .then((res) => res.data);

      console.log(userInfo);
    },
  });

  const registerLink = () => {
    navigate("/register");
    setIsToken(false);
    clearFields();
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const response = await loginUser(username, password);
      if (response.status === 200) {
        showMessage(
          "success",
          "You have successfully loged in. Procede with token."
        );
        setIsToken(true);
      }
    } catch (error) {
      handleLoginError();
    }
  };

  const showMessage = (type, content) => {
    messageApi.open({
      type: type,
      content: content,
    });
  };

  const handleToken = async (e) => {
    e.preventDefault();
    try {
      const response = await verifyToken(username, password, token);
      if (response.status === 200) {
        showMessage("success", "Wellcome.");
        dispatch(authState());
        navigate("/forum_rooms");
      } else {
        setIsToken(false);
      }
    } catch (error) {
      console.log(error);
      setIsToken(false);
      handleTokenError();
    }
  };

  const returnToLogin = () => {
    setIsToken(false);
  };

  return (
    <div className={`wrapper`}>
      {contextHolder}
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
            <button type="submit" onClick={handleLogin}>
              Login
            </button>
            <br />
            <br />
            <div>
              <button onClick={() => googleLogin()}>
                Sign in with Google 🚀
              </button>
            </div>
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
    </div>
  );
};

export default Login;
