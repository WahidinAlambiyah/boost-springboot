import { create } from "zustand";

import {
  clearAuthTokens,
  setAuthTokens,
} from "@/lib/api";
import { AuthResponse, UserProfileResponse } from "@/types/api";

export type AuthStatus = "loading" | "authenticated" | "unauthenticated";

interface AuthStore {
  status: AuthStatus;
  token: string | null;
  user: UserProfileResponse | null;
  authorities: string[];
  setStatus: (status: AuthStatus) => void;
  setUser: (payload: UserProfileResponse) => void;
  setTokens: (accessToken: string, refreshToken: string) => void;
  hydrateSession: (payload: UserProfileResponse, tokens?: Partial<AuthResponse>) => void;
  clearSession: () => void;
}

export const useAuthStore = create<AuthStore>((set) => ({
  status: "loading",
  token: null,
  user: null,
  authorities: [],

  setStatus: (status) => set({ status }),

  setUser: (payload) =>
    set({
      status: "authenticated",
      user: payload,
      authorities: payload.permissions ?? [],
    }),

  setTokens: (accessToken, refreshToken) => {
    setAuthTokens({ accessToken, refreshToken });
    set({ token: accessToken });
  },

  hydrateSession: (payload, tokens) => {
    if (tokens?.accessToken || tokens?.refreshToken) {
      setAuthTokens(tokens);
    }

    set({
      status: "authenticated",
      token: tokens?.accessToken ?? null,
      user: payload,
      authorities: payload.permissions ?? [],
    });
  },

  clearSession: () => {
    clearAuthTokens();
    set({
      status: "unauthenticated",
      token: null,
      user: null,
      authorities: [],
    });
  },
}));
