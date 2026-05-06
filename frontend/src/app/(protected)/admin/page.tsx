"use client";

import { DataTable, type DataTableColumn } from "@/app/components/data-table";
import { ErrorMessage } from "@/app/components/error-message";
import { FormField } from "@/app/components/form-field";
import { PageHeader } from "@/app/components/page-header";
import { StatusBadge, type StatusBadgeTone } from "@/app/components/status-badge";
import AppShell from "@/app/components/app-shell";
import RequirePermission from "@/app/components/require-permission";
import { ADMIN_PERMISSION_BUNDLE } from "@/lib/admin-guard";

interface ComponentChecklistRow {
  component: string;
  statusLabel: string;
  statusTone: StatusBadgeTone;
  note: string;
}

const COMPONENT_CHECKLIST: ComponentChecklistRow[] = [
  { component: "page-header", statusLabel: "Ready", statusTone: "success", note: "Header dan action slot bisa langsung dipakai." },
  { component: "status-badge", statusLabel: "Ready", statusTone: "success", note: "Tone standar untuk state modul berikutnya." },
  { component: "error-message", statusLabel: "Ready", statusTone: "success", note: "Menangani pesan validasi/error inline." },
  { component: "data-table", statusLabel: "Ready", statusTone: "success", note: "Generic columns + getRowKey untuk daftar data." },
  { component: "form-field", statusLabel: "Ready", statusTone: "success", note: "Input dasar dengan hint/error bawaan." },
];

const checklistColumns: DataTableColumn<ComponentChecklistRow>[] = [
  {
    key: "component",
    header: "Komponen",
    render: (row) => <span className="font-medium text-zinc-900">{row.component}</span>,
  },
  {
    key: "status",
    header: "Status",
    render: (row) => <StatusBadge status={row.statusLabel} variant={row.statusTone === "neutral" ? "default" : row.statusTone} />,
  },
  {
    key: "note",
    header: "Catatan Reuse",
    render: (row) => row.note,
  },
];

export default function AdminPage() {
  return (
    <RequirePermission permissions={[...ADMIN_PERMISSION_BUNDLE]} mode="any">
      <AppShell>
      <PageHeader
        title="UI Component Verification"
        description="Halaman utilitas internal untuk validasi compile-time dan pola reuse komponen inti tanpa implementasi CRUD penuh."
        actions={<StatusBadge status="Internal Utility" variant="info" />}
      />

      <section className="space-y-3 rounded-lg border border-zinc-200 bg-white p-5 shadow-sm">
        <h2 className="text-sm font-semibold uppercase tracking-wide text-zinc-500">Form Sample</h2>
        <div className="grid gap-3 md:grid-cols-2">
          <FormField htmlFor="sample-name" label="Nama">
            <input
              id="sample-name"
              className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm text-zinc-900 outline-none transition focus:border-zinc-500 focus:ring-2 focus:ring-zinc-200"
              placeholder="Contoh: Paket Bronze"
            />
            <p className="text-xs text-zinc-500">Gunakan untuk uji komposisi field.</p>
          </FormField>
          <FormField htmlFor="sample-capacity" label="Kapasitas" error="Contoh error untuk verifikasi tampilan validasi.">
            <input
              id="sample-capacity"
              type="number"
              aria-invalid="true"
              className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm text-zinc-900 outline-none transition focus:border-zinc-500 focus:ring-2 focus:ring-zinc-200"
              placeholder="0"
            />
          </FormField>
        </div>
        <ErrorMessage message="Contoh general error: simpan data dinonaktifkan karena halaman ini hanya untuk verifikasi komponen." />
      </section>

      <section className="space-y-3 rounded-lg border border-zinc-200 bg-white p-5 shadow-sm">
        <h2 className="text-sm font-semibold uppercase tracking-wide text-zinc-500">Checklist Komponen</h2>
        <DataTable columns={checklistColumns} data={COMPONENT_CHECKLIST} getRowKey={(row) => row.component} />
      </section>

      <p className="text-sm text-zinc-600">
        Akses admin tetap berlaku. Bundle permission admin saat ini: <span className="font-medium">{ADMIN_PERMISSION_BUNDLE.join(", ")}</span>.
      </p>
      </AppShell>
    </RequirePermission>
  );
}
