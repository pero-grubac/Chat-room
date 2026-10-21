import base from "./baseService";

const instance = base.service();

// Role, exact permission set and, for moderators, the rooms they manage (null keeps them)
export const changeRole = async (user) => {
  const response = await instance.put("/users/changeRole", {
    id: user.id,
    role: user.role,
    permissions: user.permissions,
    roomIds: user.roomIds,
  });
  return response;
};

export const approveUser = async (user) => {
  const response = await instance.put("/users/approve", {
    id: user.id,
    isApproved: user.isApproved,
    role: user.role,
    permissions: user.permissions,
    roomIds: user.roomIds,
  });
  return response;
};

// Blocks (allowed = false) or restores access for an approved account
export const setAccess = async (id, allowed) => {
  const response = await instance.put("/users/access", { id, allowed });
  return response;
};

export const requests = async () => {
  const response = await instance.get("/users/requests");
  return response;
};

// 1 = approved accounts, 0 = rejected registrations and blocked accounts
export const getApprovedUser = async (isApproved) => {
  const response = await instance.get(`/users/approved/${isApproved}`);
  return response;
};
