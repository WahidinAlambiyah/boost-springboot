import { create } from "zustand";

import {
  api,
  clearAuthTokens,
  refreshAccessToken,
  setAuthTokens,
} from "@/lib/api";

export type AuthStatus = "loading" | "authenticated" | "unauthenticated";

interface UserProfile {
  id: string;
  username: string;
  email: string;
  isActive: boolean;
  roles: string[];
  permissions: string[];
  createdAt: string;
}

interface ApiResponse<T> {
  status: number;
  message: string;
  data: T;
}

interface AuthStore {
  status: AuthStatus;
  user: UserProfile | null;
  authorities: string[];
  hydrateSession: (payload: UserProfile) => void;
  clearSession: () => void;
  setTokens: (accessToken: string, refreshToken: string) => void;
  initializeSession: () => Promise<boolean>;
}

export const useAuthStore = create<AuthStore>((set) => ({
  status: "loading",
  user: null,
  authorities: [],

  hydrateSession: (payload) =>
    set({
      status: "authenticated",
      user: payload,
      authorities: payload.permissions ?? [],
    }),

  clearSession: () => {
    clearAuthTokens();
    set({
      status: "unauthenticated",
      user: null,
      authorities: [],
    });
  },

  setTokens: (accessToken, refreshToken) => {
    setAuthTokens({ accessToken, refreshToken });
  },

  initializeSession: async () => {
    set({ status: "loading" });

    const refreshed = await refreshAccessToken();
    if (!refreshed) {
      clearAuthTokens();
      set({
        status: "unauthenticated",
        user: null,
        authorities: [],
      });
      return false;
    }

    try {
      const profileResponse = await api.get<ApiResponse<UserProfile>>("/api/users/me");
      const profile = profileResponse.data.data;

      set({
        status: "authenticated",
        user: profile,
        authorities: profile.permissions ?? [],
      });

      return true;
    } catch {
      clearAuthTokens();
      set({
        status: "unauthenticated",
        user: null,
        authorities: [],
      });
      return false;
    }
  },
}));
