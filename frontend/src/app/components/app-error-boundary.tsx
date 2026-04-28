"use client";

import { Component, ErrorInfo, ReactNode } from "react";
import { logClientRuntimeError } from "@/lib/client-observability";

interface AppErrorBoundaryProps {
  children: ReactNode;
}

interface AppErrorBoundaryState {
  hasError: boolean;
}

export default class AppErrorBoundary extends Component<AppErrorBoundaryProps, AppErrorBoundaryState> {
  public state: AppErrorBoundaryState = {
    hasError: false,
  };

  public static getDerivedStateFromError(): AppErrorBoundaryState {
    return { hasError: true };
  }

  public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error("Unhandled app error", error, errorInfo);
    logClientRuntimeError({
      source: "react.error-boundary",
      message: error.message,
      stack: `${error.stack ?? ""}
${errorInfo.componentStack ?? ""}`.trim(),
    });
  }

  private handleRetry = () => {
    this.setState({ hasError: false });
    if (typeof window !== "undefined") {
      window.location.reload();
    }
  };

  public render() {
    if (!this.state.hasError) {
      return this.props.children;
    }

    return (
      <main className="flex min-h-screen flex-col items-center justify-center bg-zinc-50 px-6 text-center">
        <p className="text-6xl font-bold text-zinc-900">500</p>
        <h1 className="mt-3 text-2xl font-semibold text-zinc-900">Terjadi kendala di aplikasi</h1>
        <p className="mt-2 max-w-md text-zinc-600">Silakan muat ulang halaman atau coba beberapa saat lagi.</p>
        <button
          type="button"
          onClick={this.handleRetry}
          className="mt-4 inline-flex rounded-md bg-zinc-900 px-3 py-2 text-sm font-medium text-white hover:bg-zinc-700"
        >
          Coba lagi
        </button>
      </main>
    );
  }
}
