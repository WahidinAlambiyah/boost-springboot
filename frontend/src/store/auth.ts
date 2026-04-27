import { create } from "zustand";

import {
  clearAuthTokens,
  setAuthTokens,
} from "@/lib/api";
import { UserProfileResponse } from "@/types/api";

export type AuthStatus = "loading" | "authenticated" | "unauthenticated";

interface AuthStore {
  status: AuthStatus;
  user: UserProfileResponse | null;
  authorities: string[];
  hydrateSession: (payload: UserProfileResponse) => void;
  clearSession: () => void;
  setTokens: (accessToken: string, refreshToken: string) => void;
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
}));
