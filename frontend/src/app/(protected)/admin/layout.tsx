import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "Admin",
  description: "Area administrasi untuk pengaturan dan kontrol operasional Boost Academy.",
  openGraph: {
    title: "Admin | Boost Academy",
    description:
      "Area administrasi untuk pengaturan dan kontrol operasional Boost Academy.",
  },
};

export default function AdminLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return children;
}
