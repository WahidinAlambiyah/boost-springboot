import { render, screen } from "@testing-library/react";

import DevToolsPage from "./page";

vi.mock("@/app/components/app-shell", () => ({
  default: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
}));

vi.mock("@/app/components/require-permission", () => ({
  default: ({ children }: { children: React.ReactNode }) => <>{children}</>,
}));

vi.mock("@/app/components/page-header", () => ({
  PageHeader: ({ title, description }: { title: string; description: string }) => (
    <header>
      <h1>{title}</h1>
      <p>{description}</p>
    </header>
  ),
}));

describe("DevToolsPage", () => {
  it("lists the dev tool entry points from the dev index", () => {
    render(<DevToolsPage />);

    expect(screen.getByRole("heading", { name: "Developer Tools" })).toBeInTheDocument();
    expect(screen.getByRole("link", { name: /Components/ })).toHaveAttribute("href", "/dev/components");
    expect(screen.getByRole("link", { name: /CRUD Demo/ })).toHaveAttribute("href", "/dev/crud-demo");
    expect(screen.getByRole("link", { name: /Table Demo/ })).toHaveAttribute("href", "/dev/table-demo");
    expect(screen.getByRole("link", { name: /Form Demo/ })).toHaveAttribute("href", "/dev/form-demo");
  });
});
