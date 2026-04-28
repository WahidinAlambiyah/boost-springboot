const API_URL_ENV_KEYS = [
  "NEXT_PUBLIC_API_URL",
  "NEXT_PUBLIC_API_BASE_URL",
] as const;

const readPublicEnv = (key: string): string | null => {
  const value = process.env[key];

  if (!value) {
    return null;
  }

  const trimmedValue = value.trim();
  return trimmedValue.length > 0 ? trimmedValue : null;
};

export const getApiBaseUrl = (): string => {
  for (const key of API_URL_ENV_KEYS) {
    const value = readPublicEnv(key);
    if (value) {
      return value;
    }
  }

  throw new Error(
    `[request] Missing API base URL. Set NEXT_PUBLIC_API_URL (preferred) or NEXT_PUBLIC_API_BASE_URL in .env.local / deployment environment variables before making API requests.`,
  );
};
