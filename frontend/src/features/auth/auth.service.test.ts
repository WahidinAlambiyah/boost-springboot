import axios from "axios";

import { authService } from "@/features/auth/auth.service";
import { api, clearAuthTokens, getRefreshToken, setAuthTokens } from "@/lib/api";

vi.mock("axios");
vi.mock("@/lib/api", () => ({
  api: {
    post: vi.fn(),
    get: vi.fn(),
  },
  clearAuthTokens: vi.fn(),
  getRefreshToken: vi.fn(),
  setAuthTokens: vi.fn(),
}));

describe("auth service", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("login stores tokens from API response", async () => {
    vi.mocked(api.post).mockResolvedValue({
      data: {
        data: {
          accessToken: "access-token",
          refreshToken: "refresh-token",
          tokenType: "Bearer",
        },
      },
    });

    const result = await authService.login({ username: "demo", password: "secret" });

    expect(api.post).toHaveBeenCalledWith("/api/auth/login", { username: "demo", password: "secret" });
    expect(setAuthTokens).toHaveBeenCalledWith(result);
    expect(result.accessToken).toBe("access-token");
  });

  it("refresh clears tokens when refresh token does not exist", async () => {
    vi.mocked(getRefreshToken).mockReturnValue(null);

    const refreshed = await authService.refresh();

    expect(refreshed).toBe(false);
    expect(clearAuthTokens).toHaveBeenCalledTimes(1);
    expect(axios.post).not.toHaveBeenCalled();
  });

  it("refresh stores new tokens when refresh succeeds", async () => {
    vi.mocked(getRefreshToken).mockReturnValue("seed-refresh-token");
    vi.mocked(axios.post).mockResolvedValue({
      data: {
        data: {
          accessToken: "new-access",
          refreshToken: "new-refresh",
          tokenType: "Bearer",
        },
      },
    });

    const refreshed = await authService.refresh();

    expect(refreshed).toBe(true);
    expect(setAuthTokens).toHaveBeenCalledWith({
      accessToken: "new-access",
      refreshToken: "new-refresh",
    });
  });

  it("logout posts refresh token then clears auth state", async () => {
    vi.mocked(getRefreshToken).mockReturnValue("seed-refresh-token");
    vi.mocked(api.post).mockResolvedValue({});

    await authService.logout();

    expect(api.post).toHaveBeenCalledWith("/api/auth/logout", { refreshToken: "seed-refresh-token" });
    expect(clearAuthTokens).toHaveBeenCalledTimes(1);
  });

  it("me returns authenticated user profile", async () => {
    vi.mocked(api.get).mockResolvedValue({
      data: {
        data: {
          id: "1",
          username: "demo",
          email: "demo@example.com",
          isActive: true,
          roles: ["USER"],
          permissions: ["CLASS_READ"],
          createdAt: "2026-01-01T00:00:00.000Z",
        },
      },
    });

    const profile = await authService.me();

    expect(api.get).toHaveBeenCalledWith("/api/users/me");
    expect(profile.username).toBe("demo");
  });
});
