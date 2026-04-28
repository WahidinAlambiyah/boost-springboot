import axios, {
  AxiosError,
  AxiosHeaders,
} from "axios";

import { getApiBaseUrl } from "@/lib/env";
import { handleGlobalHttpError } from "@/lib/http-error-events";
import { ApiResponse, AuthResponse } from "@/types/api";

declare module "axios" {
  interface InternalAxiosRequestConfig {
    _retry?: boolean;
  }
}

const ACCESS_TOKEN_KEY = "accessToken";
const REFRESH_TOKEN_KEY = "refreshToken";
const ENCRYPTED_REFRESH_TOKEN_KEY = "refreshToken.enc.v1";
const REFRESH_TOKEN_CIPHER_SEED = "boost-refresh-token-seed-v1";

let accessToken: string | null = null;
let refreshTokenMemory: string | null = null;
let refreshPromise: Promise<boolean> | null = null;

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

  const token = getAccessToken();

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
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config;

    if (!originalRequest || error.response?.status !== 401 || originalRequest._retry) {
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
