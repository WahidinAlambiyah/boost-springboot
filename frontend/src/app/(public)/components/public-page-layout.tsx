import type { ReactNode } from "react";

import PublicFooter from "@/app/(public)/components/public-footer";
import PublicNavbar from "@/app/(public)/components/public-navbar";

type PublicPageLayoutProps = {
  children: ReactNode;
};

export default function PublicPageLayout({ children }: PublicPageLayoutProps) {
  return (
    <main className="min-h-screen bg-zinc-50">
      <PublicNavbar />
      {children}
      <PublicFooter />
    </main>
  );
}
