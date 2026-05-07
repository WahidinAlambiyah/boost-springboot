"use client";

import AppShell from "@/app/components/app-shell";
import { ConfirmActionButton } from "@/app/components/confirm-action-button";
import { DataTable, type DataTableColumn } from "@/app/components/data-table";
import EmptyState from "@/app/components/empty-state";
import { ErrorMessage } from "@/app/components/error-message";
import { FormField } from "@/app/components/form-field";
import LoadingSkeleton from "@/app/components/loading-skeleton";
import { PageHeader } from "@/app/components/page-header";
import { SectionCard } from "@/app/components/section-card";
import { StatCard } from "@/app/components/stat-card";
import { StatusBadge } from "@/app/components/status-badge";

interface ComponentDemoRow {
  name: string;
  status: "READY" | "REVIEW" | "DRAFT";
  group: "Layout" | "Feedback" | "Actions" | "Forms" | "Tables";
  usage: string;
}

const componentRows: ComponentDemoRow[] = [
  {
    name: "PageHeader",
    status: "READY",
    group: "Layout",
    usage: "Standar judul, deskripsi, dan action halaman CRUD.",
  },
  {
    name: "SectionCard",
    status: "READY",
    group: "Layout",
    usage: "Mengelompokkan form, tabel, filter, atau summary agar halaman mudah dipindai.",
  },
  {
    name: "StatCard",
    status: "READY",
    group: "Layout",
    usage: "Menonjolkan metrik ringkas seperti total data, status aktif, atau item perlu review.",
  },
  {
    name: "StatusBadge",
    status: "READY",
    group: "Feedback",
    usage: "Memberi tone visual untuk status entity seperti ACTIVE, DRAFT, APPROVED, atau PAID.",
  },
  {
    name: "ErrorMessage",
    status: "READY",
    group: "Feedback",
    usage: "Menampilkan error API atau validasi global di atas form dan tabel.",
  },
  {
    name: "EmptyState",
    status: "READY",
    group: "Feedback",
    usage: "Mengisi area kosong saat data CRUD belum tersedia.",
  },
  {
    name: "LoadingSkeleton",
    status: "READY",
    group: "Feedback",
    usage: "Placeholder saat query list atau detail masih berjalan.",
  },
  {
    name: "ConfirmActionButton",
    status: "REVIEW",
    group: "Actions",
    usage: "Konfirmasi cepat untuk delete, cancel, approve, atau aksi sensitif lain.",
  },
  {
    name: "FormField",
    status: "READY",
    group: "Forms",
    usage: "Label, required indicator, control, dan pesan error dalam satu pola konsisten.",
  },
  {
    name: "DataTable",
    status: "READY",
    group: "Tables",
    usage: "Menampilkan data tabular dengan loading, error, empty state, dan row key.",
  },
];

const columns: DataTableColumn<ComponentDemoRow>[] = [
  {
    key: "name",
    header: "Komponen",
    render: (row) => <span className="font-medium text-zinc-900">{row.name}</span>,
  },
  {
    key: "group",
    header: "Group",
    render: (row) => row.group,
  },
  {
    key: "status",
    header: "Status",
    render: (row) => <StatusBadge status={row.status} />,
  },
  {
    key: "usage",
    header: "Penggunaan",
    render: (row) => row.usage,
  },
];

// TODO: replace dummy create/update/delete checks with DEV_TOOLS_WRITE when dev tools write permission is available.
const noopConfirm = () => undefined;

