# Admin UI Components

Dokumen ini merangkum komponen UI admin reusable di `frontend/src/app/components/`. Komponen-komponen ini sengaja memakai React + Tailwind className tanpa UI library tambahan, dengan visual konsisten: permukaan `bg-white`, teks/border `zinc`, border ringan, dan `shadow-sm` untuk container utama.

## Daftar Komponen

| Komponen | Fungsi Utama |
| --- | --- |
| `PageHeader` | Header halaman dengan title, description, dan slot actions. |
| `SectionCard` | Container section berbentuk card untuk mengelompokkan konten, form, filter, atau table. |
| `StatusBadge` | Badge status dengan variant otomatis dari domain status atau variant eksplisit. |
| `SearchInput` | Input pencarian standar untuk filter list/table. |
| `PaginationBar` | Kontrol pagination dan pilihan ukuran halaman. |
| `ConfirmActionButton` | Tombol action yang meminta konfirmasi browser sebelum menjalankan callback. |
| `FormField` | Wrapper label, required marker, children field, dan error text. |
| `StatCard` | Kartu ringkasan angka/statistik dengan description dan trend slot. |
| `ErrorMessage` | Alert ringkas untuk pesan error umum atau inline. |
| `DataTable` | Tabel generic berbasis column config dengan loading, error, empty, horizontal scroll, row key, dan optional action column. |

## Style Convention

- Pakai palet netral `zinc` untuk teks, border, hover, focus ring, dan background lembut seperti `bg-zinc-50`.
- Pakai `bg-white` untuk permukaan utama card/table/form agar kontras dengan background halaman.
- Pakai `border border-zinc-200` untuk container dan `border-zinc-300` untuk input/button.
- Pakai `shadow-sm` hanya untuk card/table/container yang butuh elevasi halus; hindari shadow besar untuk UI admin.
- Jangan menambahkan UI library baru. Komponen harus tetap berbasis React + Tailwind/className agar bundle ringan dan pattern mudah direview.

## `DataTable`

Gunakan `DataTable<T>` dengan tipe row generic agar konfigurasi column tetap type-safe. `DataTable` sudah menyediakan:

- **Loading state** lewat `isLoading` dan `loadingRows`; header tetap tampil supaya layout tidak melompat.
- **Error state** lewat `error` dan optional `errorTitle`.
- **Empty state** lewat `emptyTitle` dan `emptyDescription`.
- **Horizontal scroll** lewat wrapper `overflow-x-auto` dan table `min-w-max`, sehingga column banyak tetap aman di viewport kecil.
- **Action column** lewat `actionColumn`, atau tetap bisa dibuat manual sebagai column terakhir jika butuh kontrol penuh.

```tsx
import { ConfirmActionButton } from "@/app/components/confirm-action-button";
import { DataTable, type DataTableColumn } from "@/app/components/data-table";
import { StatusBadge } from "@/app/components/status-badge";

type InvoiceRow = {
  id: string;
  code: string;
  memberName: string;
  status: "DRAFT" | "PAID" | "CANCELLED";
  total: number;
};

const columns: DataTableColumn<InvoiceRow>[] = [
  {
    key: "code",
    header: "Kode",
    render: (row) => <span className="font-medium text-zinc-900">{row.code}</span>,
  },
  {
    key: "memberName",
    header: "Member",
    render: (row) => row.memberName,
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
];

export function InvoiceTableExample({ rows, isLoading, error }: {
  rows: InvoiceRow[];
  isLoading: boolean;
  error?: string;
}) {
  return (
    <DataTable<InvoiceRow>
      ariaLabel="Daftar invoice"
      columns={columns}
      data={rows}
      error={error}
      errorTitle="Gagal memuat invoice"
      emptyTitle="Belum ada invoice"
      emptyDescription="Invoice yang dibuat akan muncul di tabel ini."
      getRowKey={(row) => row.id}
      isLoading={isLoading}
      actionColumn={{
        render: (row) => (
          <div className="flex justify-end gap-2">
            <button
              type="button"
              className="rounded-md border border-zinc-300 bg-white px-3 py-1.5 text-sm text-zinc-700 shadow-sm hover:bg-zinc-50"
              onClick={() => console.log("detail", row.id)}
            >
              Detail
            </button>
            <ConfirmActionButton
              className="px-3 py-1.5"
              confirmMessage={`Batalkan ${row.code}?`}
              label="Batalkan"
              onConfirm={() => console.log("cancel", row.id)}
              variant="danger"
            />
          </div>
        ),
      }}
    />
  );
}
```

## `FormField`

`FormField` bertugas sebagai wrapper label dan error state. Komponen ini tidak membatasi jenis field, sehingga bisa membungkus `input`, `select`, `textarea`, atau custom control lain.

```tsx
import { FormField } from "@/app/components/form-field";

export function AdminFormExample() {
  return (
    <div className="grid gap-4 md:grid-cols-2">
      <FormField htmlFor="member-name" label="Nama Member" required>
        <input
          id="member-name"
          name="memberName"
          className="w-full rounded-md border border-zinc-300 bg-white px-3 py-2 text-sm text-zinc-900 shadow-sm outline-none transition focus:border-zinc-500 focus:ring-2 focus:ring-zinc-200"
          placeholder="Contoh: Andi Pratama"
        />
      </FormField>

      <FormField htmlFor="member-status" label="Status" error="Pilih salah satu status.">
        <select
          id="member-status"
          name="memberStatus"
          aria-invalid="true"
          className="w-full rounded-md border border-zinc-300 bg-white px-3 py-2 text-sm text-zinc-900 shadow-sm outline-none transition focus:border-zinc-500 focus:ring-2 focus:ring-zinc-200"
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

## `StatusBadge`

`StatusBadge` dapat menerima status domain seperti `ACTIVE`, `PAID`, dan `CANCELLED`. Variant default akan dipilih dari mapping internal komponen. Pakai `variant` jika status baru butuh warna tertentu.

```tsx
import { StatusBadge } from "@/app/components/status-badge";

export function StatusBadgeExample() {
  return (
    <div className="flex flex-wrap gap-2">
      <StatusBadge status="ACTIVE" />
      <StatusBadge status="PAID" />
      <StatusBadge status="CANCELLED" />
      <StatusBadge status="Custom Info" variant="info" />
    </div>
  );
}
```

## Layout List Page Admin

Kombinasikan `PageHeader`, `SectionCard`, `SearchInput`, `PaginationBar`, dan `DataTable` untuk halaman list standar.

```tsx
<PageHeader
  title="Members"
  description="Kelola data member dan status membership."
  actions={<button className="rounded-md border border-zinc-300 bg-white px-3 py-2 text-sm shadow-sm">Tambah</button>}
/>

<SectionCard title="Daftar Member" description="Gunakan filter untuk mempersempit data.">
  <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
    <SearchInput value={keyword} onChange={setKeyword} placeholder="Cari member..." className="w-full sm:max-w-xs" />
  </div>

  <DataTable columns={columns} data={rows} getRowKey={(row) => row.id} isLoading={isLoading} />

  <PaginationBar
    page={page}
    size={size}
    totalElements={totalElements}
    onPageChange={setPage}
    onSizeChange={setSize}
  />
</SectionCard>
```
