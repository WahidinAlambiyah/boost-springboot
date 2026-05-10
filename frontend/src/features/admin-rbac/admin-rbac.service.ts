import { api } from "@/lib/api";
import { ApiResponse } from "@/types/api";

import {
  AdminPermission,
  AdminPermissionRequest,
  AdminRole,
  AdminRoleRequest,
  AdminUser,
  AdminUserCreateRequest,
  AdminUserUpdateRequest,
} from "./admin-rbac.types";

export interface AdminRbacListParams {
  search?: string;
}

export const adminRbacService = {
  async listUsers(params?: AdminRbacListParams): Promise<AdminUser[]> {
    const response = await api.get<ApiResponse<AdminUser[]>>("/api/admin/rbac/users", {
      params: { search: params?.search || undefined },
    });
    return response.data.data ?? [];
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

  async listRoles(params?: AdminRbacListParams): Promise<AdminRole[]> {
    const response = await api.get<ApiResponse<AdminRole[]>>("/api/admin/rbac/roles", {
      params: { search: params?.search || undefined },
    });
    return response.data.data ?? [];
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

  async listPermissions(params?: AdminRbacListParams): Promise<AdminPermission[]> {
    const response = await api.get<ApiResponse<AdminPermission[]>>("/api/admin/rbac/permissions", {
      params: { search: params?.search || undefined },
    });
    return response.data.data ?? [];
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
