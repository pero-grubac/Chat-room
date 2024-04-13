import base from "./baseService";
const instance = base.service(true);

export const getForumRooms = async (navigate) => {
  try {
    const response = await instance.get("/forumrooms");
    return response;
  } catch (error) {
    handleErrorResponse(error, navigate);
  }
};

export const updateForumRoom = async (id, name, navigate) => {
  try {
    const response = await instance.put("/forumrooms", { id, name });
    return response;
  } catch (error) {
    handleErrorResponse(error, navigate);
  }
};

export const deleteForumRoom = async (id, navigate) => {
  try {
    const response = await instance.delete(`/forumrooms/delete/${id}`);
    return response;
  } catch (error) {
    handleErrorResponse(error, navigate);
  }
};

export const getForumRoom = async (id, navigate) => {
  try {
    const response = await instance.get(`/forumrooms/${id}`);
    return response;
  } catch (error) {
    handleErrorResponse(error, navigate);
  }
};
export const createForumRoom = async (name, navigate) => {
  try {
    const response = await instance.post("/forumrooms", { name });
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
  getForumRooms,
  updateForumRoom,
  deleteForumRoom,
  getForumRoom,
  createForumRoom,
};
