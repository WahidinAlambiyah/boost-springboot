export const can = (authorities: string[], permission: string): boolean => {
  return authorities.includes(permission);
};

export const canAny = (authorities: string[], permissions: string[]): boolean => {
  return permissions.some((permission) => can(authorities, permission));
};

export const canAll = (authorities: string[], permissions: string[]): boolean => {
  return permissions.every((permission) => can(authorities, permission));
};
