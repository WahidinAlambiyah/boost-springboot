import axios, {
  AxiosError,
  AxiosHeaders,
} from "axios";

import { getApiBaseUrl } from "@/lib/env";
import { logCriticalHttpError } from "@/lib/client-observability";
import { handleGlobalHttpError } from "@/lib/http-error-events";
import { addHttpDebugEntry } from "@/lib/http-debug-store";
import { ApiResponse, AuthResponse } from "@/types/api";

declare module "axios" {
  interface InternalAxiosRequestConfig {
    _retry?: boolean;
    _requestId?: string;
    _requestStartedAt?: number;
  }
}

const ACCESS_TOKEN_KEY = "accessToken";
const REFRESH_TOKEN_KEY = "refreshToken";
const ENCRYPTED_REFRESH_TOKEN_KEY = "refreshToken.enc.v1";
const REFRESH_TOKEN_CIPHER_SEED = "boost-refresh-token-seed-v1";

let accessToken: string | null = null;
let refreshTokenMemory: string | null = null;
let refreshPromise: Promise<boolean> | null = null;
const isHttpDebugEnabled =
  process.env.NEXT_PUBLIC_HTTP_DEBUG === "true" && process.env.NODE_ENV !== "production";

const createRequestId = () => {
  if (typeof crypto !== "undefined" && typeof crypto.randomUUID === "function") {
    return crypto.randomUUID();
  }

  return `req-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`;
};

const NETWORK_FAILURE_STATUS = 0;

const sanitizePath = (url?: string) => {
  if (!url) {
    return "unknown";
  }

  try {
    const normalized = new URL(url, getApiBaseUrl());
    return normalized.pathname;
  } catch {
    return url.split("?")[0] || "unknown";
  }
};

const logCriticalErrorEvent = ({
  requestId,
  method,
  path,
  status,
  duration,
}: {
  requestId?: string;
  method?: string;
  path?: string;
  status: number;
  duration?: number;
}) => {
  let tokenUserId: string | undefined;

  const token = getAccessToken();
  if (token) {
    try {
      const payloadPart = token.split(".")[1];
      if (payloadPart && typeof globalThis.atob === "function") {
        const decoded = JSON.parse(globalThis.atob(payloadPart)) as {
          userId?: string;
          sub?: string;
          id?: string;
        };
        tokenUserId = decoded.userId ?? decoded.sub ?? decoded.id;
      }
    } catch {
      tokenUserId = undefined;
    }
  }

  logCriticalHttpError({
    requestId: requestId ?? createRequestId(),
    method: method?.toUpperCase() ?? "GET",
    path: sanitizePath(path),
    status,
    duration: duration ?? 0,
    userId: tokenUserId,
  });
};

const summarizePayload = (payload: unknown, maxLength = 220) => {
  if (typeof payload === "undefined") {
    return "none";
  }

  if (payload === null) {
    return "null";
  }

  if (typeof payload === "string") {
    return payload.length > maxLength ? `${payload.slice(0, maxLength)}...` : payload;
  }

  try {
    const json = JSON.stringify(payload);
    return json.length > maxLength ? `${json.slice(0, maxLength)}...` : json;
  } catch {
    return "[unserializable]";
  }
};

const estimateResponseSize = (data: unknown) => {
  if (typeof data === "undefined" || data === null) {
    return "0B";
  }

  if (typeof data === "string") {
    return `${data.length}B`;
  }

  try {
    return `${JSON.stringify(data).length}B`;
  } catch {
    return "unknown";
  }
};

const getStorage = () => {
  if (typeof window === "undefined") {
    return null;
  }

  return window.localStorage;
};

const readTokenFromStorage = (key: string) => getStorage()?.getItem(key) ?? null;

const buildRefreshCipherKey = () => {
  if (typeof window === "undefined") {
    return REFRESH_TOKEN_CIPHER_SEED;
  }

  return `${REFRESH_TOKEN_CIPHER_SEED}:${window.location.origin}`;
};

