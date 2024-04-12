import developmentConfig from "./config.development.json";
import productionConfig from "./config.production.json"
// eslint-disable-next-line import/no-anonymous-default-export
export default () => {
  switch (process.env.NODE_ENV) {
    case "development": {
      return developmentConfig;
    }
    case "production": {
      return productionConfig;
    }
    default: {
      throw new Error("NODE_ENV not being set");
    }
  }
};
