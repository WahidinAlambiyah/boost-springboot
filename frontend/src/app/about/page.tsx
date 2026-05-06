import PublicPageLayout from "@/app/(public)/components/public-page-layout";
import { aboutContent } from "@/content/public/site";

export default function AboutPage() {
  return (
    <PublicPageLayout>
      <section className="container mx-auto w-full space-y-8 px-6 py-16">
        <div>
          <h1 className="text-3xl font-semibold text-zinc-900">{aboutContent.title}</h1>
          <p className="mt-4 max-w-3xl text-zinc-700">{aboutContent.intro}</p>
        </div>

        <div>
          <h2 className="text-2xl font-semibold text-zinc-900">{aboutContent.missionTitle}</h2>
          <p className="mt-3 max-w-3xl text-zinc-700">{aboutContent.mission}</p>
        </div>

        <div>
          <h2 className="text-2xl font-semibold text-zinc-900">{aboutContent.valuesTitle}</h2>
          <ul className="mt-3 list-disc space-y-2 pl-6 text-zinc-700">
            {aboutContent.values.map((value) => (
              <li key={value}>{value}</li>
            ))}
          </ul>
        </div>

        <div>
          <h2 className="text-2xl font-semibold text-zinc-900">{aboutContent.progressTitle}</h2>
          <p className="mt-3 max-w-3xl text-zinc-700">{aboutContent.progressReason}</p>
        </div>
      </section>
    </PublicPageLayout>
  );
}
