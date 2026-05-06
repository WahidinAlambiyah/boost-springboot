import PublicPageLayout from "@/app/(public)/components/public-page-layout";
import { pricingNote, pricingPackages, pricingPageContent } from "@/content/public/pricing";

export default function PricingPage() {
  return (
    <PublicPageLayout>
      <section className="container mx-auto w-full px-6 py-16">
        <h1 className="text-3xl font-semibold text-zinc-900">{pricingPageContent.title}</h1>
        <p className="mt-4 max-w-2xl text-zinc-700">{pricingPageContent.description}</p>
        <ul className="mt-6 list-disc space-y-2 pl-6 text-zinc-800">
          {pricingPackages.map((item) => (
            <li key={item}>{item}</li>
          ))}
        </ul>
        <p className="mt-6 text-sm text-zinc-600">{pricingNote}</p>
      </section>
    </PublicPageLayout>
  );
}
