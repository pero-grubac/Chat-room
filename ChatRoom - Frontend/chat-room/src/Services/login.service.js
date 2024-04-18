import base from "./baseService";
import { jwtDecode } from "jwt-decode";

const instance = base.service(false);

export const loginUser = async (username, password) => {
  const response = await instance.post("/auth/login", { username, password });
  return response;
};

export const logout = () => sessionStorage.clear();

export const verifyToken = async (username, password, token) => {
  const response = await instance.post("/auth/token", {
    username,
    password,
    token,
  });
  const user = response.data;
  sessionStorage.setItem("auth", user.token);
  const userDetails = jwtDecode(sessionStorage.getItem("auth"));
  sessionStorage.setItem("role", userDetails.role);
  sessionStorage.setItem("sub", userDetails.sub);
  sessionStorage.setItem("permissions", userDetails.permissions);
  return response;
};

export const register = async (username, password, email) => {
  const response = await instance.post("/users/add", {
    username,
    password,
    email,
  });
  return response;
};
export const oauth = async (oiduser) => {
  const response = await instance.post("/auth/oauth", oiduser);
  return response;
};

// eslint-disable-next-line import/no-anonymous-default-export
export default {
  loginUser,
  logout,
  verifyToken,
  register,
  oauth,
};
