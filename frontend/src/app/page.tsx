import CtaSection from "@/app/(public)/components/cta-section";
import EmptyImagePlaceholder from "@/app/(public)/components/empty-image-placeholder";
import FaqSection from "@/app/(public)/components/faq-section";
import HeroSection from "@/app/(public)/components/hero-section";
import ProgramCard from "@/app/(public)/components/program-card";
import ProgressPreviewCard from "@/app/(public)/components/progress-preview-card";
import PublicPageLayout from "@/app/(public)/components/public-page-layout";
import TestimonialCard from "@/app/(public)/components/testimonial-card";

const trustBadges = ["Untuk usia 2–7 tahun", "Didampingi coach berpengalaman", "Progress anak tercatat", "Laporan untuk orang tua"];
const reasons = [
  "Kurikulum terstruktur sesuai usia dan level anak",
  "Laporan progres mingguan dengan insight coach",
  "Komunikasi orang tua dan coach lebih cepat",
];
const programs = [
  { title: "Pushbike Class", level: "Program utama", description: "Latihan keseimbangan, kontrol arah, dan teknik dasar pushbike dengan pendekatan fun learning." },
  { title: "Kegiatan Belajar Anak", level: "Program utama", description: "Aktivitas belajar terarah untuk melatih fokus, motorik, dan kerja sama anak." },
  { title: "Event & Race", level: "Program utama", description: "Sesi event ramah anak untuk membangun keberanian, sportivitas, dan pengalaman kompetisi positif." },
];
const trialSteps = ["Pilih program", "Konsultasi via WhatsApp", "Ikut trial class", "Anak dinilai oleh coach", "Orang tua mendapat ringkasan progress"];
const coachNotes = ["Kehadiran", "Skill pushbike", "Fokus dan disiplin", "Keberanian", "Rekomendasi latihan berikutnya"];
const audience = [
  "Anak usia 2–7 tahun",
  "Anak yang aktif bergerak",
  "Anak yang ingin lebih percaya diri",
  "Orang tua yang ingin progress anak lebih terpantau",
];

const progressPreviews = [{ studentName: "Rafa", level: "Beginner", balance: "4/5", braking: "3/5", confidence: "4/5", coachNote: "Rafa menunjukkan progres baik di lintasan lurus dan mulai berani mengambil tikungan. Lanjutkan latihan braking bertahap agar kontrol kecepatan makin stabil." }];

const testimonials = [
  { quote: "Anak jadi lebih disiplin latihan dan kami selalu update progresnya tiap minggu.", name: "Dina P.", role: "Orang Tua Siswa" },
  { quote: "Manajemen kelas jauh lebih rapi, absensi dan evaluasi coach bisa langsung dipantau.", name: "Coach Ario", role: "Head Coach" },
  { quote: "Event balapan terasa lebih tertata karena semua data peserta dan jadwal sudah terintegrasi.", name: "Nadya R.", role: "Event Coordinator" },
];

function ChecklistSection({ id, title, items }: { id: string; title: string; items: string[] }) {
  return (
    <section id={id} className="container mx-auto w-full px-6 py-14">
      <h2 className="text-2xl font-semibold text-zinc-900">{title}</h2>
      <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {items.map((item) => (
          <article key={item} className="rounded-2xl border border-zinc-200/80 bg-white p-5 shadow-sm">
            <p className="text-sm font-medium text-zinc-800">{item}</p>
          </article>
        ))}
      </div>
    </section>
  );
}

export default function HomePage() {
  return (
    <PublicPageLayout>
      <HeroSection />

      <section className="container mx-auto w-full px-6 py-10">
        <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
          {trustBadges.map((badge) => (
            <p key={badge} className="rounded-full border border-emerald-200 bg-emerald-50 px-4 py-2 text-center text-xs font-semibold uppercase tracking-wide text-emerald-700">{badge}</p>
          ))}
        </div>
      </section>

      <ChecklistSection id="kenapa-kami" title="Kenapa Memilih Kami" items={reasons} />

      <section id="program" className="container mx-auto w-full px-6 py-14">
        <h2 className="text-2xl font-semibold text-zinc-900">Program Utama</h2>
        <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {programs.map((program) => (<ProgramCard key={program.title} {...program} />))}
        </div>
      </section>

      <section id="alur-trial" className="container mx-auto w-full px-6 py-14">
        <h2 className="text-2xl font-semibold text-zinc-900">Bagaimana Alur Ikut Trial</h2>
        <ol className="mt-6 grid gap-4 md:grid-cols-2 lg:grid-cols-5">
          {trialSteps.map((step, index) => (
            <li key={step} className="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm">
              <p className="text-xs font-semibold uppercase tracking-wide text-emerald-700">Step {index + 1}</p>
              <p className="mt-2 text-sm font-medium text-zinc-800">{step}</p>
            </li>
          ))}
        </ol>
      </section>

      <ChecklistSection id="catatan-coach" title="Yang Dicatat Coach" items={coachNotes} />
      <ChecklistSection id="untuk-siapa" title="Untuk Siapa Program Ini" items={audience} />

      <section id="progres" className="container mx-auto w-full px-6 py-14">
        <h2 className="text-2xl font-semibold text-zinc-900">Preview Progress Anak</h2>
        <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {progressPreviews.map((preview) => (<ProgressPreviewCard key={preview.studentName} {...preview} />))}
        </div>
      </section>

      <section id="galeri-preview" className="container mx-auto w-full px-6 py-14">
        <h2 className="text-2xl font-semibold text-zinc-900">Preview Aktivitas Anak</h2>
        <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <EmptyImagePlaceholder category="Pushbike" title="Drill start gate dan latihan tikungan" />
          <EmptyImagePlaceholder category="Belajar" title="Aktivitas fokus, motorik, dan koordinasi" />
          <EmptyImagePlaceholder category="Event" title="Fun race ramah anak bersama orang tua" />
        </div>
      </section>

      <section id="testimoni" className="container mx-auto w-full px-6 py-14">
        <h2 className="text-2xl font-semibold text-zinc-900">Testimoni</h2>
        <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {testimonials.map((item) => (<TestimonialCard key={item.name} {...item} />))}
        </div>
      </section>

      <FaqSection />
      <CtaSection />
    </PublicPageLayout>
  );
}
