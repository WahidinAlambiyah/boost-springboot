import Link from "next/link";

import { footerLinks, siteBrand, siteContact } from "@/content/public/site";
import { createWhatsAppLink } from "@/lib/whatsapp";

export default function PublicFooter() {
  return (
    <footer className="border-t border-zinc-200 bg-white">
      <div className="layout-container grid gap-10 py-12 sm:grid-cols-2 lg:grid-cols-3">
        <div>
          <p className="text-lg font-semibold text-zinc-900">{siteBrand.name}</p>
          <p className="text-body mt-3 max-w-sm">{siteBrand.footerDescription}</p>
        </div>
        <div>
          <p className="text-sm font-semibold uppercase tracking-wide text-zinc-500">Link Penting</p>
          <nav className="mt-3 grid gap-2" aria-label="Footer navigation">
            {footerLinks.map((item) => (
              <Link
                key={item.href}
                href={item.href}
                className="text-sm font-medium text-zinc-600 transition hover:text-zinc-900"
              >
                {item.label}
              </Link>
            ))}
          </nav>
        </div>
        <div className="sm:justify-self-end lg:justify-self-auto">
          <p className="text-sm font-semibold uppercase tracking-wide text-zinc-500">Kontak</p>
          <p className="mt-3 text-sm text-zinc-600">{siteContact.email}</p>
          <p className="mt-1 text-sm text-zinc-600">{siteContact.phone}</p>
          <a
            href={createWhatsAppLink(siteContact.whatsappMessage)}
            className="mt-4 inline-flex rounded-full border border-emerald-200 bg-emerald-50 px-4 py-2 text-sm font-semibold text-emerald-700 transition hover:bg-emerald-100"
          >
            Konsultasi WhatsApp
          </a>
        </div>
      </div>
    </footer>
  );
}
