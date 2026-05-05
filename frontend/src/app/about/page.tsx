import PublicPageLayout from "@/app/(public)/components/public-page-layout";

export default function AboutPage() {
  return (
    <PublicPageLayout>
      <section className="container mx-auto w-full space-y-8 px-6 py-16">
        <div>
          <h1 className="text-3xl font-semibold text-zinc-900">Tentang Academy</h1>
          <p className="mt-4 max-w-3xl text-zinc-700">
            Boost Academy adalah ruang belajar dan aktivitas anak yang menggabungkan olahraga,
            pembelajaran, serta pendampingan coach berpengalaman agar setiap anak berkembang
            secara menyeluruh.
          </p>
        </div>

        <div>
          <h2 className="text-2xl font-semibold text-zinc-900">Misi</h2>
          <p className="mt-3 max-w-3xl text-zinc-700">
            Membantu anak bertumbuh jadi pribadi yang sehat, percaya diri, dan punya semangat
            belajar lewat proses yang terarah, menyenangkan, dan aman.
          </p>
        </div>

        <div>
          <h2 className="text-2xl font-semibold text-zinc-900">Nilai Utama</h2>
          <ul className="mt-3 list-disc space-y-2 pl-6 text-zinc-700">
            <li>Aman</li>
            <li>Fun</li>
            <li>Progress</li>
            <li>Komunitas positif</li>
          </ul>
        </div>

        <div>
          <h2 className="text-2xl font-semibold text-zinc-900">Kenapa progress anak dicatat?</h2>
          <p className="mt-3 max-w-3xl text-zinc-700">
            Pencatatan progress membantu coach dan orang tua memahami perkembangan anak dari
            waktu ke waktu, melihat kekuatan yang perlu terus didorong, serta area yang perlu
            pendampingan tambahan supaya tujuan belajar anak lebih jelas dan terukur.
          </p>
        </div>
      </section>
    </PublicPageLayout>
  );
}
