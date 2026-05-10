export interface AdminUser {
  id: string;
  username: string;
  email: string;
  fullName?: string | null;
  active: boolean;
  roles: string[];
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface AdminRole {
  id: string;
  code: string;
  name: string;
  description?: string | null;
  active: boolean;
  permissions: string[];
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface AdminPermission {
  id: string;
  code: string;
  name: string;
  description?: string | null;
  module?: string | null;
  active: boolean;
  createdAt?: string | null;
  updatedAt?: string | null;
}

export interface AdminUserCreateRequest {
  username: string;
  email: string;
  password: string;
  fullName?: string | null;
  active: boolean;
  roleCodes: string[];
}

export interface AdminUserUpdateRequest {
  username: string;
  email: string;
  fullName?: string | null;
  active: boolean;
  roleCodes: string[];
}

export interface AdminRoleRequest {
  code: string;
  name: string;
  description?: string | null;
  active: boolean;
  permissionCodes: string[];
}

export interface AdminPermissionRequest {
  code: string;
  name: string;
  description?: string | null;
  module?: string | null;
  active: boolean;
}