const xorCipher = (value: string, key: string) => {
  const valueCodes = Array.from(value).map((character) => character.charCodeAt(0));
  const keyCodes = Array.from(key).map((character) => character.charCodeAt(0));
  const output = valueCodes.map((code, index) => code ^ keyCodes[index % keyCodes.length]);
  return String.fromCharCode(...output);
};

const encryptRefreshToken = (token: string) => {
  if (typeof window === "undefined") {
    return null;
  }

  try {
    const encrypted = xorCipher(token, buildRefreshCipherKey());
    return window.btoa(encrypted);
  } catch {
    return null;
  }
};

const decryptRefreshToken = (encryptedToken: string) => {
  if (typeof window === "undefined") {
    return null;
  }

  try {
    const decoded = window.atob(encryptedToken);
    return xorCipher(decoded, buildRefreshCipherKey());
  } catch {
    return null;
  }
};

export const getAccessToken = () => {
  if (accessToken) {
    return accessToken;
  }

  accessToken = readTokenFromStorage(ACCESS_TOKEN_KEY);
  return accessToken;
};

export const getRefreshToken = () => {
  if (refreshTokenMemory) {
    return refreshTokenMemory;
  }

  const storage = getStorage();
  const encryptedRefreshToken = storage?.getItem(ENCRYPTED_REFRESH_TOKEN_KEY);
  if (encryptedRefreshToken) {
    const decryptedRefreshToken = decryptRefreshToken(encryptedRefreshToken);
    if (decryptedRefreshToken) {
      refreshTokenMemory = decryptedRefreshToken;
      return decryptedRefreshToken;
    }
  }

  const legacyRefreshToken = storage?.getItem(REFRESH_TOKEN_KEY);
  if (!legacyRefreshToken) {
    return null;
  }

  refreshTokenMemory = legacyRefreshToken;
  const encrypted = encryptRefreshToken(legacyRefreshToken);
  if (encrypted) {
    storage?.setItem(ENCRYPTED_REFRESH_TOKEN_KEY, encrypted);
    storage?.removeItem(REFRESH_TOKEN_KEY);
  }

  return legacyRefreshToken;
};

export const setAuthTokens = (tokens: Partial<AuthResponse>) => {
  const storage = getStorage();

  if (typeof tokens.accessToken === "string") {
    accessToken = tokens.accessToken;
    storage?.setItem(ACCESS_TOKEN_KEY, tokens.accessToken);
  }

  if (typeof tokens.refreshToken === "string") {
    refreshTokenMemory = tokens.refreshToken;
    const encrypted = encryptRefreshToken(tokens.refreshToken);
    if (encrypted) {
      storage?.setItem(ENCRYPTED_REFRESH_TOKEN_KEY, encrypted);
      storage?.removeItem(REFRESH_TOKEN_KEY);
    } else {
      storage?.setItem(REFRESH_TOKEN_KEY, tokens.refreshToken);
    }
  }
};

export const clearAuthTokens = () => {
  accessToken = null;
  refreshTokenMemory = null;
  const storage = getStorage();
  storage?.removeItem(ACCESS_TOKEN_KEY);
  storage?.removeItem(REFRESH_TOKEN_KEY);
  storage?.removeItem(ENCRYPTED_REFRESH_TOKEN_KEY);
};

export const api = axios.create({
  headers: {
    "Content-Type": "application/json",
  },
});

api.interceptors.request.use((config) => {
  config.baseURL = getApiBaseUrl();
  config._requestId = createRequestId();
  config._requestStartedAt = Date.now();

  const token = getAccessToken();

  if (isHttpDebugEnabled) {
    console.info("[HTTP REQUEST]", {
      timestamp: new Date(config._requestStartedAt).toISOString(),
      requestId: config._requestId,
      method: config.method?.toUpperCase() ?? "GET",
      url: `${config.baseURL ?? ""}${config.url ?? ""}`,
      payload: summarizePayload(config.params ?? config.data),
    });
  }

  if (!token) {
    return config;
  }

  if (!config.headers) {
    config.headers = new AxiosHeaders();
  }

  config.headers.set("Authorization", `Bearer ${token}`);

  return config;
});

