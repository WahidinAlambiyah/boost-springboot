export interface RuntimeErrorPayload {
  source: "window.error" | "unhandledrejection" | "react.error-boundary";
  message: string;
  stack?: string;
  url: string;
  userAgent: string;
  timestamp: string;
}

export interface CriticalHttpErrorPayload {
  requestId: string;
  method: string;
  path: string;
  status: number;
  duration: number;
  userId?: string;
  timestamp: string;
}

type ObservabilityPayload = RuntimeErrorPayload | CriticalHttpErrorPayload;

const getRuntimeLogEndpoint = (): string | null => {
  const value = process.env.NEXT_PUBLIC_RUNTIME_LOG_ENDPOINT;
  if (!value) {
    return null;
  }

  const normalizedValue = value.trim();
  return normalizedValue.length > 0 ? normalizedValue : null;
};

const sendPayload = (payload: ObservabilityPayload) => {
  const endpoint = getRuntimeLogEndpoint();

  if (!endpoint) {
    return;
  }

  const body = JSON.stringify(payload);

  if (navigator.sendBeacon) {
    const blob = new Blob([body], { type: "application/json" });
    navigator.sendBeacon(endpoint, blob);
    return;
  }

  void fetch(endpoint, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body,
    keepalive: true,
  });
};

export const logCriticalHttpError = (payload: Omit<CriticalHttpErrorPayload, "timestamp">) => {
  sendPayload({
    ...payload,
    timestamp: new Date().toISOString(),
  });
};

export const logClientRuntimeError = (errorPayload: Omit<RuntimeErrorPayload, "url" | "userAgent" | "timestamp">) => {
  if (typeof window === "undefined") {
    return;
  }

  const payload: RuntimeErrorPayload = {
    ...errorPayload,
    url: window.location.href,
    userAgent: navigator.userAgent,
    timestamp: new Date().toISOString(),
  };

  sendPayload(payload);
};

export const installClientRuntimeObservers = () => {
  if (typeof window === "undefined") {
    return;
  }

  window.addEventListener("error", (event) => {
    logClientRuntimeError({
      source: "window.error",
      message: event.message || "Unknown runtime error",
      stack: event.error?.stack,
    });
  });

  window.addEventListener("unhandledrejection", (event) => {
    const reason = event.reason;
    const message = reason instanceof Error ? reason.message : String(reason);
    const stack = reason instanceof Error ? reason.stack : undefined;

    logClientRuntimeError({
      source: "unhandledrejection",
      message,
      stack,
    });
  });
};
