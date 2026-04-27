import { bootstrapSession } from "@/features/auth/bootstrapSession";
import { api, refreshAccessToken } from "@/lib/api";
import { useAuthStore } from "@/store/auth";

vi.mock("@/lib/api", () => ({
  api: {
    get: vi.fn(),
  },
  refreshAccessToken: vi.fn(),
  clearAuthTokens: vi.fn(),
}));

const sampleProfile = {
  id: "1",
  username: "demo",
  email: "demo@example.com",
  isActive: true,
  roles: ["USER"],
  permissions: ["CLASS_READ"],
  createdAt: "2026-01-01T00:00:00.000Z",
};

describe("bootstrapSession", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    useAuthStore.setState({
      status: "unauthenticated",
      user: null,
      authorities: [],
    });
  });

  it("hydrates authenticated session when refresh and profile fetch succeed", async () => {
    vi.mocked(refreshAccessToken).mockResolvedValue(true);
    vi.mocked(api.get).mockResolvedValue({
      data: {
        data: sampleProfile,
      },
    });

    const result = await bootstrapSession();

    expect(result).toBe(true);
    expect(useAuthStore.getState().status).toBe("authenticated");
    expect(useAuthStore.getState().authorities).toEqual(["CLASS_READ"]);
  });

  it("clears session when refresh fails", async () => {
    vi.mocked(refreshAccessToken).mockResolvedValue(false);

    const result = await bootstrapSession();

    expect(result).toBe(false);
    expect(useAuthStore.getState().status).toBe("unauthenticated");
    expect(useAuthStore.getState().user).toBeNull();
  });

  it("clears session when profile fetch fails", async () => {
    vi.mocked(refreshAccessToken).mockResolvedValue(true);
    vi.mocked(api.get).mockRejectedValue(new Error("boom"));

    const result = await bootstrapSession();

    expect(result).toBe(false);
    expect(useAuthStore.getState().status).toBe("unauthenticated");
    expect(useAuthStore.getState().user).toBeNull();
  });
});
