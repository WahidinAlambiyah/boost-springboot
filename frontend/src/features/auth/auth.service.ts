import axios from "axios";

import {
  api,
  clearAuthTokens,
  getRefreshToken,
  setAuthTokens,
} from "@/lib/api";
import { getApiBaseUrl } from "@/lib/env";
import {
  ApiResponse,
  AuthResponse,
  MenuItemResponse,
  UserProfileResponse,
} from "@/types/api";

interface LoginPayload {
  username: string;
  password: string;
}

export const authService = {
  async login(payload: LoginPayload): Promise<AuthResponse> {
    const response = await api.post<ApiResponse<AuthResponse>>("/api/auth/login", payload);
    const tokens = response.data.data;
    setAuthTokens(tokens);
    return tokens;
  },

  async refresh(): Promise<boolean> {
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
      setAuthTokens({ accessToken: tokens.accessToken, refreshToken: tokens.refreshToken });
      return true;
    } catch {
      clearAuthTokens();
      return false;
    }
  },

  async logout(): Promise<void> {
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
  },

  async me(): Promise<UserProfileResponse> {
    const response = await api.get<ApiResponse<UserProfileResponse>>("/api/users/me");
    return response.data.data;
  },

  async menu(): Promise<MenuItemResponse[]> {
    const response = await api.get<ApiResponse<MenuItemResponse[]>>("/api/me/menu");
    return response.data.data;
  },
};
