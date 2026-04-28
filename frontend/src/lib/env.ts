const normalizeEnvValue = (value: string | undefined): string | null => {
  if (!value) {
    return null;
  }

  const trimmed = value.trim();
  return trimmed.length > 0 ? trimmed : null;
};

const resolveApiBaseUrlFromPublicEnv = (): string | null => {
  return (
    normalizeEnvValue(process.env.NEXT_PUBLIC_API_URL) ??
    normalizeEnvValue(process.env.NEXT_PUBLIC_API_BASE_URL)
  );
};

export const getApiBaseUrl = (): string => {
  const resolvedApiBaseUrl = resolveApiBaseUrlFromPublicEnv();
  if (resolvedApiBaseUrl) {
    return resolvedApiBaseUrl;
  }

  throw new Error(
    `[request] Missing API base URL. Set NEXT_PUBLIC_API_URL (preferred) or NEXT_PUBLIC_API_BASE_URL in .env.local / deployment environment variables before making API requests.`,
  );
};
