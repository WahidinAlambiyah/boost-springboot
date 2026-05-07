import { render, screen } from "@testing-library/react";

import { DataTable, type DataTableColumn } from "./data-table";
import { FormField } from "./form-field";
import { StatusBadge } from "./status-badge";

describe("admin foundation components", () => {
  it("renders StatusBadge status text with the mapped success variant", () => {
    render(<StatusBadge status="ACTIVE" />);

    expect(screen.getByText("ACTIVE")).toBeInTheDocument();
    expect(screen.getByText("ACTIVE")).toHaveClass("bg-emerald-100", "text-emerald-700");
  });

  it("renders DataTable empty state", () => {
    type Row = { name: string };
    const columns: DataTableColumn<Row>[] = [
      {
        key: "name",
        header: "Nama",
        render: (row) => row.name,
      },
    ];

    render(
      <DataTable
        columns={columns}
        data={[]}
        emptyTitle="Belum ada data"
        emptyDescription="Tambahkan data untuk melihat daftar."
      />,
    );

    expect(screen.getByText("Belum ada data")).toBeInTheDocument();
    expect(screen.getByText("Tambahkan data untuk melihat daftar.")).toBeInTheDocument();
    expect(screen.queryByRole("cell", { name: /andi/i })).not.toBeInTheDocument();
  });

  it("renders DataTable loading, error, and action column states", () => {
    type Row = { id: string; name: string };
    const columns: DataTableColumn<Row>[] = [
      {
        key: "name",
        header: "Nama",
        render: (row) => row.name,
      },
    ];

    const { rerender } = render(
      <DataTable columns={columns} data={[]} isLoading loadingRows={2} ariaLabel="Tabel demo" />,
    );

    expect(screen.getByRole("table", { name: "Tabel demo" })).toBeInTheDocument();
    expect(screen.getAllByRole("row")).toHaveLength(3);

    rerender(<DataTable columns={columns} data={[]} error="Gagal memuat data" />);
    expect(screen.getByRole("alert")).toHaveTextContent("Gagal memuat data");

    rerender(
      <DataTable
        columns={columns}
        data={[{ id: "row-1", name: "Andi" }]}
        getRowKey={(row) => row.id}
        actionColumn={{
          render: (row) => <button type="button">Edit {row.name}</button>,
        }}
      />,
    );

    expect(screen.getByRole("columnheader", { name: "Aksi" })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Edit Andi" })).toBeInTheDocument();
  });

  it("renders FormField label, input association, and error message", () => {
    render(
      <FormField id="name" label="Nama" error="Nama wajib diisi" required>
        <input id="name" />
      </FormField>,
    );

    expect(screen.getByLabelText(/Nama/)).toBeInTheDocument();
    expect(screen.getByText("*", { selector: "span" })).toHaveAttribute("aria-hidden", "true");
    expect(screen.getByRole("alert")).toHaveTextContent("Nama wajib diisi");
  });
});
