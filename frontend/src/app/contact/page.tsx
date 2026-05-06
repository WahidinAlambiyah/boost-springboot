import ContactForm from "@/app/(public)/components/contact-form";
import PublicPageLayout from "@/app/(public)/components/public-page-layout";
import { contactPageContent } from "@/content/public/contact";
import { siteContact } from "@/content/public/site";

export default function ContactPage() {
  return (
    <PublicPageLayout>
      <section className="container mx-auto w-full space-y-10 px-6 py-16">
        <div>
          <h1 className="text-3xl font-semibold text-zinc-900">{contactPageContent.title}</h1>
          <p className="mt-4 max-w-2xl text-zinc-700">{contactPageContent.description}</p>
        </div>

        <div className="grid gap-8 lg:grid-cols-2">
          <div className="space-y-6 rounded-xl border border-zinc-200 p-6">
            <div>
              <h2 className="text-xl font-semibold text-zinc-900">{contactPageContent.quickContactTitle}</h2>
              <p className="mt-2 text-zinc-700">{contactPageContent.quickContactDescription}</p>
              <a
                href={siteContact.whatsappUrl}
                target="_blank"
                rel="noreferrer"
                className="mt-4 inline-flex rounded-lg bg-emerald-600 px-4 py-2 font-medium text-white transition hover:bg-emerald-700"
              >
                Chat via WhatsApp
              </a>
            </div>

            <div>
              <h3 className="text-lg font-semibold text-zinc-900">Lokasi</h3>
              <p className="mt-2 text-zinc-700">{siteContact.location}</p>
            </div>

            <div>
              <h3 className="text-lg font-semibold text-zinc-900">Jam Operasional</h3>
              <ul className="mt-2 space-y-1 text-zinc-700">
                {siteContact.operationalHours.map((hour) => (
                  <li key={hour}>{hour}</li>
                ))}
              </ul>
            </div>

            <div>
              <h3 className="text-lg font-semibold text-zinc-900">Sosial Media</h3>
              <ul className="mt-2 space-y-1 text-zinc-700">
                {siteContact.socialMedia.map((item) => (
                  <li key={item.label}>
                    {item.label}: <a href={item.href} className="text-emerald-700 hover:underline">{item.handle}</a>
                  </li>
                ))}
              </ul>
            </div>
          </div>

          <ContactForm />
        </div>
      </section>
    </PublicPageLayout>
  );
}
