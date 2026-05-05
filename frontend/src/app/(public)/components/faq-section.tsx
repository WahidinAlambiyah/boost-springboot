const faqs = [
  {
    q: "Anak usia berapa bisa ikut?",
    a: "Program dirancang untuk anak usia 2–7 tahun dengan pembagian level agar latihan tetap aman dan menyenangkan.",
  },
  {
    q: "Harus punya pushbike sendiri?",
    a: "Tidak wajib. Untuk trial class, anak bisa menggunakan unit latihan yang disediakan sesuai ketersediaan di lokasi.",
  },
  {
    q: "Apakah orang tua wajib mendampingi?",
    a: "Disarankan mendampingi saat trial agar orang tua bisa memahami arahan coach dan perkembangan awal anak.",
  },
  {
    q: "Kalau hujan bagaimana?",
    a: "Tim akan menginformasikan penjadwalan ulang atau opsi sesi indoor sesuai kondisi cuaca dan lokasi latihan.",
  },
  {
    q: "Apakah anak langsung ikut race?",
    a: "Tidak. Coach akan menilai kesiapan anak terlebih dahulu dari skill dasar, fokus, dan keberanian sebelum ikut event race.",
  },
  {
    q: "Apakah ada laporan perkembangan?",
    a: "Ada. Orang tua menerima ringkasan progres anak yang mencakup kehadiran, kemampuan teknis, dan rekomendasi latihan berikutnya.",
  },
  {
    q: "Apakah bisa trial dulu?",
    a: "Bisa. Anda bisa mulai dari trial class agar anak mencoba suasana latihan sebelum memilih program reguler.",
  },
];

export default function FaqSection() {
  return (
    <section id="faq" className="layout-container section-space">
      <h2 className="text-2xl font-semibold tracking-tight text-zinc-900">Pertanyaan Umum Orang Tua</h2>
      <div className="mt-8 grid gap-4 md:grid-cols-2">
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
