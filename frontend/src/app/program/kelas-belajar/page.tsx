import Link from "next/link";

import PublicPageLayout from "@/app/(public)/components/public-page-layout";

const kelasList = [
  "Fun English",
  "Basic Math",
  "Storytelling",
  "Focus Activity",
  "Creative Learning",
];

export default function KelasBelajarPage() {
  return (
    <PublicPageLayout>
      <section className="container mx-auto w-full px-6 py-16">
        <h1 className="text-3xl font-semibold text-zinc-900">Kelas Belajar</h1>

        <div className="mt-8 grid gap-8 lg:grid-cols-2">
          <div>
            <h2 className="text-xl font-semibold text-zinc-900">Pilihan Kelas</h2>
            <ul className="mt-4 list-disc space-y-2 pl-6 text-zinc-700">
              {kelasList.map((kelas) => (
                <li key={kelas}>{kelas}</li>
              ))}
            </ul>
          </div>

          <div>
            <h2 className="text-xl font-semibold text-zinc-900">Manfaat untuk Anak</h2>
            <p className="mt-4 text-zinc-700">
              Program dirancang untuk membantu anak lebih percaya diri, fokus saat belajar, terbiasa berinteraksi, dan berkembang
              secara kreatif melalui aktivitas yang menyenangkan.
            </p>
          </div>
        </div>

        <div className="mt-10 rounded-xl border border-zinc-200 bg-zinc-50 p-6">
          <h2 className="text-xl font-semibold text-zinc-900">Jadwal Dummy</h2>
          <ul className="mt-4 space-y-2 text-zinc-700">
            <li>Senin & Rabu - 16.00 - 17.00 (Fun English & Storytelling)</li>
            <li>Selasa & Kamis - 16.00 - 17.00 (Basic Math & Focus Activity)</li>
            <li>Jumat - 15.30 - 16.30 (Creative Learning)</li>
          </ul>
        </div>

        <div className="mt-10">
          <Link
            href="/kontak"
            className="inline-flex items-center rounded-lg bg-teal-600 px-5 py-3 text-sm font-semibold text-white transition hover:bg-teal-700"
          >
            Daftar Trial Gratis
          </Link>
        </div>
      </section>
    </PublicPageLayout>
  );
}
