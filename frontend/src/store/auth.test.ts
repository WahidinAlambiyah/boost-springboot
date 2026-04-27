import { useAuthStore } from "@/store/auth";
import { api, clearAuthTokens, refreshAccessToken, setAuthTokens } from "@/lib/api";

vi.mock("@/lib/api", () => ({
  api: {
    get: vi.fn(),
  },
  clearAuthTokens: vi.fn(),
  refreshAccessToken: vi.fn(),
  setAuthTokens: vi.fn(),
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

describe("auth store", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    useAuthStore.setState({
      status: "loading",
      user: null,
      authorities: [],
    });
  });

  it("hydrates session and populates authorities", () => {
    useAuthStore.getState().hydrateSession(sampleProfile);

    const state = useAuthStore.getState();
    expect(state.status).toBe("authenticated");
    expect(state.user?.username).toBe("demo");
    expect(state.authorities).toEqual(["CLASS_READ"]);
  });

  it("clears session and tokens", () => {
    useAuthStore.getState().hydrateSession(sampleProfile);

    useAuthStore.getState().clearSession();

    const state = useAuthStore.getState();
    expect(clearAuthTokens).toHaveBeenCalledTimes(1);
    expect(state.status).toBe("unauthenticated");
    expect(state.user).toBeNull();
    expect(state.authorities).toEqual([]);
  });

  it("stores tokens", () => {
    useAuthStore.getState().setTokens("access", "refresh");

    expect(setAuthTokens).toHaveBeenCalledWith({
      accessToken: "access",
      refreshToken: "refresh",
    });
  });

  it("initializes session when refresh and profile fetch succeed", async () => {
    vi.mocked(refreshAccessToken).mockResolvedValue(true);
    vi.mocked(api.get).mockResolvedValue({
      data: {
        data: sampleProfile,
      },
    });

    const result = await useAuthStore.getState().initializeSession();

    expect(result).toBe(true);
    expect(useAuthStore.getState().status).toBe("authenticated");
    expect(useAuthStore.getState().authorities).toEqual(["CLASS_READ"]);
  });

  it("falls back to unauthenticated when refresh fails", async () => {
    vi.mocked(refreshAccessToken).mockResolvedValue(false);

    const result = await useAuthStore.getState().initializeSession();

    expect(result).toBe(false);
    expect(clearAuthTokens).toHaveBeenCalled();
    expect(useAuthStore.getState().status).toBe("unauthenticated");
  });

  it("falls back to unauthenticated when profile fetch fails", async () => {
    vi.mocked(refreshAccessToken).mockResolvedValue(true);
    vi.mocked(api.get).mockRejectedValue(new Error("boom"));

    const result = await useAuthStore.getState().initializeSession();

    expect(result).toBe(false);
    expect(clearAuthTokens).toHaveBeenCalled();
    expect(useAuthStore.getState().status).toBe("unauthenticated");
  });
});
