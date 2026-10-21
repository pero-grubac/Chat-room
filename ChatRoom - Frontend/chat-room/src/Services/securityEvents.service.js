import base from "./baseService";

const instance = base.service();

// Newest first; type narrows the list to one event kind (admin only)
export const getSecurityEvents = async (limit, type) => {
  const response = await instance.get("/security-events", {
    params: type ? { limit, type } : { limit },
  });
  return response;
};
