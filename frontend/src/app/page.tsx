import CtaSection from "@/app/(public)/components/cta-section";
import EmptyImagePlaceholder from "@/app/(public)/components/empty-image-placeholder";
import FaqSection from "@/app/(public)/components/faq-section";
import HeroSection from "@/app/(public)/components/hero-section";
import ProgramCard from "@/app/(public)/components/program-card";
import ProgressPreviewCard from "@/app/(public)/components/progress-preview-card";
import PublicPageLayout from "@/app/(public)/components/public-page-layout";
import TestimonialCard from "@/app/(public)/components/testimonial-card";

import {
  galleryPreviewItems,
  progressPreviews,
  reasons,
  trialSteps,
  trustBadges,
} from "@/content/public/hero";
import { audience, coachNotes, programs } from "@/content/public/programs";
import { testimonials } from "@/content/public/testimonials";

function ChecklistSection({ id, title, items }: { id: string; title: string; items: readonly string[] }) {
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
          {galleryPreviewItems.map((item) => (
            <EmptyImagePlaceholder key={item.title} category={item.category} title={item.title} />
          ))}
        </div>
      </section>

      <section id="testimoni" className="container mx-auto w-full px-6 py-14">
        <h2 className="text-2xl font-semibold text-zinc-900">Testimoni</h2>
        <div className="mt-6 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {testimonials.map((item) => (<TestimonialCard key={item.id} {...item} />))}
        </div>
      </section>

      <FaqSection />
      <CtaSection />
    </PublicPageLayout>
  );
}
