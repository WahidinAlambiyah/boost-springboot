import Link from "next/link";

import { trialCtaCopy } from "@/content/public/hero";
import { createWhatsAppLink } from "@/lib/whatsapp";

export default function CtaSection() {
  return (
    <section className="layout-container section-space pb-16">
      <div className="overflow-hidden rounded-3xl border border-zinc-700/40 bg-gradient-to-r from-slate-900 via-zinc-900 to-emerald-900 p-7 sm:p-10">
        <h2 className="text-2xl font-semibold tracking-tight text-white sm:text-3xl">Siap coba trial class untuk anak Anda?</h2>
        <p className="mt-3 max-w-2xl text-sm leading-6 text-zinc-200">
          Pilih program yang cocok, konsultasi singkat via WhatsApp, lalu jadwalkan trial class bersama coach.
        </p>
        <div className="mt-7 flex flex-col gap-3 sm:flex-row sm:flex-wrap">
          <Link href={trialCtaCopy.href} className="rounded-full bg-emerald-500 px-5 py-2.5 text-center text-sm font-semibold text-white transition hover:bg-emerald-400">
            {trialCtaCopy.sectionLabel}
          </Link>
          <a href={createWhatsAppLink(trialCtaCopy.whatsappMessage)} className="rounded-full border border-white/60 bg-transparent px-5 py-2.5 text-center text-sm font-semibold text-white transition hover:bg-white/10">
            {trialCtaCopy.whatsappLabel}
          </a>
          <a href={trialCtaCopy.programHref} className="rounded-full bg-zinc-700 px-5 py-2.5 text-center text-sm font-semibold text-white transition hover:bg-zinc-600">
            {trialCtaCopy.programLabel}
          </a>
        </div>
      </div>
    </section>
  );
}
