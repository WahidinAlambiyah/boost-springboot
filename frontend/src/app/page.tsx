import CtaSection from "@/app/(public)/components/cta-section";
import FaqSection from "@/app/(public)/components/faq-section";
import HeroSection from "@/app/(public)/components/hero-section";
import ProgramCard from "@/app/(public)/components/program-card";
import ProgressPreviewCard from "@/app/(public)/components/progress-preview-card";
import PublicFooter from "@/app/(public)/components/public-footer";
import PublicNavbar from "@/app/(public)/components/public-navbar";
import TestimonialCard from "@/app/(public)/components/testimonial-card";

const trustBadges = ["Untuk usia 2–7 tahun", "Didampingi coach", "Progress anak tercatat", "Laporan untuk orang tua"];

const reasons = [
  "Kurikulum terstruktur sesuai usia dan level anak",
  "Laporan progres mingguan dengan insight pelatih",
  "Komunikasi orang tua dan coach lebih cepat",
];

const programs = [
  {
    title: "Pushbike Class",
    level: "Program utama",
    description: "Latihan keseimbangan, kontrol arah, dan teknik dasar pushbike dengan pendekatan fun learning.",
  },
  {
    title: "Kegiatan Belajar Anak",
    level: "Program utama",
    description: "Aktivitas belajar terarah untuk melatih fokus, motorik, dan kerja sama anak dalam suasana yang menyenangkan.",
  },
  {
    title: "Event & Race",
    level: "Program utama",
    description: "Sesi event dan race ramah anak untuk membangun keberanian, sportivitas, dan pengalaman kompetisi positif.",
  },
];

const progressPreviews = [
  {
    studentName: "Rafa",
    level: "Beginner",
    balance: "4/5",
    braking: "3/5",
    confidence: "4/5",
    coachNote: "Rafa menunjukkan progres baik di lintasan lurus dan mulai berani mengambil tikungan. Lanjutkan latihan braking bertahap agar kontrol kecepatan makin stabil.",
  },
];

const pushbikeActivities = ["Drill start gate", "Mini obstacle run", "Cornering challenge"];
const learningActivities = ["Motorik halus & kasar", "Latihan fokus instruksi", "Kolaborasi tim kecil"];
const raceEvents = ["Weekend Fun Race", "Regional Pushbike Cup", "Family Championship Day"];

const testimonials = [
  {
    quote: "Anak jadi lebih disiplin latihan dan kami selalu update progresnya tiap minggu.",
    name: "Dina P.",
    role: "Orang Tua Siswa",
  },
  {
    quote: "Manajemen kelas jauh lebih rapi, absensi dan evaluasi coach bisa langsung dipantau.",
    name: "Coach Ario",
    role: "Head Coach",
  },
  {
    quote: "Event balapan terasa lebih tertata karena semua data peserta dan jadwal sudah terintegrasi.",
    name: "Nadya R.",
    role: "Event Coordinator",
  },
];

function SimpleCardSection({ id, title, items }: { id: string; title: string; items: string[] }) {
  return (
    <section id={id} className="mx-auto w-full max-w-6xl px-6 py-14">
      <h2 className="text-2xl font-semibold text-zinc-900">{title}</h2>
      <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {items.map((item) => (
          <article key={item} className="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm">
            <p className="text-sm leading-6 text-zinc-700">{item}</p>
          </article>
        ))}
      </div>
    </section>
  );
}

export default function HomePage() {
  return (
    <main className="min-h-screen bg-zinc-50">
      <PublicNavbar />
      <HeroSection />

      <section className="mx-auto w-full max-w-6xl px-6 py-10">
        <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
          {trustBadges.map((badge) => (
            <p
              key={badge}
              className="rounded-full border border-emerald-200 bg-emerald-50 px-4 py-2 text-center text-xs font-semibold uppercase tracking-wide text-emerald-700"
            >
              {badge}
            </p>
          ))}
        </div>
      </section>

      <SimpleCardSection id="kenapa-kami" title="Kenapa Memilih Kami" items={reasons} />

      <section id="program" className="mx-auto w-full max-w-6xl px-6 py-14">
        <h2 className="text-2xl font-semibold text-zinc-900">Program Utama</h2>
        <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {programs.map((program) => (
            <ProgramCard key={program.title} {...program} />
          ))}
        </div>
      </section>

      <section id="progres" className="mx-auto w-full max-w-6xl px-6 py-14">
        <h2 className="text-2xl font-semibold text-zinc-900">Preview Progress Anak</h2>
        <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {progressPreviews.map((preview) => (
            <ProgressPreviewCard key={preview.studentName} {...preview} />
          ))}
        </div>
      </section>

      <SimpleCardSection id="kegiatan-pushbike" title="Kegiatan Pushbike" items={pushbikeActivities} />
      <SimpleCardSection id="kegiatan-belajar" title="Kegiatan Belajar Anak" items={learningActivities} />
      <SimpleCardSection id="event-balapan" title="Event Balapan" items={raceEvents} />

      <section id="testimoni" className="mx-auto w-full max-w-6xl px-6 py-14">
        <h2 className="text-2xl font-semibold text-zinc-900">Testimoni</h2>
        <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {testimonials.map((item) => (
            <TestimonialCard key={item.name} {...item} />
          ))}
        </div>
      </section>

      <FaqSection />
      <CtaSection />
      <PublicFooter />
    </main>
  );
}
