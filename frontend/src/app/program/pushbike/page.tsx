import PublicPageLayout from "@/app/(public)/components/public-page-layout";

export default function PushbikeProgramPage() {
  return (
    <PublicPageLayout>
      <section className="container mx-auto w-full px-6 py-16">
        <h1 className="text-3xl font-semibold text-zinc-900">Program Pushbike</h1>
        <p className="mt-4 max-w-2xl text-zinc-700">
          Program pushbike berfokus pada keseimbangan, koordinasi, dan teknik dasar berkendara anak lewat sesi latihan yang menyenangkan.
        </p>
      </section>
    </PublicPageLayout>
  );
}
