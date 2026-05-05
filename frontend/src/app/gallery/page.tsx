import PublicFooter from "@/app/(public)/components/public-footer";
import PublicNavbar from "@/app/(public)/components/public-navbar";

export default function GalleryPage() {
  return (
    <main className="min-h-screen bg-zinc-50">
      <PublicNavbar />
      <section className="container mx-auto w-full px-6 py-16">
        <h1 className="text-3xl font-semibold text-zinc-900">Gallery</h1>
        <p className="mt-4 max-w-2xl text-zinc-700">
          Dokumentasi kegiatan kelas, latihan pushbike, dan momen terbaik event akademi.
        </p>
      </section>
      <PublicFooter />
    </main>
  );
}
