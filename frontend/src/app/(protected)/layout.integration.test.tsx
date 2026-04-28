import { render, screen, waitFor } from "@testing-library/react";

import ProtectedLayout from "@/app/(protected)/layout";
import { bootstrapSession } from "@/features/auth/bootstrapSession";
import { useAuthStore } from "@/store/auth";

const replaceMock = vi.fn();

vi.mock("next/navigation", () => ({
  useRouter: () => ({
    replace: replaceMock,
  }),
}));

vi.mock("@/features/auth/bootstrapSession", () => ({
  bootstrapSession: vi.fn(),
}));

describe("protected layout integration", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    useAuthStore.setState({
      status: "loading",
      token: null,
      user: null,
      authorities: [],
    });
  });

  it("renders protected content when bootstrap authenticates user", async () => {
    vi.mocked(bootstrapSession).mockImplementation(async () => {
      useAuthStore.setState({ status: "authenticated" });
      return true;
    });

    render(
      <ProtectedLayout>
        <h1>Protected Content</h1>
      </ProtectedLayout>,
    );

    await waitFor(() => {
      expect(screen.getByRole("heading", { name: "Protected Content" })).toBeVisible();
    });
    expect(replaceMock).not.toHaveBeenCalled();
  });

  it("redirects to login when bootstrap leaves user unauthenticated", async () => {
    vi.mocked(bootstrapSession).mockImplementation(async () => {
      useAuthStore.setState({ status: "unauthenticated" });
      return false;
    });

    render(
      <ProtectedLayout>
        <h1>Protected Content</h1>
      </ProtectedLayout>,
    );

    await waitFor(() => {
      expect(replaceMock).toHaveBeenCalledWith("/login");
    });
  });
});
