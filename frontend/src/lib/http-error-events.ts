"use client";

import { AxiosError } from "axios";

import { useAuthStore } from "@/store/auth";

export interface ApiErrorToastPayload {
  id: string;
  message: string;
  retryLabel?: string;
  onRetry?: () => void | Promise<void>;
}

const API_ERROR_TOAST_EVENT = "app:api-error-toast";
const ACCESS_TOKEN_KEY = "accessToken";
const REFRESH_TOKEN_KEY = "refreshToken";
const ENCRYPTED_REFRESH_TOKEN_KEY = "refreshToken.enc.v1";

const clearPersistedAuthTokens = () => {
  if (typeof window === "undefined") {
    return;
  }

  window.localStorage.removeItem(ACCESS_TOKEN_KEY);
  window.localStorage.removeItem(REFRESH_TOKEN_KEY);
  window.localStorage.removeItem(ENCRYPTED_REFRESH_TOKEN_KEY);
};

const emitToast = (payload: Omit<ApiErrorToastPayload, "id">) => {
  if (typeof window === "undefined") {
    return;
  }

  window.dispatchEvent(
    new CustomEvent<ApiErrorToastPayload>(API_ERROR_TOAST_EVENT, {
      detail: {
        id: `${Date.now()}-${Math.random().toString(36).slice(2)}`,
        retryLabel: "Coba lagi",
        ...payload,
      },
    }),
  );
};

export const subscribeApiErrorToast = (listener: (payload: ApiErrorToastPayload) => void) => {
  if (typeof window === "undefined") {
    return () => undefined;
  }

  const handler = (event: Event) => {
    const customEvent = event as CustomEvent<ApiErrorToastPayload>;
    if (customEvent.detail) {
      listener(customEvent.detail);
    }
  };

  window.addEventListener(API_ERROR_TOAST_EVENT, handler as EventListener);

  return () => {
    window.removeEventListener(API_ERROR_TOAST_EVENT, handler as EventListener);
  };
};

const redirectTo = (path: string) => {
  if (typeof window === "undefined") {
    return;
  }

  if (window.location.pathname === path) {
    return;
  }

  window.location.replace(path);
};

export const handleGlobalHttpError = (
  error: unknown,
  options?: {
    retry?: () => void | Promise<void>;
  },
): boolean => {
  if (!(error instanceof AxiosError)) {
    return false;
  }

  const status = error.response?.status;

  if (status === 401) {
    clearPersistedAuthTokens();
    useAuthStore.getState().clearSession();
    redirectTo("/login");
    return true;
  }

  if (status === 403) {
    redirectTo("/forbidden");
    return true;
  }

  if ((status ?? 0) >= 500) {
    const fallbackMessage = error.response?.data?.message;
    emitToast({
      message: typeof fallbackMessage === "string" ? fallbackMessage : "Terjadi gangguan server. Silakan coba lagi.",
      onRetry: options?.retry,
    });
    return true;
  }

  return false;
};
