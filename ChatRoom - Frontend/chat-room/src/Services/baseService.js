import axios from "axios";
import environment from "../Enviroments";
const baseUrlConfig = {
  baseUrl: environment().baseServiceUrl,
};
// eslint-disable-next-line import/no-anonymous-default-export
export default {
  service: (useAuth) => {
    const instance = axios.create({
      baseURL: baseUrlConfig.baseUrl,
    });
    instance.defaults.headers.common["Content-Type"] = "application/json";
    if (useAuth) {
      instance.interceptors.request.use(
        async (config) => {
          const token = sessionStorage.getItem("auth");
          if (token) {
            config.headers = {
              ...config.headers,
              Authorization: `Bearer ${token}`,
            };
          }
          return config;
        },
        (error) => {
          return Promise.reject(error);
        }
      );
    }
    return instance;
  },
};
