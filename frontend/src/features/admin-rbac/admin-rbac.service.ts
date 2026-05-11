import { api } from "@/lib/api";
import { ApiResponse } from "@/types/api";

import {
  AdminDatatableParams,
  AdminPermission,
  AdminPermissionRequest,
  AdminRole,
  AdminRoleRequest,
  AdminSliceResponse,
  AdminUser,
  AdminUserCreateRequest,
  AdminUserUpdateRequest,
} from "./admin-rbac.types";

const buildParams = (params?: AdminDatatableParams) => ({
  search: params?.search || undefined,
  page: params?.page ?? 0,
  size: params?.size ?? 10,
  sort: params?.sort || undefined,
  direction: params?.direction || undefined,
  active: typeof params?.active === "boolean" ? params.active : undefined,
  roleCode: params?.roleCode || undefined,
  permissionCode: params?.permissionCode || undefined,
  module: params?.module || undefined,
});

const normalizeSlice = <T>(slice: AdminSliceResponse<T> | undefined): AdminSliceResponse<T> => ({
  items: slice?.items ?? slice?.content ?? [],
  content: slice?.content,
  page: slice?.page ?? 0,
  size: slice?.size ?? 10,
  hasNext: Boolean(slice?.hasNext),
  hasPrevious: Boolean(slice?.hasPrevious ?? (slice?.page ?? 0) > 0),
  sort: slice?.sort ?? slice?.sortBy ?? "createdAt",
  direction: slice?.direction ?? slice?.sortDirection ?? "asc",
  sortBy: slice?.sortBy,
  sortDirection: slice?.sortDirection,
});

export const adminRbacService = {
  async listUsers(params?: AdminDatatableParams): Promise<AdminSliceResponse<AdminUser>> {
    const response = await api.get<ApiResponse<AdminSliceResponse<AdminUser>>>("/api/admin/rbac/users", {
      params: buildParams(params),
    });
    return normalizeSlice(response.data.data);
  },

  async getUser(id: string): Promise<AdminUser> {
    const response = await api.get<ApiResponse<AdminUser>>(`/api/admin/rbac/users/${id}`);
    return response.data.data;
  },

  async createUser(payload: AdminUserCreateRequest): Promise<AdminUser> {
    const response = await api.post<ApiResponse<AdminUser>>("/api/admin/rbac/users", payload);
    return response.data.data;
  },

  async updateUser(id: string, payload: AdminUserUpdateRequest): Promise<AdminUser> {
    const response = await api.put<ApiResponse<AdminUser>>(`/api/admin/rbac/users/${id}`, payload);
    return response.data.data;
  },

  async updateUserPassword(id: string, password: string): Promise<AdminUser> {
    const response = await api.patch<ApiResponse<AdminUser>>(`/api/admin/rbac/users/${id}/password`, { password });
    return response.data.data;
  },

  async updateUserActive(id: string, active: boolean): Promise<AdminUser> {
    const response = await api.patch<ApiResponse<AdminUser>>(`/api/admin/rbac/users/${id}/active`, { active });
    return response.data.data;
  },

  async deleteUser(id: string): Promise<void> {
    await api.delete<ApiResponse<void>>(`/api/admin/rbac/users/${id}`);
  },

  async listRoles(params?: AdminDatatableParams): Promise<AdminSliceResponse<AdminRole>> {
    const response = await api.get<ApiResponse<AdminSliceResponse<AdminRole>>>("/api/admin/rbac/roles", {
      params: buildParams(params),
    });
    return normalizeSlice(response.data.data);
  },

  async getRole(id: string): Promise<AdminRole> {
    const response = await api.get<ApiResponse<AdminRole>>(`/api/admin/rbac/roles/${id}`);
    return response.data.data;
  },

  async createRole(payload: AdminRoleRequest): Promise<AdminRole> {
    const response = await api.post<ApiResponse<AdminRole>>("/api/admin/rbac/roles", payload);
    return response.data.data;
  },

  async updateRole(id: string, payload: AdminRoleRequest): Promise<AdminRole> {
    const response = await api.put<ApiResponse<AdminRole>>(`/api/admin/rbac/roles/${id}`, payload);
    return response.data.data;
  },

  async deleteRole(id: string): Promise<void> {
    await api.delete<ApiResponse<void>>(`/api/admin/rbac/roles/${id}`);
  },

  async listPermissions(params?: AdminDatatableParams): Promise<AdminSliceResponse<AdminPermission>> {
    const response = await api.get<ApiResponse<AdminSliceResponse<AdminPermission>>>("/api/admin/rbac/permissions", {
      params: buildParams(params),
    });
    return normalizeSlice(response.data.data);
  },

  async getPermission(id: string): Promise<AdminPermission> {
    const response = await api.get<ApiResponse<AdminPermission>>(`/api/admin/rbac/permissions/${id}`);
    return response.data.data;
  },

  async createPermission(payload: AdminPermissionRequest): Promise<AdminPermission> {
    const response = await api.post<ApiResponse<AdminPermission>>("/api/admin/rbac/permissions", payload);
    return response.data.data;
  },

  async updatePermission(id: string, payload: AdminPermissionRequest): Promise<AdminPermission> {
    const response = await api.put<ApiResponse<AdminPermission>>(`/api/admin/rbac/permissions/${id}`, payload);
    return response.data.data;
  },

  async deletePermission(id: string): Promise<void> {
    await api.delete<ApiResponse<void>>(`/api/admin/rbac/permissions/${id}`);
  },
};
