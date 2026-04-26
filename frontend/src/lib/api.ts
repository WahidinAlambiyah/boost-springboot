import axios, {
  AxiosError,
  AxiosHeaders,
  InternalAxiosRequestConfig,
} from "axios";

interface ApiResponse<T> {
  status: number;
  message: string;
  data: T;
}

interface AuthTokens {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
}

const ACCESS_TOKEN_KEY = "accessToken";
const REFRESH_TOKEN_KEY = "refreshToken";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "";

let accessToken: string | null = null;
let refreshPromise: Promise<boolean> | null = null;

const getStorage = () => {
  if (typeof window === "undefined") {
    return null;
  }

  return window.localStorage;
};

const readTokenFromStorage = (key: string) => getStorage()?.getItem(key) ?? null;

export const getAccessToken = () => {
  if (accessToken) {
    return accessToken;
  }

  accessToken = readTokenFromStorage(ACCESS_TOKEN_KEY);
  return accessToken;
};

export const getRefreshToken = () => readTokenFromStorage(REFRESH_TOKEN_KEY);

export const setAuthTokens = (tokens: Partial<AuthTokens>) => {
  const storage = getStorage();

  if (typeof tokens.accessToken === "string") {
    accessToken = tokens.accessToken;
    storage?.setItem(ACCESS_TOKEN_KEY, tokens.accessToken);
  }

  if (typeof tokens.refreshToken === "string") {
    storage?.setItem(REFRESH_TOKEN_KEY, tokens.refreshToken);
  }
};

export const clearAuthTokens = () => {
  accessToken = null;
  const storage = getStorage();
  storage?.removeItem(ACCESS_TOKEN_KEY);
  storage?.removeItem(REFRESH_TOKEN_KEY);
};

export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

api.interceptors.request.use((config) => {
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
    const response = await axios.post<ApiResponse<AuthTokens>>(
      `${API_BASE_URL}/api/auth/refresh`,
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

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as
      | (InternalAxiosRequestConfig & { _retry?: boolean })
      | undefined;

    if (!originalRequest || error.response?.status !== 401 || originalRequest._retry) {
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
