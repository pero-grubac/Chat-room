import base from "./baseService";

const instance = base.service(true);

export const getComments = async (id) => {
  const response = await instance.get(`/comments/${id}`);
  return response;
};

export const createComment = async (text, idForumRoom) => {
  const response = await instance.post("/comments/create", {
    text,
    idForumRoom,
  });
  return response;
};

export const approveComment = async (id, isApproved) => {
  const response = await instance.put("/comments/approve", {
    id,
    isApproved,
  });
  return response;
};

export const getRequestedComments = async (id) => {
  const response = await instance.get(`/comments/requests/${id}`);
  return response;
};

export const updateComment = async (idComment, text, idForumRoom) => {
  const response = await instance.put("/comments/update", {
    idComment,
    text,
    idForumRoom,
  });
  return response;
};

export const deleteComment = async (id) => {
  const response = await instance.delete(`/comments/delete/${id}`);
  return response;
};

// eslint-disable-next-line import/no-anonymous-default-export
export default {
  getComments,
  createComment,
  approveComment,
  getRequestedComments,
  updateComment,
  deleteComment,
};
