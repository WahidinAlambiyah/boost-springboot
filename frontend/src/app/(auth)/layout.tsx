import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "Akses Akun",
  description: "Masuk ke Boost Academy untuk melanjutkan aktivitas Anda.",
  openGraph: {
    title: "Akses Akun | Boost Academy",
    description: "Masuk ke Boost Academy untuk melanjutkan aktivitas Anda.",
  },
};

export default function PublicLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return children;
}
