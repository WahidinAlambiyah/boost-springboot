export const ENABLE_DEV_TOOLS =
  process.env.NEXT_PUBLIC_ENABLE_DEV_TOOLS === "true" || process.env.NODE_ENV !== "production";
