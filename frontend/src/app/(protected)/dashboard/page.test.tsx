import { render, screen } from "@testing-library/react";

import DashboardPage from "./page";
import { useAuthStore } from "@/store/auth";

vi.mock("@/app/components/app-shell", () => ({
  default: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
}));

describe("DashboardPage", () => {
  beforeEach(() => {
    useAuthStore.setState({
      status: "authenticated",
      token: "token",
      user: null,
      authorities: [],
      menu: [],
    });
  });

  it("exposes dev tools through the dashboard quick links when permitted", () => {
    useAuthStore.setState({ authorities: ["DEV_TOOLS_READ"] });

    render(<DashboardPage />);

    expect(screen.getByRole("heading", { name: "Quick Links" })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: "Developer Tools" })).toHaveAttribute("href", "/dev");
  });

  it("hides the dev tools quick link without a matching permission", () => {
    render(<DashboardPage />);

    expect(screen.queryByRole("link", { name: "Developer Tools" })).not.toBeInTheDocument();
  });
});
