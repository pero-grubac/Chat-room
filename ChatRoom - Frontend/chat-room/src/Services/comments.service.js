import base from "./baseService";

const instance = base.service(true);

export const getComment = async (id, navigate) => {
  try {
    const response = await instance.get(`/comments/${id}`);
    return response;
  } catch (error) {
    handleErrorResponse(error, navigate);
  }
};

export const createComment = async (text, idForumRoom, idUser, navigate) => {
  try {
    const response = await instance.post("/comments", {
      text,
      idForumRoom,
      idUser,
    });
    return response;
  } catch (error) {
    handleErrorResponse(error, navigate);
  }
};

export const approveComment = async (id, isApproved, navigate) => {
  try {
    const response = await instance.get("/comments/approve", {
      id,
      isApproved,
    });
    return response;
  } catch (error) {
    handleErrorResponse(error, navigate);
  }
};

export const getRequestedComment = async (id, navigate) => {
  try {
    const response = await instance.get(`/comments/requests/${id}`);
    return response;
  } catch (error) {
    handleErrorResponse(error, navigate);
  }
};

export const updateComment = async (
  idComment,
  text,
  idForumRoom,
  idUser,
  navigate
) => {
  try {
    const response = await instance.put("/comments/update", {
      idComment,
      text,
      idForumRoom,
      idUser,
    });
    return response;
  } catch (error) {
    handleErrorResponse(error, navigate);
  }
};

export const deleteComment = async (id, navigate) => {
  try {
    const response = await instance.delete(`/comments/delete/${id}`);
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
  getComment,
  createComment,
  approveComment,
  getRequestedComment,
  updateComment,
  deleteComment,
};
