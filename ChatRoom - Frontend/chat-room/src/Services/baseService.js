import axios from "./axios";
import environment from "../Enviroments";
const baseUrlConfig = {
  baseUrl: environment().baseServiceUrl,
};
// eslint-disable-next-line import/no-anonymous-default-export
export default {
  services: () => {
    const instance = axios.create(baseUrlConfig);
    instance.defaults.header.common["Content-Type"] = "application/json";
    return instance;
  },
};
