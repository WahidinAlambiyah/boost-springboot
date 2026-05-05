import PublicPageLayout from "@/app/(public)/components/public-page-layout";

export default function ProgramPage() {
  return (
    <PublicPageLayout>
      <section className="container mx-auto w-full px-6 py-16">
        <h1 className="text-3xl font-semibold text-zinc-900">Program</h1>
        <p className="mt-4 max-w-2xl text-zinc-700">
          Jelajahi program Boost Academy untuk mendukung perkembangan motorik, fokus belajar, dan kepercayaan diri anak.
        </p>
      </section>
    </PublicPageLayout>
  );
}
