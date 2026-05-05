import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import AppErrorBoundary from "@/app/components/app-error-boundary";
import QueryProvider from "@/lib/query-provider";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: {
    default: "Boost Academy",
    template: "%s | Boost Academy",
  },
  description:
    "Platform manajemen operasional akademi untuk administrasi, pembelajaran, dan pelaporan.",
  openGraph: {
    title: "Boost Academy",
    description:
      "Platform manajemen operasional akademi untuk administrasi, pembelajaran, dan pelaporan.",
    type: "website",
    locale: "id_ID",
    siteName: "Boost Academy",
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html
      lang="en"
      className={`${geistSans.variable} ${geistMono.variable} h-full antialiased`}
    >
      <body className="min-h-full flex flex-col">
        <AppErrorBoundary>
          <QueryProvider>{children}</QueryProvider>
        </AppErrorBoundary>
      </body>
    </html>
  );
}
