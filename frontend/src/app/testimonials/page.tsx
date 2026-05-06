import PublicPageLayout from "@/app/(public)/components/public-page-layout";
import { testimonialHighlightPoints, testimonialsPageContent } from "@/content/public/testimonials";

export default function TestimonialsPage() {
  return (
    <PublicPageLayout>
      <section className="container mx-auto w-full px-6 py-16">
        <h1 className="text-3xl font-semibold text-zinc-900">{testimonialsPageContent.title}</h1>
        <p className="mt-4 max-w-2xl text-zinc-700">{testimonialsPageContent.description}</p>

        <div className="mt-10 grid gap-6 lg:grid-cols-2">
          <article className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm">
            <h2 className="text-xl font-semibold text-zinc-900">{testimonialsPageContent.parentSectionTitle}</h2>
            <p className="mt-3 text-zinc-700">{testimonialsPageContent.parentSectionDescription}</p>
          </article>

          <article className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm">
            <h2 className="text-xl font-semibold text-zinc-900">{testimonialsPageContent.coachSectionTitle}</h2>
            <p className="mt-3 text-zinc-700">{testimonialsPageContent.coachSectionDescription}</p>
          </article>
        </div>

        <article className="mt-8 rounded-2xl border border-zinc-200 bg-zinc-50 p-6">
          <h2 className="text-xl font-semibold text-zinc-900">{testimonialsPageContent.progressSectionTitle}</h2>
          <p className="mt-3 text-zinc-700">{testimonialsPageContent.progressSectionDescription}</p>

          <ul className="mt-4 grid gap-3 sm:grid-cols-2">
            {testimonialHighlightPoints.map((point) => (
              <li
                key={point}
                className="rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm font-medium text-emerald-800"
              >
                {point}
              </li>
            ))}
          </ul>
        </article>
      </section>
    </PublicPageLayout>
  );
}
