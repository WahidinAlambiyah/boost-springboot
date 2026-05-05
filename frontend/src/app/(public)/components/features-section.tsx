export default function FeaturesSection() {
  const features = [
    "Monitoring kelas dan sesi pembelajaran realtime",
    "Manajemen siswa, pelatih, dan paket latihan",
    "Laporan operasional dan progres belajar terpusat",
  ];

  return (
    <section className="mx-auto w-full max-w-6xl px-6 py-14">
      <h2 className="text-2xl font-semibold text-zinc-900">Fitur Utama</h2>
      <div className="mt-6 grid gap-4 md:grid-cols-3">
        {features.map((feature) => (
          <article key={feature} className="rounded-xl border border-zinc-200 bg-white p-5 shadow-sm">
            <p className="text-sm leading-6 text-zinc-700">{feature}</p>
          </article>
        ))}
      </div>
    </section>
  );
}
