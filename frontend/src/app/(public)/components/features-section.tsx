import { featuresSectionContent } from "@/content/public/programs";

export default function FeaturesSection() {
  return (
    <section className="layout-container section-space">
      <h2 className="text-2xl font-semibold text-zinc-900">{featuresSectionContent.title}</h2>
      <div className="mt-6 grid gap-4 md:grid-cols-3">
        {featuresSectionContent.items.map((feature) => (
          <article key={feature} className="surface-card rounded-xl p-5">
            <p className="text-body text-zinc-700">{feature}</p>
          </article>
        ))}
      </div>
    </section>
  );
}
