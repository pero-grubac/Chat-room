import axios from "axios";
import environment from "../Enviroments";
const baseUrlConfig = {
  baseUrl: environment().baseServiceUrl,
};
// eslint-disable-next-line import/no-anonymous-default-export
export default {
  service: () => {
    const instance = axios.create(baseUrlConfig);
    instance.defaults.headers.common["Content-Type"] = "application/json";
    return instance;
  },
};
