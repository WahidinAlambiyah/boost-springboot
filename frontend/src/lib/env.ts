const API_URL_ENV_KEY = "NEXT_PUBLIC_API_URL";

const readRequiredPublicEnv = (key: string): string => {
  const value = process.env[key];

  if (!value || value.trim().length === 0) {
    throw new Error(
      `[startup] Missing required environment variable ${key}. Set it in .env.local / Vercel environment variables before starting the frontend.`,
    );
  }

  return value;
};

export const API_BASE_URL = readRequiredPublicEnv(API_URL_ENV_KEY);
