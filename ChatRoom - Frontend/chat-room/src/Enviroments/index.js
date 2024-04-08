import developmnetConfig from "./config.development.json";

// eslint-disable-next-line import/no-anonymous-default-export
export default () => {
  switch (process.env.NODE_ENV) {
    case "development": {
      return developmnetConfig;
    }
    default: {
      throw new Error("NODE_ENV not being set");
    }
  }
};
