import { useAuthStore } from "@/store/auth";
import { clearAuthTokens, setAuthTokens } from "@/lib/api";

vi.mock("@/lib/api", () => ({
  clearAuthTokens: vi.fn(),
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
});
