import { can, canAll } from "@/lib/rbac";

export const ADMIN_PERMISSION_BUNDLE = ["USER_WRITE", "ROLE_WRITE", "PERMISSION_WRITE"] as const;

export const hasAdminAccess = (authorities: string[]): boolean => {
  return can(authorities, "ROLE_ADMIN") || canAll(authorities, [...ADMIN_PERMISSION_BUNDLE]);
};
