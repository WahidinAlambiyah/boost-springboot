import PublicFooter from "@/app/(public)/components/public-footer";
import PublicNavbar from "@/app/(public)/components/public-navbar";

export default function ContactPage() {
  return (
    <main className="min-h-screen bg-zinc-50">
      <PublicNavbar />
      <section className="container mx-auto w-full px-6 py-16">
        <h1 className="text-3xl font-semibold text-zinc-900">Contact</h1>
        <p className="mt-4 max-w-2xl text-zinc-700">
          Hubungi tim kami untuk konsultasi program, jadwal trial class, dan informasi pendaftaran.
        </p>
      </section>
      <PublicFooter />
    </main>
  );
}
