import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import type { ReactNode } from "react";

import StudentsPage from "@/app/(protected)/students/page";
import { studentService } from "@/features/students/student.service";
import { useAuthStore } from "@/store/auth";

const invalidateQueriesMock = vi.fn();
const useQueryMock = vi.fn();
const useMutationMock = vi.fn();
let queryState: { isLoading: boolean; isError: boolean; data: unknown } = {
  isLoading: false,
  isError: false,
  data: [],
};

vi.mock("@tanstack/react-query", async () => {
  const actual = await vi.importActual<typeof import("@tanstack/react-query")>("@tanstack/react-query");
  return {
    ...actual,
    useQuery: (args: unknown) => useQueryMock(args),
    useMutation: (args: unknown) => useMutationMock(args),
    useQueryClient: () => ({ invalidateQueries: invalidateQueriesMock }),
  };
});

vi.mock("@/features/students/student.service", () => ({
  studentService: {
    list: vi.fn(),
    create: vi.fn(),
    update: vi.fn(),
    isEndpointEnabled: true,
  },
}));

vi.mock("next/navigation", () => ({
  useRouter: () => ({ replace: vi.fn() }),
}));
vi.mock("@/app/components/app-shell", () => ({
  default: ({ children }: { children: ReactNode }) => <div>{children}</div>,
}));

describe("students page integration", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    useAuthStore.setState({
      status: "authenticated",
      token: "token",
      user: null,
      authorities: ["STUDENT_READ", "STUDENT_WRITE"],
    });
    useMutationMock.mockImplementation(({ onSuccess }) => ({
      mutate: () => {
        if (onSuccess) {
          void onSuccess();
        }
      },
      error: null,
      isPending: false,
    }));
    useQueryMock.mockImplementation(() => queryState);
  });

  it("renders loading, error, and empty states", () => {
    queryState = { isLoading: true, isError: false, data: undefined };
    const { container, rerender } = render(<StudentsPage />);
    expect(container.querySelector(".animate-pulse")).toBeInTheDocument();

    queryState = { isLoading: false, isError: true, data: undefined };
    rerender(<StudentsPage />);
    expect(screen.getByText("Gagal memuat data students.")).toBeInTheDocument();

    queryState = { isLoading: false, isError: false, data: [] };
    rerender(<StudentsPage />);
    expect(screen.getByText("Belum ada student")).toBeInTheDocument();
  });

  it("hides action column when user has no write permission", () => {
    useAuthStore.setState({ authorities: ["STUDENT_READ"] });
    queryState = {
      isLoading: false,
      isError: false,
      data: [{ id: "s1", fullName: "Demo Student", studentNo: "001", nickname: null, status: "ACTIVE" }],
    };

    render(<StudentsPage />);

    expect(screen.queryByText("Action")).not.toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "Edit" })).not.toBeInTheDocument();
  });

  it("invalidates student queries after create mutation succeeds", async () => {
    queryState = { isLoading: false, isError: false, data: [] };
    vi.mocked(studentService.create).mockResolvedValue({ id: "new" } as never);

    render(<StudentsPage />);
    fireEvent.change(screen.getByPlaceholderText("studentNo"), { target: { value: "S-01" } });
    fireEvent.change(screen.getByPlaceholderText("fullName"), { target: { value: "Student Baru" } });
    fireEvent.change(document.querySelector('input[type="date"]') as HTMLInputElement, { target: { value: "2020-01-01" } });
    fireEvent.click(screen.getByRole("button", { name: "Tambah Student" }));

    await waitFor(() => {
      expect(invalidateQueriesMock).toHaveBeenCalledWith({ queryKey: ["students"] });
    });
  });
});
