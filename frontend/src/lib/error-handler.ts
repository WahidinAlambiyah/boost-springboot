"use client";

import { AxiosError } from "axios";
import { useRouter } from "next/navigation";

import { ErrorResponse } from "@/types/api";

export const parseValidationErrors = (error: unknown): Record<string, string> => {
  if (!(error instanceof AxiosError<ErrorResponse>)) {
    return {};
  }

  const body = error.response?.data;
  return body?.data?.errors ?? {};
};

export const parseErrorMessage = (error: unknown): string => {
  if (!(error instanceof AxiosError<ErrorResponse>)) {
    return "Terjadi kesalahan tidak terduga.";
  }

  const body = error.response?.data;
  return body?.message ?? "Terjadi kesalahan tidak terduga.";
};

export const useStandardErrorRedirect = () => {
  const router = useRouter();

  return (error: unknown) => {
    if (!(error instanceof AxiosError)) {
      return;
    }

    const status = error.response?.status;
    if (status === 401) {
      router.replace("/login");
      return;
    }

    if (status === 403) {
      router.replace("/forbidden");
    }
  };
};
