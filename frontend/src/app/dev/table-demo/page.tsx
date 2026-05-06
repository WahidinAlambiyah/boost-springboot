import AppShell from "@/app/components/app-shell";
import { PageHeader } from "@/app/components/page-header";
import RequirePermission from "@/app/components/require-permission";
import { SectionCard } from "@/app/components/section-card";
import DevCrudTable from "@/features/dev-crud/components/dev-crud-table";
import { devCrudMockItems } from "@/features/dev-crud/dev-crud.mock";
import { DEV_TOOLS_READ_PERMISSIONS } from "@/features/dev-crud/dev-crud.types";

export default function DevTableDemoPage() {
  return (
    <RequirePermission permissions={[...DEV_TOOLS_READ_PERMISSIONS]} mode="any">
      <AppShell>
        <PageHeader
          title="Dev Table Demo"
          description="Halaman protected untuk memvalidasi tampilan tabel reusable dengan sample data dev CRUD."
        />

        <SectionCard title="Readonly Table" description="Aksi write disembunyikan agar fokus pada state dan kolom tabel.">
          <DevCrudTable items={devCrudMockItems} />
        </SectionCard>
      </AppShell>
    </RequirePermission>
  );
}
