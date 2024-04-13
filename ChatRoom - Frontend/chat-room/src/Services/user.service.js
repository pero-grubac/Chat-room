import base from "./baseService";

const instance = base.service(true);

export const getUsers = async (navigate) => {
  try {
    const response = await instance.get("/users");
    return response;
  } catch (error) {
    handleErrorResponse(error, navigate);
  }
};

export const changeRole = async (id, role, permissions, navigate) => {
  try {
    const response = await instance.put("/users/changeRole", {
      id,
      role,
      permissions,
    });
    return response;
  } catch (error) {
    handleErrorResponse(error, navigate);
  }
};

export const approveUser = async (
  id,
  isApproved,
  role,
  permissions,
  navigate
) => {
  try {
    const response = await instance.put("/users/approve", {
      id,
      isApproved,
      role,
      permissions,
    });
    return response;
  } catch (error) {
    handleErrorResponse(error, navigate);
  }
};

export const deleteUser = async (id, navigate) => {
  try {
    const response = await instance.delete(`/users/delete/${id}`);
    return response;
  } catch (error) {
    handleErrorResponse(error, navigate);
  }
};

export const requests = async (navigate) => {
  try {
    const response = await instance.get("/users/requests");
    return response;
  } catch (error) {
    handleErrorResponse(error, navigate);
  }
};

export const getApprovedUser = async (id, navigate) => {
  try {
    const response = await instance.get(`/users/approved/${id}`);
    return response;
  } catch (error) {
    handleErrorResponse(error, navigate);
  }
};

export const updateUser = async (
  id,
  username,
  password,
  email,
  role,
  permissions,
  navigate
) => {
  try {
    const response = await instance.put("/users/update", {
      id,
      username,
      password,
      email,
      role,
      permissions,
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
  getUsers,
  changeRole,
  approveUser,
  deleteUser,
  requests,
  getApprovedUser,
  updateUser,
};
