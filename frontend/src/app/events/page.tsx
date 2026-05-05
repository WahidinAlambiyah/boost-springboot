import PublicFooter from "@/app/(public)/components/public-footer";
import PublicNavbar from "@/app/(public)/components/public-navbar";

export default function EventsPage() {
  return (
    <main className="min-h-screen bg-zinc-50">
      <PublicNavbar />
      <section className="container mx-auto w-full px-6 py-16">
        <h1 className="text-3xl font-semibold text-zinc-900">Events</h1>
        <p className="mt-4 max-w-2xl text-zinc-700">
          Lihat jadwal event dan race yang dirancang ramah anak untuk menumbuhkan sportivitas dan pengalaman kompetisi positif.
        </p>
      </section>
      <PublicFooter />
    </main>
  );
}
