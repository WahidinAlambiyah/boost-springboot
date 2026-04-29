import { render, screen } from "@testing-library/react";

import Sidebar from "./sidebar";
import { useAuthStore } from "@/store/auth";
import { MenuItemResponse } from "@/types/api";

vi.mock("next/navigation", () => ({
  usePathname: () => "/dashboard",
}));

describe("Sidebar", () => {
  beforeEach(() => {
    useAuthStore.setState({
      status: "authenticated",
      token: "token",
      user: null,
      authorities: [],
      menu: [],
    });
  });

  it("renders sidebar based on menu payload", () => {
    const payload: MenuItemResponse[] = [
      { id: "1", label: "Dashboard", path: "/dashboard", visible: true, children: [] },
      { id: "2", label: "Billing", path: "/billing", visible: true, children: [] },
      { id: "3", label: "Admin", path: "/admin", visible: false, children: [] },
    ];

    useAuthStore.setState({ menu: payload });
    render(<Sidebar />);

    expect(screen.getByText("Dashboard")).toBeInTheDocument();
    expect(screen.getByText("Billing")).toBeInTheDocument();
    expect(screen.queryByText("Admin")).not.toBeInTheDocument();
  });

  it("updates rendered menu immediately when payload changes", () => {
    useAuthStore.setState({
      menu: [{ id: "1", label: "Dashboard", path: "/dashboard", visible: true, children: [] }],
    });

    const { rerender } = render(<Sidebar />);
    expect(screen.getByText("Dashboard")).toBeInTheDocument();
    expect(screen.queryByText("Billing")).not.toBeInTheDocument();

    useAuthStore.setState({
      menu: [{ id: "2", label: "Billing", path: "/billing", visible: true, children: [] }],
    });
    rerender(<Sidebar />);

    expect(screen.queryByText("Dashboard")).not.toBeInTheDocument();
    expect(screen.getByText("Billing")).toBeInTheDocument();
  });
});
