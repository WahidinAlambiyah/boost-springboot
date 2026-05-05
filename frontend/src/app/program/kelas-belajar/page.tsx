import PublicFooter from "@/app/(public)/components/public-footer";
import PublicNavbar from "@/app/(public)/components/public-navbar";

export default function KelasBelajarPage() {
  return (
    <main className="min-h-screen bg-zinc-50">
      <PublicNavbar />
      <section className="container mx-auto w-full px-6 py-16">
        <h1 className="text-3xl font-semibold text-zinc-900">Kelas Belajar</h1>
        <p className="mt-4 max-w-2xl text-zinc-700">
          Kelas belajar dirancang untuk melatih fokus, kemampuan sosial, dan motorik anak melalui aktivitas terstruktur sesuai usia.
        </p>
      </section>
      <PublicFooter />
    </main>
  );
}
