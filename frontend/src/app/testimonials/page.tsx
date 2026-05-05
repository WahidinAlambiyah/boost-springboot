import PublicFooter from "@/app/(public)/components/public-footer";
import PublicNavbar from "@/app/(public)/components/public-navbar";

export default function TestimonialsPage() {
  return (
    <main className="min-h-screen bg-zinc-50">
      <PublicNavbar />
      <section className="container mx-auto w-full px-6 py-16">
        <h1 className="text-3xl font-semibold text-zinc-900">Testimonials</h1>
        <p className="mt-4 max-w-2xl text-zinc-700">
          Cerita orang tua, coach, dan peserta tentang pengalaman mereka bersama program Boost Academy.
        </p>
      </section>
      <PublicFooter />
    </main>
  );
}
