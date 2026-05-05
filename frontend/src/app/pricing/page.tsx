import PublicPageLayout from "@/app/(public)/components/public-page-layout";

export default function PricingPage() {
  return (
    <PublicPageLayout>
      <section className="container mx-auto w-full px-6 py-16">
        <h1 className="text-3xl font-semibold text-zinc-900">Pricing</h1>
        <p className="mt-4 max-w-2xl text-zinc-700">
          Informasi paket biaya program dan pilihan kelas yang bisa disesuaikan dengan kebutuhan perkembangan anak.
        </p>
      </section>
    </PublicPageLayout>
  );
}