export default function DevComponentsPage() {
  return (
    <AppShell>
      <PageHeader
        title="Dev Components"
        description="Katalog demo komponen UI internal untuk mempercepat pembuatan halaman CRUD berikutnya. Semua contoh di halaman ini memakai data dummy static."
        actions={
          <StatusBadge
            status="STATIC DEMO"
            variant="info"
            className="rounded-md px-3 py-2"
          />
        }
      />

      <div className="space-y-6">
        <SectionCard
          title="Panduan CRUD Singkat"
          description="Gunakan pola ini sebagai baseline saat membuat halaman list, create, edit, dan detail berikutnya."
        >
          <div className="grid gap-4 text-sm text-zinc-700 md:grid-cols-3">
            <div className="rounded-lg border border-zinc-200 bg-zinc-50 p-4">
              <p className="font-semibold text-zinc-900">1. Mulai dari struktur halaman</p>
              <p className="mt-2">
                Bungkus route protected dengan <code>RequirePermission</code> dan <code>AppShell</code>, lalu pakai <code>PageHeader</code> untuk judul, deskripsi, dan action utama.
              </p>
            </div>
            <div className="rounded-lg border border-zinc-200 bg-zinc-50 p-4">
              <p className="font-semibold text-zinc-900">2. Pisahkan area kerja</p>
              <p className="mt-2">
                Gunakan <code>SectionCard</code> untuk filter, form, tabel, dan ringkasan. Tambahkan <code>StatCard</code> jika perlu highlight angka penting.
              </p>
            </div>
            <div className="rounded-lg border border-zinc-200 bg-zinc-50 p-4">
              <p className="font-semibold text-zinc-900">3. Tutup semua state</p>
              <p className="mt-2">
                Setiap CRUD sebaiknya punya loading, empty, error, status badge, form validation, table, dan konfirmasi aksi destruktif.
              </p>
            </div>
          </div>
        </SectionCard>

        <SectionCard
          title="Layout"
          description="Contoh PageHeader, SectionCard, dan StatCard untuk membangun kerangka halaman admin."
        >
          <PageHeader
            title="Academy CRUD"
            description="Contoh header lokal untuk halaman list academy dengan action utama."
            className="mb-4 rounded-lg border border-dashed border-zinc-300 bg-zinc-50 p-4"
            actions={
              <button
                type="button"
                className="rounded-md bg-zinc-900 px-3 py-2 text-sm font-medium text-white"
              >
                Tambah Data
              </button>
            }
          />
          <div className="grid gap-4 md:grid-cols-3">
            <StatCard title="Total Komponen" value={componentRows.length} description="Komponen reusable yang tersedia di katalog demo." trend="+3 baru" />
            <StatCard title="Siap CRUD" value="8" description="Komponen yang bisa langsung dipakai untuk list dan form." trend={<StatusBadge status="READY" />} />
            <StatCard title="Perlu Review" value="2" description="Contoh area untuk menandai komponen yang butuh refinement." trend={<StatusBadge status="REVIEW" variant="warning" />} />
          </div>
        </SectionCard>

        <SectionCard
          title="Feedback"
          description="StatusBadge, ErrorMessage, EmptyState, dan LoadingSkeleton untuk state komunikasi pengguna."
        >
          <div className="grid gap-4 lg:grid-cols-2">
            <div className="rounded-lg border border-zinc-200 p-4">
              <p className="mb-3 text-sm font-semibold text-zinc-900">StatusBadge</p>
              <div className="flex flex-wrap gap-2">
                <StatusBadge status="ACTIVE" />
                <StatusBadge status="DRAFT" />
                <StatusBadge status="APPROVED" />
                <StatusBadge status="CANCELLED" />
                <StatusBadge status="CUSTOM" variant="info" />
              </div>
            </div>
            <ErrorMessage
              title="Gagal menyimpan data"
              message="Contoh pesan error static untuk response API atau validasi bisnis."
            >
              Coba cek input wajib, lalu ulangi submit.
            </ErrorMessage>
            <EmptyState
              title="Belum ada data"
              description="Tampilkan empty state ketika query list CRUD mengembalikan array kosong."
            />
            <div className="rounded-lg border border-zinc-200 p-4">
              <p className="text-sm font-semibold text-zinc-900">LoadingSkeleton</p>
              <LoadingSkeleton rows={4} />
            </div>
          </div>
        </SectionCard>

        <SectionCard
          title="Actions"
          description="ConfirmActionButton untuk aksi yang perlu persetujuan eksplisit pengguna."
        >
          <div className="flex flex-wrap items-center gap-3">
            <ConfirmActionButton
              label="Arsipkan Data"
              confirmMessage="Arsipkan data dummy ini?"
              onConfirm={noopConfirm}
            />
            <ConfirmActionButton
              label="Hapus Data"
              confirmMessage="Hapus data dummy ini?"
              onConfirm={noopConfirm}
              variant="danger"
            />
            <ConfirmActionButton
              label="Aksi Disabled"
              confirmMessage="Aksi ini sedang disabled."
              onConfirm={noopConfirm}
              disabled
            />
          </div>
        </SectionCard>

        <SectionCard
          title="Forms"
          description="FormField menjaga label, required indicator, control, dan pesan error tetap konsisten."
        >
          <div className="grid gap-4 md:grid-cols-2">
            <FormField label="Nama Entity" htmlFor="entity-name" required>
              <input
                id="entity-name"
                className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm"
                defaultValue="Academy Kemang"
                readOnly
              />
            </FormField>
            <FormField label="Status" htmlFor="entity-status">
              <select
                id="entity-status"
                className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm"
                defaultValue="ACTIVE"
                disabled
              >
                <option value="ACTIVE">ACTIVE</option>
                <option value="INACTIVE">INACTIVE</option>
              </select>
            </FormField>
            <FormField label="Kode" htmlFor="entity-code" error="Kode wajib diisi untuk contoh validasi.">
              <input
                id="entity-code"
                className="w-full rounded-md border border-red-300 px-3 py-2 text-sm"
                defaultValue=""
                readOnly
              />
            </FormField>
          </div>
        </SectionCard>

        <SectionCard
          title="Tables"
          description="DataTable untuk daftar CRUD. Contoh ini memakai row static dan kolom reusable."
        >
          <DataTable columns={columns} data={componentRows} getRowKey={(row) => row.name} />
        </SectionCard>
      </div>
    </AppShell>
  );
}
