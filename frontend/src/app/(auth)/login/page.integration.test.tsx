import { fireEvent, render, screen, waitFor } from "@testing-library/react";

import LoginPage from "@/app/(auth)/login/page";
import { authService } from "@/features/auth/auth.service";
import { useAuthStore } from "@/store/auth";

const replaceMock = vi.fn();

vi.mock("next/navigation", () => ({
  useRouter: () => ({
    replace: replaceMock,
  }),
}));

vi.mock("@/features/auth/auth.service", () => ({
  authService: {
    login: vi.fn(),
    me: vi.fn(),
  },
}));

const profile = {
  id: "1",
  username: "demo",
  email: "demo@example.com",
  isActive: true,
  roles: ["USER"],
  permissions: ["CLASS_READ"],
  createdAt: "2026-01-01T00:00:00.000Z",
};

describe("login page integration", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    useAuthStore.setState({
      status: "unauthenticated",
      token: null,
      user: null,
      authorities: [],
    });
  });

  it("submits login and redirects to dashboard", async () => {
    vi.mocked(authService.login).mockResolvedValue({
      accessToken: "access-token",
      refreshToken: "refresh-token",
      tokenType: "Bearer",
    });
    vi.mocked(authService.me).mockResolvedValue(profile);

    render(<LoginPage />);

    fireEvent.change(screen.getByLabelText("Username"), { target: { value: "demo" } });
    fireEvent.change(screen.getByLabelText("Password"), { target: { value: "secret" } });
    fireEvent.click(screen.getByRole("button", { name: "Login" }));

    await waitFor(() => {
      expect(authService.login).toHaveBeenCalledWith({ username: "demo", password: "secret" });
    });
    expect(authService.me).toHaveBeenCalledTimes(1);
    expect(useAuthStore.getState().status).toBe("authenticated");
    expect(useAuthStore.getState().user?.username).toBe("demo");
    expect(replaceMock).toHaveBeenCalledWith("/dashboard");
  });
});
