"use client";

import { useRouter } from "next/navigation";
import { useEffect } from "react";

import { DataTable, type DataTableColumn } from "@/app/components/data-table";
import { ErrorMessage } from "@/app/components/error-message";
import { FormField } from "@/app/components/form-field";
import { PageHeader } from "@/app/components/page-header";
import { StatusBadge, type StatusBadgeTone } from "@/app/components/status-badge";
import { ADMIN_PERMISSION_BUNDLE, hasAdminAccess } from "@/lib/admin-guard";
import { useAuthStore } from "@/store/auth";

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
  { component: "data-table", statusLabel: "Ready", statusTone: "success", note: "Generic columns + rowKey untuk daftar data." },
  { component: "form-field", statusLabel: "Ready", statusTone: "success", note: "Input dasar dengan hint/error bawaan." },
];

const checklistColumns: DataTableColumn<ComponentChecklistRow>[] = [
  {
    key: "component",
    header: "Komponen",
    cell: (row) => <span className="font-medium text-zinc-900">{row.component}</span>,
  },
  {
    key: "status",
    header: "Status",
    cell: (row) => <StatusBadge label={row.statusLabel} tone={row.statusTone} />,
  },
  {
    key: "note",
    header: "Catatan Reuse",
    cell: (row) => row.note,
  },
];

export default function AdminPage() {
  const router = useRouter();
  const status = useAuthStore((state) => state.status);
  const authorities = useAuthStore((state) => state.authorities);

  const isAllowed = hasAdminAccess(authorities);

  useEffect(() => {
    if (status !== "authenticated") {
      return;
    }

    if (!isAllowed) {
      router.replace("/forbidden");
    }
  }, [isAllowed, router, status]);

  if (status !== "authenticated" || !isAllowed) {
    return null;
  }

  return (
    <main className="flex-1 space-y-6 p-6">
      <PageHeader
        title="UI Component Verification"
        description="Halaman utilitas internal untuk validasi compile-time dan pola reuse komponen inti tanpa implementasi CRUD penuh."
        actions={<StatusBadge label="Internal Utility" tone="info" />}
      />

      <section className="space-y-3 rounded-lg border border-zinc-200 bg-white p-5 shadow-sm">
        <h2 className="text-sm font-semibold uppercase tracking-wide text-zinc-500">Form Sample</h2>
        <div className="grid gap-3 md:grid-cols-2">
          <FormField id="sample-name" label="Nama" placeholder="Contoh: Paket Bronze" hint="Gunakan untuk uji komposisi field." />
          <FormField id="sample-capacity" label="Kapasitas" type="number" placeholder="0" error="Contoh error untuk verifikasi tampilan validasi." />
        </div>
        <ErrorMessage message="Contoh general error: simpan data dinonaktifkan karena halaman ini hanya untuk verifikasi komponen." />
      </section>

      <section className="space-y-3 rounded-lg border border-zinc-200 bg-white p-5 shadow-sm">
        <h2 className="text-sm font-semibold uppercase tracking-wide text-zinc-500">Checklist Komponen</h2>
        <DataTable columns={checklistColumns} data={COMPONENT_CHECKLIST} rowKey={(row) => row.component} />
      </section>

      <p className="text-sm text-zinc-600">
        Akses admin tetap berlaku. Bundle permission admin saat ini: <span className="font-medium">{ADMIN_PERMISSION_BUNDLE.join(", ")}</span>.
      </p>
    </main>
  );
}
