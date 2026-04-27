import { render, waitFor } from "@testing-library/react";

import AuthBootstrap from "@/app/components/auth-bootstrap";
import { bootstrapSession } from "@/features/auth/bootstrapSession";

const replace = vi.fn();
const usePathnameMock = vi.fn();

const mockStore = {
  status: "loading" as "loading" | "authenticated" | "unauthenticated",
  authorities: [] as string[],
};

vi.mock("next/navigation", () => ({
  useRouter: () => ({
    replace,
  }),
  usePathname: () => usePathnameMock(),
}));

vi.mock("@/features/auth/bootstrapSession", () => ({
  bootstrapSession: vi.fn<() => Promise<boolean>>(),
}));

vi.mock("@/store/auth", () => ({
  useAuthStore: (selector: (state: typeof mockStore) => unknown) => selector(mockStore),
}));

describe("AuthBootstrap route guard", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockStore.status = "loading";
    mockStore.authorities = [];
    vi.mocked(bootstrapSession).mockResolvedValue(true);
  });

  it("redirects unauthenticated users to /login for private routes", async () => {
    usePathnameMock.mockReturnValue("/dashboard");
    vi.mocked(bootstrapSession).mockImplementation(async () => {
      mockStore.status = "unauthenticated";
      return false;
    });

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
    vi.mocked(bootstrapSession).mockImplementation(async () => {
      mockStore.status = "authenticated";
      return true;
    });

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
    vi.mocked(bootstrapSession).mockImplementation(async () => {
      mockStore.status = "authenticated";
      return true;
    });
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
