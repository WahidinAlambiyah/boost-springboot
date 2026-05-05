import PublicPageLayout from "@/app/(public)/components/public-page-layout";

export default function AboutPage() {
  return (
    <PublicPageLayout>
      <section className="container mx-auto w-full px-6 py-16">
        <h1 className="text-3xl font-semibold text-zinc-900">About</h1>
        <p className="mt-4 max-w-2xl text-zinc-700">
          Boost Academy adalah ruang belajar dan aktivitas anak yang menggabungkan olahraga, pembelajaran, serta pendampingan coach berpengalaman.
        </p>
      </section>
    </PublicPageLayout>
  );
}
