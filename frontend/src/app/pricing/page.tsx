import PublicPageLayout from "@/app/(public)/components/public-page-layout";

export default function PricingPage() {
  const packages = ["Trial Class", "Paket Bulanan", "Private Coaching", "Event Race"];

  return (
    <PublicPageLayout>
      <section className="container mx-auto w-full px-6 py-16">
        <h1 className="text-3xl font-semibold text-zinc-900">Pricing</h1>
        <p className="mt-4 max-w-2xl text-zinc-700">
          Informasi paket biaya program dan pilihan kelas yang bisa disesuaikan dengan kebutuhan perkembangan anak.
        </p>
        <ul className="mt-6 list-disc space-y-2 pl-6 text-zinc-800">
          {packages.map((item) => (
            <li key={item}>{item}</li>
          ))}
        </ul>
        <p className="mt-6 text-sm text-zinc-600">
          Harga dapat disesuaikan berdasarkan lokasi, jadwal, dan jenis program.
        </p>
      </section>
    </PublicPageLayout>
  );
}
