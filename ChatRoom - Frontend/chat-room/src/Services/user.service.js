import base from "./baseService";

const instance = base.service(true);

export const getUsers = async () => {
  const response = await instance.get("/users");
  return response;
};

export const changeRole = async (user) => {
  const response = await instance.put("/users/changeRole", {
    id: user.id,
    role: user.role,
    permissions: user.permissions,
  });
  return response;
};

export const approveUser = async (user) => {
  const response = await instance.put("/users/approve", {
    id: user.id,
    isApproved: user.isApproved,
    role: user.role,
    permissions: user.permissions,
  });
  return response;
};

export const deleteUser = async (id) => {
  const response = await instance.delete(`/users/delete/${id}`);
  return response;
};

export const requests = async () => {
  const response = await instance.get("/users/requests");
  return response;
};

export const getApprovedUser = async (id) => {
  const response = await instance.get(`/users/approved/${id}`);
  return response;
};

export const updateUser = async (user) => {
  const response = await instance.put("/users/update", {
    id: user.id,
    username: user.username,
    password: user.password,
    email: user.email,
    role: user.role,
    permissions: user.permissions,
  });
  return response;
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
