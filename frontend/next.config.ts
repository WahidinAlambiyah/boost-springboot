import type { NextConfig } from "next";

const ALAMBIYAH_FRONTEND_HOSTNAME = "protofeone.alambiyah.com";
const ALAMBIYAH_API_BASE_URL = "https://protobeone.alambiyah.com";
const LOOPBACK_HOSTNAMES = new Set(["localhost", "127.0.0.1", "0.0.0.0", "::1"]);

const normalizeEnvValue = (value: string | undefined): string | null => {
  if (!value) {
    return null;
  }

  const trimmed = value.trim();
  return trimmed.length > 0 ? trimmed : null;
};

const isLoopbackHostname = (hostname: string) => {
  const normalizedHostname = hostname.toLowerCase();
  return LOOPBACK_HOSTNAMES.has(normalizedHostname) || normalizedHostname.endsWith(".localhost");
};

const resolveBackendApiBaseUrl = () => {
  const configuredApiBaseUrl =
    normalizeEnvValue(process.env.NEXT_PUBLIC_API_URL) ??
    normalizeEnvValue(process.env.NEXT_PUBLIC_API_BASE_URL);

  if (!configuredApiBaseUrl) {
    return ALAMBIYAH_API_BASE_URL;
  }

  try {
    const configuredUrl = new URL(configuredApiBaseUrl);
    if (isLoopbackHostname(configuredUrl.hostname)) {
      return ALAMBIYAH_API_BASE_URL;
    }
  } catch {
    return ALAMBIYAH_API_BASE_URL;
  }

  return configuredApiBaseUrl;
};

const nextConfig: NextConfig = {
  allowedDevOrigins: [ALAMBIYAH_FRONTEND_HOSTNAME, "*.alambiyah.com"],
  async rewrites() {
    return [
      {
        source: "/api/:path*",
        destination: `${resolveBackendApiBaseUrl()}/api/:path*`,
      },
    ];
  },
};

export default nextConfig;