export const refreshAccessToken = async (): Promise<boolean> => {
  const refreshToken = getRefreshToken();

  if (!refreshToken) {
    clearAuthTokens();
    return false;
  }

  try {
    const response = await axios.post<ApiResponse<AuthResponse>>(
      `${getApiBaseUrl()}/api/auth/refresh`,
      { refreshToken },
      {
        headers: {
          "Content-Type": "application/json",
        },
      },
    );

    const tokens = response.data.data;

    setAuthTokens({
      accessToken: tokens.accessToken,
      refreshToken: tokens.refreshToken,
    });

    return true;
  } catch {
    clearAuthTokens();
    return false;
  }
};

export const logoutSession = async (): Promise<void> => {
  const refreshToken = getRefreshToken();

  try {
    if (refreshToken) {
      await api.post("/api/auth/logout", { refreshToken });
      return;
    }

    await api.post("/api/auth/logout");
  } finally {
    clearAuthTokens();
  }
};

api.interceptors.response.use(
  (response) => {
    const duration = response.config._requestStartedAt
      ? Date.now() - response.config._requestStartedAt
      : undefined;

    if (isHttpDebugEnabled) {
      const path = sanitizePath(response.config.url);
      addHttpDebugEntry({
        requestId: response.config._requestId ?? createRequestId(),
        method: response.config.method?.toUpperCase() ?? "GET",
        path,
        status: response.status,
        durationMs: duration ?? 0,
        timestamp: new Date().toISOString(),
      });

      console.info("[HTTP RESPONSE]", {
        requestId: response.config._requestId,
        status: response.status,
        durationMs: duration,
        responseSize: estimateResponseSize(response.data),
      });
    }

    return response;
  },
  async (error: AxiosError) => {
    const originalRequest = error.config;
    const status = error.response?.status;
    const duration = originalRequest?._requestStartedAt ? Date.now() - originalRequest._requestStartedAt : undefined;
    const shouldRetry = Boolean(originalRequest && status === 401 && !originalRequest._retry);

    const isCriticalStatus = typeof status === "number" && status >= 400 && status < 600;
    const isNetworkFailure = !status;

    if (isCriticalStatus || isNetworkFailure) {
      logCriticalErrorEvent({
        requestId: originalRequest?._requestId,
        method: originalRequest?.method,
        path: originalRequest?.url,
        status: status ?? NETWORK_FAILURE_STATUS,
        duration,
      });
    }

    if (isHttpDebugEnabled) {
      addHttpDebugEntry({
        requestId: originalRequest?._requestId ?? createRequestId(),
        method: originalRequest?.method?.toUpperCase() ?? "GET",
        path: sanitizePath(originalRequest?.url),
        status: status ?? NETWORK_FAILURE_STATUS,
        durationMs: duration ?? 0,
        timestamp: new Date().toISOString(),
      });

      console.error("[HTTP ERROR]", {
        requestId: originalRequest?._requestId,
        status,
        code: error.code,
        message: error.message,
        retryStatus: shouldRetry ? "scheduled" : "not-scheduled",
      });
    }

    if (!originalRequest || status !== 401 || originalRequest._retry) {
      handleGlobalHttpError(error, {
        retry: originalRequest ? () => api(originalRequest) : undefined,
      });
      return Promise.reject(error);
    }

    const isRefreshRequest = originalRequest.url?.includes("/api/auth/refresh");
    if (isRefreshRequest) {
      return Promise.reject(error);
    }

    originalRequest._retry = true;

    if (!refreshPromise) {
      refreshPromise = refreshAccessToken().finally(() => {
        refreshPromise = null;
      });
    }

    const refreshed = await refreshPromise;
    if (!refreshed) {
      handleGlobalHttpError(error);
      return Promise.reject(error);
    }

    const token = getAccessToken();
    if (token) {
      if (!originalRequest.headers) {
        originalRequest.headers = new AxiosHeaders();
      }
      originalRequest.headers.set("Authorization", `Bearer ${token}`);
    }

    return api(originalRequest);
  },
);
