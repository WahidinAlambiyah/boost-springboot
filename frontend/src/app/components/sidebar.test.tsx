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
    vi.restoreAllMocks();
  });

  it("renders sidebar based on backend menu and local fallback", () => {
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
    expect(screen.getByText("Academies")).toBeInTheDocument();
  });

  it("prioritizes backend menu and does not duplicate fallback routes", () => {
    const payload: MenuItemResponse[] = [
      { id: "1", label: "Academies", path: "/academies", visible: true, children: [] },
    ];

    useAuthStore.setState({ menu: payload });
    render(<Sidebar />);

    expect(screen.getAllByText("Academies")).toHaveLength(1);
    expect(screen.getByText("Students")).toBeInTheDocument();
  });

  it("does not add dev tools from the local fallback menu", () => {
    useAuthStore.setState({ menu: [] });
    render(<Sidebar />);

    expect(screen.queryByRole("link", { name: /dev tools/i })).not.toBeInTheDocument();
    expect(screen.queryByRole("link", { name: /developer tools/i })).not.toBeInTheDocument();
    expect(screen.queryByRole("link", { name: "/dev" })).not.toBeInTheDocument();
  });

  it("renders dev tools naturally when the backend menu provides it", () => {
    const payload: MenuItemResponse[] = [
      { id: "backend-dev", label: "Dev Tools", path: "/dev", visible: true, children: [] },
    ];

    useAuthStore.setState({ menu: payload });
    render(<Sidebar />);

    expect(screen.getByRole("link", { name: "Dev Tools" })).toHaveAttribute("href", "/dev");
  });

  it("renders compact labels when collapsed", () => {
    const payload: MenuItemResponse[] = [
      { id: "1", label: "Dashboard", path: "/dashboard", visible: true, children: [] },
      { id: "2", label: "User Management", path: "/users", visible: true, children: [] },
    ];

    useAuthStore.setState({ menu: payload });
    render(<Sidebar collapsed />);

    expect(screen.getByLabelText("Sidebar navigation")).toHaveClass("w-20");
    expect(screen.getByRole("link", { name: "Dashboard" })).toHaveAttribute("title", "Dashboard");
    expect(screen.getByText("DA")).toBeInTheDocument();
    expect(screen.getByText("UM")).toBeInTheDocument();
  });

  it("writes observability log when fallback is active", () => {
    const infoSpy = vi.spyOn(console, "info").mockImplementation(() => undefined);

    useAuthStore.setState({ menu: [] });
    render(<Sidebar />);

    expect(infoSpy).toHaveBeenCalledWith(
      "[sidebar] Local menu fallback active.",
      expect.objectContaining({ fallbackCount: 5 }),
    );
  });
});
