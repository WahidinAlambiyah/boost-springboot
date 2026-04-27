import { can, canAll, canAny } from "@/lib/rbac";

describe("rbac helpers", () => {
  const authorities = ["CLASS_READ", "ENROLLMENT_WRITE", "admin:access"];

  it("returns true when a permission exists", () => {
    expect(can(authorities, "CLASS_READ")).toBe(true);
  });

  it("returns false when a permission does not exist", () => {
    expect(can(authorities, "BILLING_READ")).toBe(false);
  });

  it("returns true when at least one permission exists", () => {
    expect(canAny(authorities, ["BILLING_READ", "CLASS_READ"])).toBe(true);
  });

  it("returns false when none of the permissions exist", () => {
    expect(canAny(authorities, ["BILLING_READ", "ATTENDANCE_MARK"])).toBe(false);
  });

  it("returns true when all permissions exist", () => {
    expect(canAll(authorities, ["CLASS_READ", "admin:access"])).toBe(true);
  });

  it("returns false when one of required permissions is missing", () => {
    expect(canAll(authorities, ["CLASS_READ", "BILLING_READ"])).toBe(false);
  });
});
