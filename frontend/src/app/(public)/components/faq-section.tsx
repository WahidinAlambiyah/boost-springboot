import { faqGroups } from "@/content/public/faqs";

export default function FaqSection() {
  return (
    <section id="faq" className="layout-container section-space">
      <h2 className="text-2xl font-semibold tracking-tight text-zinc-900">Pertanyaan Umum Orang Tua</h2>
      <div className="mt-8 space-y-8">
        {faqGroups.map((group) => (
          <div key={group.id}>
            <h3 className="text-sm font-semibold uppercase tracking-wide text-emerald-700">{group.label}</h3>
            <div className="mt-4 grid gap-4 md:grid-cols-2">
              {group.items.map((item) => (
                <article key={item.id} className="surface-card p-6">
                  <h4 className="text-sm font-semibold text-zinc-900">{item.question}</h4>
                  <p className="text-body mt-2">{item.answer}</p>
                </article>
              ))}
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}
