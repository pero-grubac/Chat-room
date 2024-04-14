import base from "./baseService";
const instance = base.service(true);

export const getForumRooms = async () => {
  const response = await instance.get("/forumrooms");
  return response;
};

export const updateForumRoom = async (id, name) => {
  const response = await instance.put("/forumrooms/update", { id, name });
  return response;
};

export const deleteForumRoom = async (id) => {
  const response = await instance.delete(`/forumrooms/delete/${id}`);
  return response;
};

export const getForumRoom = async (id) => {
  const response = await instance.get(`/forumrooms/${id}`);
  return response;
};
export const createForumRoom = async (name) => {
  const response = await instance.post("/forumrooms/create", { name });
  return response;
};

// eslint-disable-next-line import/no-anonymous-default-export
export default {
  getForumRooms,
  updateForumRoom,
  deleteForumRoom,
  getForumRoom,
  createForumRoom,
};
