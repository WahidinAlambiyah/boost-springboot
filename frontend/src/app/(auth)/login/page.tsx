"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import axios from "axios";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";

import { authService } from "@/features/auth/auth.service";
import { useAuthStore } from "@/store/auth";

const loginSchema = z.object({
  username: z.string().min(1, "Username wajib diisi"),
  password: z.string().min(1, "Password wajib diisi"),
});

type LoginFormValues = z.infer<typeof loginSchema>;

export default function LoginPage() {
  const router = useRouter();
  const hydrateSession = useAuthStore((state) => state.hydrateSession);
  const [submitError, setSubmitError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      username: "",
      password: "",
    },
  });

  const onSubmit = async (values: LoginFormValues) => {
    setSubmitError(null);

    try {
      const tokens = await authService.login(values);
      const profile = await authService.me();
      hydrateSession(profile, tokens);
      router.replace("/dashboard");
    } catch (error) {
      if (axios.isAxiosError(error)) {
        if (error.response?.status === 401) {
          setSubmitError("credential salah");
          return;
        }

        if ((error.response?.status ?? 0) >= 500) {
          setSubmitError("Terjadi gangguan server (5xx). Silakan coba lagi.");
          return;
        }
      }

      setSubmitError("Terjadi kesalahan. Silakan coba kembali.");
    }
  };

  return (
    <main className="flex min-h-screen flex-col items-center justify-center bg-zinc-50 px-6">
      <div className="w-full max-w-sm rounded-lg border border-zinc-200 bg-white p-6 shadow-sm">
        <h1 className="text-xl font-semibold text-zinc-900">Login</h1>
        <p className="mt-2 text-sm text-zinc-600">Masukkan username dan password untuk melanjutkan.</p>

        <form className="mt-4 space-y-4" onSubmit={handleSubmit(onSubmit)}>
          <div className="space-y-1">
            <label className="text-sm font-medium text-zinc-700" htmlFor="username">
              Username
            </label>
            <input
              id="username"
              type="text"
              autoComplete="username"
              className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm outline-none ring-0 focus:border-zinc-500"
              {...register("username")}
            />
            {errors.username ? (
              <p className="text-xs text-red-600" role="alert">
                {errors.username.message}
              </p>
            ) : null}
          </div>

          <div className="space-y-1">
            <label className="text-sm font-medium text-zinc-700" htmlFor="password">
              Password
            </label>
            <input
              id="password"
              type="password"
              autoComplete="current-password"
              className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm outline-none ring-0 focus:border-zinc-500"
              {...register("password")}
            />
            {errors.password ? (
              <p className="text-xs text-red-600" role="alert">
                {errors.password.message}
              </p>
            ) : null}
          </div>

          {submitError ? (
            <p className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700" role="alert">
              {submitError}
            </p>
          ) : null}

          <button
            type="submit"
            disabled={isSubmitting}
            className="inline-flex w-full items-center justify-center rounded-md bg-zinc-900 px-3 py-2 text-sm font-medium text-white disabled:cursor-not-allowed disabled:opacity-60"
          >
            {isSubmitting ? "Loading..." : "Login"}
          </button>
        </form>
      </div>
    </main>
  );
}
