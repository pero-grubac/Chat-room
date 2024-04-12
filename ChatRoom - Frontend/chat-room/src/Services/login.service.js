import base from "./baseService";

const instance = base.service(false);

export const loginUser = async (username, password, navigate) => {
  try {
    const response = await instance.post("/auth/login", { username, password });
    return response;
  } catch (error) {
    handleErrorResponse(error, navigate);
  }
};

export const verifyToken = async (username, password, token, navigate) => {
  try {
    const response = await instance.post("/auth/token", {
      username,
      password,
      token,
    });
    return response;
  } catch (error) {
    handleErrorResponse(error, navigate);
  }
};

const handleErrorResponse = (error, navigate) => {
  if (error.response.status === 403) {
    navigate("/error403");
  } else if (error.response.status === 404) {
    navigate("/error404");
  } else if (error.response.status === 406) {
    navigate("/error406");
  } else if (error.response.status === 500) {
    navigate("/error500");
  } else {
    navigate("/unknown");
  }
};

// eslint-disable-next-line import/no-anonymous-default-export
export default {
  loginUser,
  verifyToken,
};
