import PublicPageLayout from "@/app/(public)/components/public-page-layout";

const highlightPoints = [
  "Lebih percaya diri",
  "Lebih fokus",
  "Lebih berani",
  "Progress lebih mudah dipantau",
];

export default function TestimonialsPage() {
  return (
    <PublicPageLayout>
      <section className="container mx-auto w-full px-6 py-16">
        <h1 className="text-3xl font-semibold text-zinc-900">Testimonials</h1>
        <p className="mt-4 max-w-2xl text-zinc-700">
          Cerita orang tua, coach, dan peserta tentang pengalaman mereka bersama program Boost Academy.
        </p>

        <div className="mt-10 grid gap-6 lg:grid-cols-2">
          <article className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm">
            <h2 className="text-xl font-semibold text-zinc-900">Section Testimoni Orang Tua</h2>
            <p className="mt-3 text-zinc-700">
              Orang tua melihat perubahan positif pada anak setelah mengikuti program: komunikasi lebih terbuka,
              semangat belajar meningkat, dan kebiasaan baik mulai terbentuk secara konsisten.
            </p>
          </article>

          <article className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm">
            <h2 className="text-xl font-semibold text-zinc-900">Section Testimoni Coach</h2>
            <p className="mt-3 text-zinc-700">
              Coach menyoroti peningkatan disiplin latihan, kemampuan memahami arahan, serta keberanian anak untuk
              mencoba tantangan baru di setiap sesi.
            </p>
          </article>
        </div>

        <article className="mt-8 rounded-2xl border border-zinc-200 bg-zinc-50 p-6">
          <h2 className="text-xl font-semibold text-zinc-900">Cerita Perkembangan Anak</h2>
          <p className="mt-3 text-zinc-700">
            Setiap anak berkembang dengan ritme masing-masing. Dengan pendampingan terstruktur, orang tua dapat
            memahami progres dari waktu ke waktu dan memberi dukungan yang tepat di rumah.
          </p>

          <ul className="mt-4 grid gap-3 sm:grid-cols-2">
            {highlightPoints.map((point) => (
              <li
                key={point}
                className="rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm font-medium text-emerald-800"
              >
                {point}
              </li>
            ))}
          </ul>
        </article>
      </section>
    </PublicPageLayout>
  );
}
