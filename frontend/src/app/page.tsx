import CtaSection from "@/app/(public)/components/cta-section";
import FaqSection from "@/app/(public)/components/faq-section";
import HeroSection from "@/app/(public)/components/hero-section";
import ProgramCard from "@/app/(public)/components/program-card";
import ProgressPreviewCard from "@/app/(public)/components/progress-preview-card";
import PublicFooter from "@/app/(public)/components/public-footer";
import PublicNavbar from "@/app/(public)/components/public-navbar";
import TestimonialCard from "@/app/(public)/components/testimonial-card";

const trustBadges = ["1200+ siswa aktif", "35+ coach bersertifikat", "4.9/5 rating orang tua"];

const reasons = [
  "Kurikulum terstruktur sesuai usia dan level anak",
  "Laporan progres mingguan dengan insight pelatih",
  "Komunikasi orang tua dan coach lebih cepat",
];

const programs = [
  {
    title: "Balance Starter",
    level: "Usia 3-5 tahun",
    description: "Fokus koordinasi dasar, keseimbangan, dan fun games untuk membangun rasa percaya diri.",
  },
  {
    title: "Skill Builder",
    level: "Usia 6-8 tahun",
    description: "Latihan teknik tikungan, pengereman, dan start dengan metode belajar bertahap.",
  },
  {
    title: "Race Preparation",
    level: "Usia 9+ tahun",
    description: "Pendampingan intensif untuk target kompetisi dengan evaluasi performa tiap sesi.",
  },
];

const progressPreviews = [
  { studentName: "Aira (6)", consistency: "88%", milestone: "Stabil cornering level 2" },
  { studentName: "Raka (7)", consistency: "91%", milestone: "Start gate reaction meningkat" },
  { studentName: "Nala (5)", consistency: "84%", milestone: "Berani track mini tanpa bantuan" },
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
        <div className="grid gap-3 sm:grid-cols-3">
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
