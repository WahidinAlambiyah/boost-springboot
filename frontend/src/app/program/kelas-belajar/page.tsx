import PublicPageLayout from "@/app/(public)/components/public-page-layout";

export default function KelasBelajarPage() {
  return (
    <PublicPageLayout>
      <section className="container mx-auto w-full px-6 py-16">
        <h1 className="text-3xl font-semibold text-zinc-900">Kelas Belajar</h1>
        <p className="mt-4 max-w-2xl text-zinc-700">
          Kelas belajar dirancang untuk melatih fokus, kemampuan sosial, dan motorik anak melalui aktivitas terstruktur sesuai usia.
        </p>
      </section>
    </PublicPageLayout>
  );
}
