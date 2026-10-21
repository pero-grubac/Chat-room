import { message } from "antd";

// Detail the server sends when the WAF blocks a request; the WAF also closes the session
export const WAF_BLOCKED = "Blocked by the web application firewall";

// A 401 means the server no longer accepts the session (it already dropped the httpOnly cookie)
export const isSessionLost = (error) =>
  error?.response?.status === 401 || error?.response?.data?.detail === WAF_BLOCKED;

// The server's own explanation (RFC 7807 "detail") where it helps the user, otherwise the fallback
const report = (error, fallback) => {
  const status = error?.response?.status;
  const detail = error?.response?.data?.detail;
  const explained = [400, 403, 404, 409, 429].includes(status);
  if (detail === WAF_BLOCKED) {
    message.error("The request was blocked by the security filter and you were signed out.");
  } else {
    message.error(explained && detail ? detail : fallback);
  }
};

// Wrong credentials stay generic; pending accounts and rate limits are explained
export const handleLoginError = (error) => report(error, "Invalid credentials");

export const handleTokenError = () => message.error("Invalid token");

export const handleGetForumRooms = (error) => report(error, "Error happened");

export const handleUpdateForumRoom = (error) => report(error, "Error happened while updateing room");

export const handleCreateForumRoom = (error) => report(error, "Error happened while creating room");

export const hadnleDeleteForumRoom = (error) => report(error, "Error happened while deleting room");

export const hadnleCreateComment = (error) => report(error, "Error happened while creating comment");

export const hadnleGetComments = (error) => report(error, "Error happened while getting comments");

export const handleGetRequestedComments = (error) =>
  report(error, "Error happened while getting comment requests");

export const handleDeleteComments = (error) => report(error, "Error happened while deleting the comment");

export const handleApproveComments = (error) => report(error, "Error happened while moderating the comment");

export const handleUpdateComments = (error) => report(error, "Error happened while updating the comment");

export const handleGetAllUsers = (error) => report(error, "Error happened while fetching users");

export const handleChangeRole = (error) => report(error, "Error happened while changing role of a user");

export const handleSetAccess = (error) => report(error, "Error happened while changing access of a user");

export const handleApproveUser = (error) => report(error, "Error happened while approving the registration");

export const handleRejectUser = (error) => report(error, "Error happened while rejecting the registration");

export const handleGetRequests = (error) => report(error, "Error happened while getting requests");

export const handleRegisterUser = (error) => report(error, "Error happened during registration");

export const handleGetSecurityEvents = (error) => report(error, "Error happened while loading the security log");
