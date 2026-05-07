import type { NextRequest } from "next/server";

const ALAMBIYAH_API_BASE_URL = "https://protobeone.alambiyah.com";
const LOOPBACK_HOSTNAMES = new Set(["localhost", "127.0.0.1", "0.0.0.0", "::1"]);
const HOP_BY_HOP_HEADERS = new Set([
  "connection",
  "content-encoding",
  "content-length",
  "keep-alive",
  "proxy-authenticate",
  "proxy-authorization",
  "te",
  "trailer",
  "transfer-encoding",
  "upgrade",
]);

export const dynamic = "force-dynamic";

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

const getRequestHostname = (request: NextRequest) => {
  const forwardedHost = request.headers.get("x-forwarded-host");
  const host = forwardedHost ?? request.headers.get("host") ?? "";
  return host.split(":")[0] ?? "";
};

const resolveBackendApiBaseUrl = (request: NextRequest) => {
  const configuredApiBaseUrl =
    normalizeEnvValue(process.env.NEXT_PUBLIC_API_URL) ??
    normalizeEnvValue(process.env.NEXT_PUBLIC_API_BASE_URL);

  if (!configuredApiBaseUrl) {
    return ALAMBIYAH_API_BASE_URL;
  }

  try {
    const configuredUrl = new URL(configuredApiBaseUrl);
    const requestHostname = getRequestHostname(request);
    if (isLoopbackHostname(configuredUrl.hostname) && requestHostname && !isLoopbackHostname(requestHostname)) {
      return ALAMBIYAH_API_BASE_URL;
    }

    return configuredApiBaseUrl;
  } catch {
    return ALAMBIYAH_API_BASE_URL;
  }
};

const createProxyHeaders = (request: NextRequest) => {
  const headers = new Headers(request.headers);

  HOP_BY_HOP_HEADERS.forEach((header) => headers.delete(header));
  headers.delete("host");

  return headers;
};

const createResponseHeaders = (headers: Headers) => {
  const responseHeaders = new Headers(headers);

  HOP_BY_HOP_HEADERS.forEach((header) => responseHeaders.delete(header));

  return responseHeaders;
};

const createTargetUrl = async (request: NextRequest, context: { params: Promise<{ path: string[] }> }) => {
  const { path } = await context.params;
  const encodedPath = path.map((segment) => encodeURIComponent(segment)).join("/");
  return `${resolveBackendApiBaseUrl(request)}/api/${encodedPath}${request.nextUrl.search}`;
};

const proxyRequest = async (request: NextRequest, context: { params: Promise<{ path: string[] }> }) => {
  const method = request.method.toUpperCase();
  const body = method === "GET" || method === "HEAD" ? undefined : await request.arrayBuffer();

  try {
    const upstreamResponse = await fetch(await createTargetUrl(request, context), {
      method,
      headers: createProxyHeaders(request),
      body,
      cache: "no-store",
      redirect: "manual",
    });

    return new Response(upstreamResponse.body, {
      status: upstreamResponse.status,
      statusText: upstreamResponse.statusText,
      headers: createResponseHeaders(upstreamResponse.headers),
    });
  } catch {
    return Response.json(
      {
        status: 502,
        message: "Backend API tidak dapat dijangkau dari frontend proxy.",
        data: null,
      },
      { status: 502 },
    );
  }
};

export const GET = proxyRequest;
export const POST = proxyRequest;
export const PUT = proxyRequest;
export const PATCH = proxyRequest;
export const DELETE = proxyRequest;
export const HEAD = proxyRequest;
export const OPTIONS = proxyRequest;
