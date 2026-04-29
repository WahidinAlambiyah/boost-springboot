import { bootstrapSession } from "@/features/auth/bootstrapSession";
import { authService } from "@/features/auth/auth.service";
import { useAuthStore } from "@/store/auth";

vi.mock("@/features/auth/auth.service", () => ({
  authService: {
    refresh: vi.fn(),
    me: vi.fn(),
    menu: vi.fn(),
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

const sampleMenu = [
  { id: "dashboard", label: "Dashboard", path: "/dashboard", visible: true, children: [] },
];

describe("bootstrapSession", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    useAuthStore.setState({
      status: "unauthenticated",
      user: null,
      authorities: [],
      token: null,
      menu: [],
    });
  });

  it("hydrates authenticated session when refresh and profile fetch succeed", async () => {
    vi.mocked(authService.refresh).mockResolvedValue(true);
    vi.mocked(authService.me).mockResolvedValue(sampleProfile);
    vi.mocked(authService.menu).mockResolvedValue(sampleMenu);

    const result = await bootstrapSession();

    expect(result).toBe(true);
    expect(useAuthStore.getState().status).toBe("authenticated");
    expect(useAuthStore.getState().authorities).toEqual(["CLASS_READ"]);
    expect(useAuthStore.getState().menu).toEqual(sampleMenu);
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
    vi.mocked(authService.menu).mockResolvedValue(sampleMenu);

    const result = await bootstrapSession();

    expect(result).toBe(false);
    expect(useAuthStore.getState().status).toBe("unauthenticated");
    expect(useAuthStore.getState().user).toBeNull();
  });

  it("clears session when menu fetch fails", async () => {
    vi.mocked(authService.refresh).mockResolvedValue(true);
    vi.mocked(authService.me).mockResolvedValue(sampleProfile);
    vi.mocked(authService.menu).mockRejectedValue(new Error("menu boom"));

    const result = await bootstrapSession();

    expect(result).toBe(false);
    expect(useAuthStore.getState().status).toBe("unauthenticated");
    expect(useAuthStore.getState().menu).toEqual([]);
  });
});
