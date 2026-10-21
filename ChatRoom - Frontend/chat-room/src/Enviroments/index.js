import developmentConfig from "./config.development.json";
import productionConfig from "./config.production.json";

// Vite sets MODE to "development" (dev server), "production" (build) or "test" (Vitest)
const configFor = (mode) => {
  switch (mode) {
    case "development":
    case "test":
      return developmentConfig;
    case "production":
      return productionConfig;
    default:
      throw new Error(`Unknown mode "${mode}"`);
  }
};

const environment = () => {
  const config = configFor(import.meta.env.MODE);
  return {
    ...config,
    // Lets a deployment use its own Google OAuth client without editing the JSON files
    googleClientId: import.meta.env.VITE_GOOGLE_CLIENT_ID || config.googleClientId,
  };
};

export default environment;
