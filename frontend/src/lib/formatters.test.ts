import { describe, expect, it } from "vitest";

import { formatCurrencyIDR, formatDate, formatDateTime, formatStatusLabel } from "./formatters";

describe("formatDate", () => {
  it("formats valid date string with id-ID locale", () => {
    expect(formatDate("2026-04-30T10:15:00Z")).toBe("30/04/2026");
  });

  it("returns fallback when invalid", () => {
    expect(formatDate(null)).toBe("-");
    expect(formatDate(undefined)).toBe("-");
    expect(formatDate("   ")).toBe("-");
    expect(formatDate("not-a-date")).toBe("-");
  });
});

describe("formatDateTime", () => {
  it("formats valid datetime string with id-ID locale", () => {
    expect(formatDateTime("2026-04-30T10:15:00Z")).toMatch(/^30\/04\/2026,?\s/);
  });

  it("returns fallback when invalid", () => {
    expect(formatDateTime(null)).toBe("-");
    expect(formatDateTime(undefined)).toBe("-");
    expect(formatDateTime("invalid")).toBe("-");
  });
});

describe("formatCurrencyIDR", () => {
  it("formats numeric and string values", () => {
    expect(formatCurrencyIDR(1500000)).toBe("Rp 1.500.000");
    expect(formatCurrencyIDR("250000")).toBe("Rp 250.000");
  });

  it("returns fallback when invalid", () => {
    expect(formatCurrencyIDR(null)).toBe("-");
    expect(formatCurrencyIDR(undefined)).toBe("-");
    expect(formatCurrencyIDR("abc")).toBe("-");
  });
});

describe("formatStatusLabel", () => {
  it("normalizes status strings into title case labels", () => {
    expect(formatStatusLabel("PENDING_APPROVAL")).toBe("Pending Approval");
    expect(formatStatusLabel("paid-status")).toBe("Paid Status");
    expect(formatStatusLabel("  in progress  ")).toBe("In Progress");
  });

  it("returns fallback when invalid", () => {
    expect(formatStatusLabel(null)).toBe("-");
    expect(formatStatusLabel(undefined)).toBe("-");
    expect(formatStatusLabel("   ")).toBe("-");
  });
});
