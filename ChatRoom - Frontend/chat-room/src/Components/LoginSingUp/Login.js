import React, { useState, useEffect } from "react";
import "./LoginSingUp.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faUser } from "@fortawesome/free-solid-svg-icons";
import { faLock } from "@fortawesome/free-solid-svg-icons";
import { faUserSecret } from "@fortawesome/free-solid-svg-icons";
import {
  loginUser,
  oauth,
  verifyToken,
  verifyTokenOAuth,
} from "../../Services/login.service";
import { message } from "antd";
import {  useNavigate } from "react-router-dom";
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
  const [isOAuth, setIsOAuth] = useState(false);
  const [email, setEmail] = useState("");
  const clearFields = () => {
    setPassword("");
    setUsername("");
    setToken("");
  };

  const googleLogin = useGoogleLogin({
    onSuccess: async (tokenResponse) => {
      try {
        const userInfo = await axios.get(
          "https://www.googleapis.com/oauth2/v3/userinfo",
          {
            headers: { Authorization: `Bearer ${tokenResponse.access_token}` },
          }
        );

        const callDB = await oauth(userInfo.data);

        if (callDB.status === 200) {
          showMessage(
            "success",
            "You have successfully loged in. Procede with token."
          );
          setEmail(userInfo.data.email);
          setUsername(userInfo.data.name);
          setIsOAuth(true);
          setIsToken(true);
        } else if (callDB.status === 201) {
          showMessage("success", "You have successfully registered");
        } else {
          showMessage("error", "Something wrong with account");
        }
      } catch (error) {
        handleLoginError();
      }
    },
  });

  const registerLink = () => {
    setIsToken(false);
    clearFields();
    navigate("/register");
  };
  useEffect(() => {
    if ((!username && username !== "") || (!token && token !== "")) return;
    // dispatch(logintoken(username, password, token));
  }, [password, token, username]);

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
  const userTokenOAuth = async () => {
    try {
      const response = await verifyTokenOAuth(email, token, "GOOGLE");
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
  const userToken = async () => {
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
  const handleToken = async (e) => {
    e.preventDefault();
    if (isOAuth) {
      userTokenOAuth();
    } else {
      userToken();
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
