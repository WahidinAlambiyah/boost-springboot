import { DataTable, type DataTableColumn } from "@/app/components/data-table";
import { FormField } from "@/app/components/form-field";
import AppShell from "@/app/components/app-shell";
import { PageHeader } from "@/app/components/page-header";
import RequirePermission from "@/app/components/require-permission";
import { SectionCard } from "@/app/components/section-card";
import { StatusBadge } from "@/app/components/status-badge";
import { DEV_TOOLS_READ_PERMISSIONS } from "@/features/dev-crud/dev-crud.types";

interface ComponentDemoRow {
  name: string;
  status: "READY" | "REVIEW";
  usage: string;
}

const componentRows: ComponentDemoRow[] = [
  { name: "PageHeader", status: "READY", usage: "Standar judul, deskripsi, dan action halaman." },
  { name: "SectionCard", status: "READY", usage: "Membungkus section demo dengan header opsional." },
  { name: "DataTable", status: "READY", usage: "Menampilkan data tabular dengan empty, loading, dan error state." },
  { name: "FormField", status: "REVIEW", usage: "Label dan pesan validasi untuk input form." },
];

const columns: DataTableColumn<ComponentDemoRow>[] = [
  {
    key: "name",
    header: "Komponen",
    render: (row) => <span className="font-medium text-zinc-900">{row.name}</span>,
  },
  {
    key: "status",
    header: "Status",
    render: (row) => <StatusBadge status={row.status} variant={row.status === "READY" ? "success" : "warning"} />,
  },
  {
    key: "usage",
    header: "Penggunaan",
    render: (row) => row.usage,
  },
];

export default function DevComponentsPage() {
  return (
    <RequirePermission permissions={[...DEV_TOOLS_READ_PERMISSIONS]} mode="any">
      <AppShell>
        <PageHeader
          title="Dev Components"
          description="Halaman protected untuk melihat contoh komponen UI internal yang sering dipakai."
        />

        <div className="space-y-6">
          <SectionCard title="Component Matrix" description="Ringkasan komponen reusable untuk halaman admin dan dev tools.">
            <DataTable columns={columns} data={componentRows} getRowKey={(row) => row.name} />
          </SectionCard>

          <SectionCard title="Form Field Sample" description="Contoh field non-interaktif untuk dokumentasi visual.">
            <div className="max-w-md">
              <FormField label="Nama Komponen" required error="Contoh pesan validasi">
                <input
                  className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm"
                  defaultValue="DevCrudForm"
                  readOnly
                />
              </FormField>
            </div>
          </SectionCard>
        </div>
      </AppShell>
    </RequirePermission>
  );
}
