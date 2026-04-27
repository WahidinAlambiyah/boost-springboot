import { bootstrapSession } from "@/features/auth/bootstrapSession";
import { authService } from "@/features/auth/auth.service";
import { useAuthStore } from "@/store/auth";

vi.mock("@/features/auth/auth.service", () => ({
  authService: {
    refresh: vi.fn(),
    me: vi.fn(),
  },
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
      token: null,
    });
  });

  it("hydrates authenticated session when refresh and profile fetch succeed", async () => {
    vi.mocked(authService.refresh).mockResolvedValue(true);
    vi.mocked(authService.me).mockResolvedValue(sampleProfile);

    const result = await bootstrapSession();

    expect(result).toBe(true);
    expect(useAuthStore.getState().status).toBe("authenticated");
    expect(useAuthStore.getState().authorities).toEqual(["CLASS_READ"]);
  });

  it("clears session when refresh fails", async () => {
    vi.mocked(authService.refresh).mockResolvedValue(false);

    const result = await bootstrapSession();

    expect(result).toBe(false);
    expect(useAuthStore.getState().status).toBe("unauthenticated");
    expect(useAuthStore.getState().user).toBeNull();
  });

  it("clears session when profile fetch fails", async () => {
    vi.mocked(authService.refresh).mockResolvedValue(true);
    vi.mocked(authService.me).mockRejectedValue(new Error("boom"));

    const result = await bootstrapSession();

    expect(result).toBe(false);
    expect(useAuthStore.getState().status).toBe("unauthenticated");
    expect(useAuthStore.getState().user).toBeNull();
  });
});
