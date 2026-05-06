# Admin UI Components — Phase 5

Dokumen ini merangkum komponen UI admin Phase 5 yang tersedia untuk dipakai ulang di halaman internal/admin. Semua komponen mengikuti convention styling Tailwind yang ringan dan konsisten, tanpa menambahkan UI library baru.

## Daftar Komponen

| Komponen | Fungsi Utama |
| --- | --- |
| `PageHeader` | Header halaman dengan title, description, dan slot actions. |
| `SectionCard` | Container section berbentuk card untuk mengelompokkan konten/form/table. |
| `StatusBadge` | Badge status dengan variant otomatis atau eksplisit. |
| `SearchInput` | Input pencarian standar untuk filter list/table. |
| `PaginationBar` | Kontrol pagination dan pilihan ukuran halaman. |
| `ConfirmActionButton` | Tombol action yang meminta konfirmasi sebelum menjalankan callback. |
| `FormField` | Wrapper label, required marker, children field, dan error text. |
| `StatCard` | Kartu ringkasan angka/statistik dengan description/trend. |
| `ErrorMessage` | Alert ringkas untuk pesan error umum atau inline. |
| `DataTable` | Tabel generic berbasis konfigurasi column, state loading/error/empty, dan row key. |

## Contoh Pemakaian `DataTable`

Gunakan `DataTable<T>` dengan tipe row generic agar render column tetap type-safe. Tambahkan action column sebagai column terakhir untuk tombol seperti detail, edit, atau delete.

```tsx
import { DataTable, type DataTableColumn } from "@/app/components/data-table";
import { ConfirmActionButton } from "@/app/components/confirm-action-button";
import { StatusBadge } from "@/app/components/status-badge";

type DummyRow = {
  id: string;
  name: string;
  status: "ACTIVE" | "PAID" | "CANCELLED";
  total: number;
};

const rows: DummyRow[] = [
  { id: "row-1", name: "Membership A", status: "ACTIVE", total: 250000 },
  { id: "row-2", name: "Invoice B", status: "PAID", total: 500000 },
  { id: "row-3", name: "Booking C", status: "CANCELLED", total: 0 },
];

const columns: DataTableColumn<DummyRow>[] = [
  {
    key: "name",
    header: "Nama",
    render: (row) => <span className="font-medium text-zinc-900">{row.name}</span>,
  },
  {
    key: "status",
    header: "Status",
    render: (row) => <StatusBadge status={row.status} />,
  },
  {
    key: "total",
    header: "Total",
    render: (row) => `Rp${row.total.toLocaleString("id-ID")}`,
    className: "text-right tabular-nums",
    headerClassName: "text-right",
  },
  {
    key: "actions",
    header: "Aksi",
    render: (row) => (
      <div className="flex justify-end gap-2">
        <button
          type="button"
          className="rounded-md border border-zinc-300 px-3 py-1.5 text-sm text-zinc-700 hover:bg-zinc-50"
          onClick={() => console.log("edit", row.id)}
        >
          Edit
        </button>
        <ConfirmActionButton
          label="Hapus"
          confirmMessage={`Hapus ${row.name}?`}
          variant="danger"
          onConfirm={() => console.log("delete", row.id)}
        />
      </div>
    ),
    className: "text-right",
    headerClassName: "text-right",
  },
];

export function DummyTableExample() {
  return (
    <DataTable<DummyRow>
      columns={columns}
      data={rows}
      emptyTitle="Belum ada data"
      emptyDescription="Tambahkan data dummy untuk melihat isi tabel."
      getRowKey={(row) => row.id}
    />
  );
}
```

## Contoh Pemakaian `FormField`

`FormField` bertugas sebagai wrapper label dan error state. Komponen ini tidak membatasi jenis field, sehingga bisa membungkus `input`, `select`, atau custom control lain.

```tsx
import { FormField } from "@/app/components/form-field";

export function AdminFormExample() {
  return (
    <div className="grid gap-4 md:grid-cols-2">
      <FormField htmlFor="member-name" label="Nama Member" required>
        <input
          id="member-name"
          name="memberName"
          className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm text-zinc-900 outline-none transition focus:border-zinc-500 focus:ring-2 focus:ring-zinc-200"
          placeholder="Contoh: Andi Pratama"
        />
      </FormField>

      <FormField htmlFor="member-status" label="Status" error="Pilih salah satu status.">
        <select
          id="member-status"
          name="memberStatus"
          aria-invalid="true"
          className="w-full rounded-md border border-zinc-300 bg-white px-3 py-2 text-sm text-zinc-900 outline-none transition focus:border-zinc-500 focus:ring-2 focus:ring-zinc-200"
          defaultValue=""
        >
          <option value="" disabled>
            Pilih status
          </option>
          <option value="ACTIVE">Active</option>
          <option value="PAID">Paid</option>
          <option value="CANCELLED">Cancelled</option>
        </select>
      </FormField>
    </div>
  );
}
```

## Contoh Pemakaian `StatusBadge`

`StatusBadge` dapat menerima status domain seperti `ACTIVE`, `PAID`, dan `CANCELLED`. Variant default akan dipilih dari mapping internal komponen.

```tsx
import { StatusBadge } from "@/app/components/status-badge";

export function StatusBadgeExample() {
  return (
    <div className="flex flex-wrap gap-2">
      <StatusBadge status="ACTIVE" />
      <StatusBadge status="PAID" />
      <StatusBadge status="CANCELLED" />
    </div>
  );
}
```

## Style Convention

- Pakai palet netral `zinc` untuk teks, border, hover, focus ring, dan background lembut seperti `bg-zinc-50`.
- Pakai `bg-white` untuk permukaan utama card/table/form agar kontras dengan background halaman.
- Pakai `border border-zinc-200` atau `border-zinc-300` untuk struktur visual yang ringan dan konsisten.
- Pakai `shadow-sm` hanya untuk card/table/container yang butuh elevasi halus; hindari shadow besar untuk UI admin.
- Jangan menambahkan UI library baru. Komponen Phase 5 harus tetap berbasis React + Tailwind/className agar bundle ringan dan convention tetap mudah direview.
