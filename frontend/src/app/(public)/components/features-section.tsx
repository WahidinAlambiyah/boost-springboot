export default function FeaturesSection() {
  const features = [
    "Monitoring kelas dan sesi pembelajaran realtime",
    "Manajemen siswa, pelatih, dan paket latihan",
    "Laporan operasional dan progres belajar terpusat",
  ];

  return (
    <section className="layout-container section-space">
      <h2 className="text-2xl font-semibold text-zinc-900">Fitur Utama</h2>
      <div className="mt-6 grid gap-4 md:grid-cols-3">
        {features.map((feature) => (
          <article key={feature} className="surface-card rounded-xl p-5">
            <p className="text-body text-zinc-700">{feature}</p>
          </article>
        ))}
      </div>
    </section>
  );
}
