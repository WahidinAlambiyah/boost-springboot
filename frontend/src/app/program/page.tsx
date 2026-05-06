import PublicPageLayout from "@/app/(public)/components/public-page-layout";
import { programHighlights } from "@/content/public/programs";

type ProgramOverviewCardProps = {
  title: string;
  description: string;
};

function ProgramOverviewCard({ title, description }: ProgramOverviewCardProps) {
  return (
    <article className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm">
      <h3 className="text-lg font-semibold text-zinc-900">{title}</h3>
      <p className="mt-3 text-sm leading-6 text-zinc-600">{description}</p>
    </article>
  );
}

export default function ProgramPage() {
  return (
    <PublicPageLayout>
      <section className="container mx-auto w-full px-6 py-16">
        <h1 className="text-3xl font-semibold text-zinc-900">Overview Program</h1>
        <p className="mt-4 max-w-2xl text-zinc-700">
          Pilih program yang dirancang untuk mendukung perkembangan keterampilan, fokus belajar, dan pengalaman seru
          anak.
        </p>

        <div className="mt-10 grid gap-5 md:grid-cols-3">
          {programHighlights.map((program) => (
            <ProgramOverviewCard key={program.title} title={program.title} description={program.description} />
          ))}
        </div>

        <div className="mt-10">
          <a
            href="/contact"
            className="inline-flex items-center rounded-xl bg-emerald-600 px-5 py-3 text-sm font-semibold text-white transition hover:bg-emerald-700"
          >
            Daftar Trial
          </a>
        </div>
      </section>
    </PublicPageLayout>
  );
}
