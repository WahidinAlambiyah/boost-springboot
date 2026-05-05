const faqs = [
  {
    q: "Apakah bisa dipakai untuk beberapa cabang akademi?",
    a: "Bisa. Anda dapat mengelola data siswa, coach, dan jadwal untuk banyak lokasi dalam satu dashboard.",
  },
  {
    q: "Apakah orang tua bisa lihat progres anak?",
    a: "Ya, progres dapat dirangkum dengan format yang mudah dibaca dan siap dibagikan oleh tim akademi.",
  },
  {
    q: "Berapa lama implementasinya?",
    a: "Umumnya 1-2 minggu untuk setup awal sesuai alur operasional akademi Anda.",
  },
];

export default function FaqSection() {
  return (
    <section id="faq" className="layout-container section-space">
      <h2 className="text-2xl font-semibold tracking-tight text-zinc-900">Pertanyaan Umum</h2>
      <div className="mt-8 space-y-4">
        {faqs.map((item) => (
          <article key={item.q} className="surface-card p-6">
            <h3 className="text-sm font-semibold text-zinc-900">{item.q}</h3>
            <p className="text-body mt-2">{item.a}</p>
          </article>
        ))}
      </div>
    </section>
  );
}
