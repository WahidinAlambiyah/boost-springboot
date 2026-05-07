// TODO: remove ROLE_READ/USER_READ fallback when DEV_TOOLS_READ is managed by backend.
export const DEV_TOOLS_READ_PERMISSIONS = ["DEV_TOOLS_READ", "ROLE_READ", "USER_READ"] as const;
