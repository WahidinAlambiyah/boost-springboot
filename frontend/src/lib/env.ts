const LOOPBACK_HOSTNAMES = new Set(["localhost", "127.0.0.1", "0.0.0.0", "::1"]);

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

const isLoopbackHostname = (hostname: string) => {
  const normalizedHostname = hostname.toLowerCase();
  return LOOPBACK_HOSTNAMES.has(normalizedHostname) || normalizedHostname.endsWith(".localhost");
};

const getBrowserHostname = () => {
  if (typeof window === "undefined") {
    return null;
  }

  return window.location.hostname;
};

const getAlambiyahApiBaseUrl = (frontendHostname: string) => {
  if (frontendHostname === "protofeone.alambiyah.com") {
    return "https://protobeone.alambiyah.com";
  }

  return null;
};

const resolveRuntimeApiBaseUrl = (configuredApiBaseUrl: string | null) => {
  const browserHostname = getBrowserHostname();
  if (!browserHostname || isLoopbackHostname(browserHostname)) {
    return configuredApiBaseUrl;
  }

  const alambiyahApiBaseUrl = getAlambiyahApiBaseUrl(browserHostname);
  if (!alambiyahApiBaseUrl) {
    return configuredApiBaseUrl;
  }

  if (!configuredApiBaseUrl) {
    return alambiyahApiBaseUrl;
  }

  try {
    const configuredUrl = new URL(configuredApiBaseUrl);
    if (isLoopbackHostname(configuredUrl.hostname)) {
      return alambiyahApiBaseUrl;
    }
  } catch {
    return alambiyahApiBaseUrl;
  }

  return configuredApiBaseUrl;
};

export const getApiBaseUrl = (): string => {
  const resolvedApiBaseUrl = resolveRuntimeApiBaseUrl(resolveApiBaseUrlFromPublicEnv());
  if (resolvedApiBaseUrl) {
    return resolvedApiBaseUrl;
  }

  throw new Error(
    `[request] Missing API base URL. Set NEXT_PUBLIC_API_URL (preferred) or NEXT_PUBLIC_API_BASE_URL in .env.local / deployment environment variables before making API requests.`,
  );
};
