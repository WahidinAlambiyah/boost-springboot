import { render, waitFor } from "@testing-library/react";

import AuthBootstrap from "@/app/components/auth-bootstrap";

const replace = vi.fn();
const usePathnameMock = vi.fn();

const mockStore = {
  status: "loading" as "loading" | "authenticated" | "unauthenticated",
  authorities: [] as string[],
  initializeSession: vi.fn<() => Promise<boolean>>(),
};

vi.mock("next/navigation", () => ({
  useRouter: () => ({
    replace,
  }),
  usePathname: () => usePathnameMock(),
}));

vi.mock("@/store/auth", () => ({
  useAuthStore: (selector: (state: typeof mockStore) => unknown) => selector(mockStore),
}));

describe("AuthBootstrap route guard", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockStore.status = "loading";
    mockStore.authorities = [];
    mockStore.initializeSession.mockReset();
  });

  it("redirects unauthenticated users to /login for private routes", async () => {
    usePathnameMock.mockReturnValue("/dashboard");
    mockStore.initializeSession.mockResolvedValue(false);

    render(
      <AuthBootstrap>
        <div>Protected</div>
      </AuthBootstrap>,
    );

    await waitFor(() => {
      expect(replace).toHaveBeenCalledWith("/login");
    });
  });

  it("redirects authenticated users away from /login", async () => {
    usePathnameMock.mockReturnValue("/login");
    mockStore.initializeSession.mockResolvedValue(true);

    render(
      <AuthBootstrap>
        <div>Login</div>
      </AuthBootstrap>,
    );

    await waitFor(() => {
      expect(replace).toHaveBeenCalledWith("/dashboard");
    });
  });

  it("redirects unauthorized admin access to /forbidden", async () => {
    usePathnameMock.mockReturnValue("/admin");
    mockStore.initializeSession.mockResolvedValue(true);
    mockStore.status = "authenticated";
    mockStore.authorities = ["CLASS_READ"];

    render(
      <AuthBootstrap>
        <div>Admin</div>
      </AuthBootstrap>,
    );

    await waitFor(() => {
      expect(replace).toHaveBeenCalledWith("/forbidden");
    });
  });
});
