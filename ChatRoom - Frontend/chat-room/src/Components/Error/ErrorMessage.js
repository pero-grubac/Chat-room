import { message } from "antd";

export const handleLoginError = () => {
  message.error("Invalid credentials");
};

export const handleTokenError = () => {
  message.error("Invalid token");
};

export const handleGetForumRooms = (error) => {
  checkJWT(error);
  message.error("Error happened");
};

const checkJWT = (error) => {
  if (
    error.response.status === 401 &&
    error.response.data.message === "JWT expired"
  ) {
    sessionStorage.clear();
  }
};
