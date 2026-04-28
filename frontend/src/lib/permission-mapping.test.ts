import {
  SCHEDULING_GUARD_PERMISSIONS,
  SCHEDULING_SIDEBAR_PERMISSIONS,
} from "@/lib/permission-mapping";

describe("permission mapping", () => {
  it("uses SCHEDULE_* as canonical permissions", () => {
    expect(SCHEDULING_SIDEBAR_PERMISSIONS[0]).toBe("SCHEDULE_READ");
    expect(SCHEDULING_SIDEBAR_PERMISSIONS[1]).toBe("SCHEDULE_WRITE");
  });

  it("keeps SESSION_* aliases during migration window", () => {
    expect(SCHEDULING_GUARD_PERMISSIONS).toContain("SESSION_READ");
    expect(SCHEDULING_GUARD_PERMISSIONS).toContain("SESSION_WRITE");
    expect(SCHEDULING_GUARD_PERMISSIONS).toContain("SESSION_RESCHEDULE");
  });
});
