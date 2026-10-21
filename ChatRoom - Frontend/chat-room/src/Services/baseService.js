import axios from "axios";
import environment from "../Enviroments";

const baseUrlConfig = {
  baseUrl: environment().baseServiceUrl,
};

// The session is an httpOnly cookie the browser attaches by itself, so no token handling here
export default {
  service: () => {
    const instance = axios.create({
      baseURL: baseUrlConfig.baseUrl,
      withCredentials: true,
    });
    instance.defaults.headers.common["Content-Type"] = "application/json";
    return instance;
  },
};
