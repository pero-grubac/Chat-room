import base from "./baseService";

const instance = base.service();

export const loginUser = async (username, password) => {
  const response = await instance.post("/auth/login", { username, password });
  return response;
};

// Ends the session on the server, which also removes the httpOnly cookie
export const logout = async () => {
  await instance.post("/auth/logout");
};

// The signed-in user ({ username, role, permissions }); fails with 401 when there is no session
export const currentUser = async () => {
  const response = await instance.get("/auth/me");
  return response;
};

// On success the server sets the session cookie and returns the user
export const verifyToken = async (username, password, token) => {
  const response = await instance.post("/auth/token", {
    username,
    password,
    token,
  });
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
// Google sign-in, step 1: 201 registered (pending), 200 code emailed, 403 not approved
export const oauth = async (accessToken) => {
  const response = await instance.post("/auth/oauth", { accessToken });
  return response;
};

export const verifyTokenOAuth = async (email, token, type) => {
  const response = await instance.post("/auth/oauth/token", {
    email,
    token,
    type,
  });
  return response;
};
