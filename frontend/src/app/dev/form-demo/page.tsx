"use client";

import { useMemo, useState } from "react";

import AppShell from "@/app/components/app-shell";
import { PageHeader } from "@/app/components/page-header";
import RequirePermission from "@/app/components/require-permission";
import { SectionCard } from "@/app/components/section-card";
import DevCrudForm from "@/features/dev-crud/components/dev-crud-form";
import type { DevCrudFormValues } from "@/features/dev-crud/dev-crud.schema";
import { DEV_TOOLS_READ_PERMISSIONS, DEV_TOOLS_WRITE_PERMISSIONS } from "@/features/dev-crud/dev-crud.types";
import { canAny } from "@/lib/permissions";
import { useAuthStore } from "@/store/auth";

export default function DevFormDemoPage() {
  const authorities = useAuthStore((state) => state.authorities);
  const canWrite = useMemo(() => canAny(authorities, [...DEV_TOOLS_WRITE_PERMISSIONS]), [authorities]);
  const [lastSubmit, setLastSubmit] = useState<DevCrudFormValues | null>(null);

  return (
    <RequirePermission permissions={[...DEV_TOOLS_READ_PERMISSIONS]} mode="any">
      <AppShell>
        <PageHeader
          title="Dev Form Demo"
          description="Halaman protected untuk mengecek field, validasi, dan payload submit form dev CRUD."
        />

        <div className="grid gap-6 lg:grid-cols-[minmax(0,1fr)_360px]">
          <SectionCard title="Form Playground" description="Submit tidak memanggil API; payload ditampilkan di panel preview.">
            <DevCrudForm canWrite={canWrite} onSubmit={setLastSubmit} />
          </SectionCard>

          <SectionCard title="Payload Preview" description="Hasil submit terakhir dari form demo.">
            <pre className="overflow-auto rounded-md bg-zinc-950 p-4 text-xs text-zinc-100">
              {lastSubmit ? JSON.stringify(lastSubmit, null, 2) : "Belum ada submit."}
            </pre>
          </SectionCard>
        </div>
      </AppShell>
    </RequirePermission>
  );
}
