import { ADMIN_PERMISSION_BUNDLE, hasAdminAccess } from "@/lib/admin-guard";

describe("admin guard", () => {
  it("returns true for ROLE_ADMIN", () => {
    expect(hasAdminAccess(["ROLE_ADMIN"])).toBe(true);
  });

  it("returns true for full admin permission bundle", () => {
    expect(hasAdminAccess([...ADMIN_PERMISSION_BUNDLE])).toBe(true);
  });

  it("returns false when only partial admin permission bundle is present", () => {
    expect(hasAdminAccess(["USER_WRITE", "ROLE_WRITE"])).toBe(false);
  });
});
