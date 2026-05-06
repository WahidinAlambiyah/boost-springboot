import Link from "next/link";

import PublicPageLayout from "@/app/(public)/components/public-page-layout";
import { learningProgramContent, programDetailPageContent } from "@/content/public/programs";

export default function KelasBelajarPage() {
  return (
    <PublicPageLayout>
      <section className="container mx-auto w-full px-6 py-16">
        <h1 className="text-3xl font-semibold text-zinc-900">{programDetailPageContent.learning.title}</h1>

        <div className="mt-8 grid gap-8 lg:grid-cols-2">
          <div>
            <h2 className="text-xl font-semibold text-zinc-900">{programDetailPageContent.learning.classesTitle}</h2>
            <ul className="mt-4 list-disc space-y-2 pl-6 text-zinc-700">
              {learningProgramContent.classes.map((kelas) => (
                <li key={kelas}>{kelas}</li>
              ))}
            </ul>
          </div>

          <div>
            <h2 className="text-xl font-semibold text-zinc-900">{programDetailPageContent.learning.benefitsTitle}</h2>
            <p className="mt-4 text-zinc-700">{programDetailPageContent.learning.benefitsDescription}</p>
          </div>
        </div>

        <div className="mt-10 rounded-xl border border-zinc-200 bg-zinc-50 p-6">
          <h2 className="text-xl font-semibold text-zinc-900">{programDetailPageContent.learning.scheduleTitle}</h2>
          <ul className="mt-4 space-y-2 text-zinc-700">
            {learningProgramContent.schedules.map((schedule) => (
              <li key={schedule}>{schedule}</li>
            ))}
          </ul>
        </div>

        <div className="mt-10">
          <Link
            href={programDetailPageContent.learning.ctaHref}
            className="inline-flex items-center rounded-lg bg-teal-600 px-5 py-3 text-sm font-semibold text-white transition hover:bg-teal-700"
          >
            {programDetailPageContent.learning.ctaLabel}
          </Link>
        </div>
      </section>
    </PublicPageLayout>
  );
}
