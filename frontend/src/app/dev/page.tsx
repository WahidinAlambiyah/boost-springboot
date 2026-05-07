"use client";

import Link from "next/link";

import AppShell from "@/app/components/app-shell";
import { PageHeader } from "@/app/components/page-header";
import RequirePermission from "@/app/components/require-permission";

interface DevToolLink {
  title: string;
  href: string;
  description: string;
}

const devToolLinks: DevToolLink[] = [
  {
    title: "Components",
    href: "/dev/components",
    description: "Katalog komponen UI reusable untuk baseline halaman internal.",
  },
  {
    title: "CRUD Demo",
    href: "/dev/crud-demo",
    description: "Contoh pola list, create, update, delete, dan state CRUD.",
  },
  {
    title: "Table Demo",
    href: "/dev/table-demo",
    description: "Referensi tabel data dengan empty, loading, dan error state.",
  },
  {
    title: "Form Demo",
    href: "/dev/form-demo",
    description: "Contoh struktur form, validasi, dan feedback input.",
  },
];

export default function DevToolsPage() {
  return (
    // TODO: remove ROLE_READ/USER_READ fallback when DEV_TOOLS_READ is managed by backend.
    <RequirePermission permissions={["DEV_TOOLS_READ", "ROLE_READ", "USER_READ"]} mode="any">
      <AppShell>
        <PageHeader
          title="Developer Tools"
          description="Kumpulan halaman demo internal untuk mempercepat pengembangan dan validasi pola UI."
        />

        <section className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
          {devToolLinks.map((tool) => (
            <Link
              key={tool.href}
              href={tool.href}
              className="group rounded-lg border border-zinc-200 bg-white p-5 shadow-sm transition hover:-translate-y-0.5 hover:border-zinc-300 hover:shadow-md focus:outline-none focus:ring-2 focus:ring-zinc-900 focus:ring-offset-2"
            >
              <div className="flex items-start justify-between gap-3">
                <h2 className="text-base font-semibold text-zinc-900">{tool.title}</h2>
                <span aria-hidden="true" className="text-zinc-400 transition group-hover:translate-x-0.5 group-hover:text-zinc-700">
                  →
                </span>
              </div>
              <p className="mt-2 text-sm leading-6 text-zinc-600">{tool.description}</p>
              <p className="mt-4 text-xs font-medium uppercase tracking-wide text-zinc-500">{tool.href}</p>
            </Link>
          ))}
        </section>
      </AppShell>
    </RequirePermission>
  );
}
