import { render, screen } from "@testing-library/react";
import type { ReactNode } from "react";

import DashboardPage from "./page";
import { useAuthStore } from "@/store/auth";

const useQueryMock = vi.fn();
const invalidateQueriesMock = vi.fn();

const dashboardQueryState = {
  isLoading: false,
  isError: false,
  data: {
    academy: { id: "academy-1", name: "Academy Demo", code: "ACD" },
    period: { from: "2026-05-01", to: "2026-05-31" },
    summaryCards: {
      activeStudents: 10,
      activeCoaches: 3,
      todaySessions: 2,
      attendanceRate: 80,
      pendingAssessments: 4,
      upcomingEvents: 1,
      unpaidInvoices: 2,
      currentMonthPayrollStatus: "CALCULATED",
    },
    todaySessions: [],
    studentsNeedAttention: [],
    upcomingEvents: [],
    recentAssessments: [],
  },
  refetch: vi.fn(),
};

const academyQueryState = {
  isLoading: false,
  isError: false,
  data: [{ id: "academy-1", name: "Academy Demo", code: "ACD" }],
};

vi.mock("@tanstack/react-query", async () => {
  const actual = await vi.importActual<typeof import("@tanstack/react-query")>("@tanstack/react-query");
  return {
    ...actual,
    useQuery: (args: { queryKey: readonly unknown[] }) => useQueryMock(args),
    useQueryClient: () => ({ invalidateQueries: invalidateQueriesMock }),
  };
});

vi.mock("next/navigation", () => ({
  useRouter: () => ({ replace: vi.fn() }),
}));

vi.mock("@/app/components/app-shell", () => ({
  default: ({ children }: { children: ReactNode }) => <div>{children}</div>,
}));

vi.mock("@/app/components/require-permission", () => ({
  default: ({ children }: { children: ReactNode }) => <>{children}</>,
}));

describe("DashboardPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    useAuthStore.setState({
      status: "authenticated",
      token: "token",
      user: null,
      authorities: ["DASHBOARD_OWNER_READ"],
      menu: [],
    });

    useQueryMock.mockImplementation((args: { queryKey: readonly unknown[] }) => {
      const [entity] = args.queryKey;
      if (entity === "academies") {
        return academyQueryState;
      }
      if (entity === "dashboard") {
        return dashboardQueryState;
      }
      return { isLoading: false, isError: false, data: [] };
    });
  });

  it("renders business dashboard header and keeps quick links section", () => {
    render(<DashboardPage />);

    expect(screen.getByRole("heading", { name: "Dashboard Academy" })).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "Quick Links" })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Refresh" })).toBeInTheDocument();
  });

  it("exposes dev tools quick link when user has permission", () => {
    useAuthStore.setState({ authorities: ["DASHBOARD_OWNER_READ", "DEV_TOOLS_READ"] });

    render(<DashboardPage />);

    expect(screen.getByRole("link", { name: /Developer Tools/ })).toHaveAttribute("href", "/dev");
  });

  it("hides dev tools quick link without matching permission", () => {
    render(<DashboardPage />);

    expect(screen.queryByRole("link", { name: /Developer Tools/ })).not.toBeInTheDocument();
  });
});
