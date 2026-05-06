export const DEV_CRUD_STATUSES = ["DRAFT", "ACTIVE", "ARCHIVED"] as const;
export const DEV_CRUD_PRIORITIES = ["LOW", "MEDIUM", "HIGH"] as const;

export type DevCrudStatus = (typeof DEV_CRUD_STATUSES)[number];
export type DevCrudPriority = (typeof DEV_CRUD_PRIORITIES)[number];

export interface DevCrudItem {
  id: string;
  title: string;
  owner: string;
  status: DevCrudStatus;
  priority: DevCrudPriority;
  dueDate: string;
  createdAt: string;
}

// TODO: Replace USER_READ fallback with DEV_TOOLS_READ once the backend authority is available.
export const DEV_TOOLS_READ_PERMISSIONS = ["USER_READ"] as const;

// TODO: Replace ROLE_READ fallback with DEV_TOOLS_WRITE once the backend authority is available.
export const DEV_TOOLS_WRITE_PERMISSIONS = ["ROLE_READ"] as const;
