import Link from "next/link";

import PublicPageLayout from "@/app/(public)/components/public-page-layout";
import { trialFormFields, trialPageContent, trialPageSteps } from "@/content/public/hero";
import { createWhatsAppLink } from "@/lib/whatsapp";

export default function TrialPage() {
  return (
    <PublicPageLayout>
      <section className="container mx-auto w-full px-6 py-16">
        <div className="grid gap-10 lg:grid-cols-[1.05fr_0.95fr] lg:items-start">
          <div>
            <p className="inline-flex rounded-full border border-emerald-200 bg-emerald-50 px-3 py-1 text-xs font-semibold uppercase tracking-wide text-emerald-700">
              {trialPageContent.eyebrow}
            </p>
            <h1 className="mt-5 text-3xl font-semibold leading-tight tracking-tight text-zinc-900 sm:text-4xl">
              {trialPageContent.title}
            </h1>
            <p className="mt-5 max-w-2xl text-base leading-7 text-zinc-700">
              {trialPageContent.description}
            </p>

            <div className="mt-8 flex flex-col gap-3 sm:flex-row sm:flex-wrap">
              <a
                href={createWhatsAppLink(trialPageContent.whatsappMessage)}
                className="rounded-full bg-emerald-600 px-5 py-3 text-center text-sm font-semibold text-white transition hover:bg-emerald-700"
              >
                {trialPageContent.whatsappLabel}
              </a>
              <Link
                href={trialPageContent.programHref}
                className="rounded-full border border-zinc-300 bg-white px-5 py-3 text-center text-sm font-semibold text-zinc-700 transition hover:bg-zinc-100"
              >
                {trialPageContent.programLabel}
              </Link>
            </div>

            <div className="mt-10 rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm">
              <h2 className="text-xl font-semibold text-zinc-900">{trialPageContent.nextStepsTitle}</h2>
              <ol className="mt-5 space-y-4 text-sm leading-6 text-zinc-700">
                {trialPageSteps.map((step, index) => (
                  <li key={step} className="flex gap-3">
                    <span className="flex h-7 w-7 shrink-0 items-center justify-center rounded-full bg-emerald-100 text-sm font-semibold text-emerald-700">
                      {index + 1}
                    </span>
                    <span>{step}</span>
                  </li>
                ))}
              </ol>
            </div>
          </div>

          <form className="rounded-3xl border border-zinc-200 bg-white p-6 shadow-sm sm:p-8">
            <h2 className="text-2xl font-semibold text-zinc-900">{trialPageContent.formTitle}</h2>
            <p className="mt-2 text-sm leading-6 text-zinc-600">
              {trialPageContent.formDescription}
            </p>

            <div className="mt-6 grid gap-5">
              <label className="grid gap-2 text-sm font-medium text-zinc-700">
                {trialFormFields.parentName.label}
                <input
                  type="text"
                  name="parentName"
                  placeholder={trialFormFields.parentName.placeholder}
                  className="rounded-xl border border-zinc-300 px-4 py-3 text-sm font-normal text-zinc-900 outline-none transition placeholder:text-zinc-400 focus:border-emerald-500 focus:ring-2 focus:ring-emerald-100"
                />
              </label>

              <label className="grid gap-2 text-sm font-medium text-zinc-700">
                {trialFormFields.childName.label}
                <input
                  type="text"
                  name="childName"
                  placeholder={trialFormFields.childName.placeholder}
                  className="rounded-xl border border-zinc-300 px-4 py-3 text-sm font-normal text-zinc-900 outline-none transition placeholder:text-zinc-400 focus:border-emerald-500 focus:ring-2 focus:ring-emerald-100"
                />
              </label>

              <label className="grid gap-2 text-sm font-medium text-zinc-700">
                {trialFormFields.childAge.label}
                <select
                  name="childAge"
                  defaultValue=""
                  className="rounded-xl border border-zinc-300 px-4 py-3 text-sm font-normal text-zinc-900 outline-none transition focus:border-emerald-500 focus:ring-2 focus:ring-emerald-100"
                >
                  <option value="" disabled>
                    {trialFormFields.childAge.placeholder}
                  </option>
                  {trialFormFields.childAge.options.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </label>

              <label className="grid gap-2 text-sm font-medium text-zinc-700">
                {trialFormFields.preferredSchedule.label}
                <input
                  type="text"
                  name="preferredSchedule"
                  placeholder={trialFormFields.preferredSchedule.placeholder}
                  className="rounded-xl border border-zinc-300 px-4 py-3 text-sm font-normal text-zinc-900 outline-none transition placeholder:text-zinc-400 focus:border-emerald-500 focus:ring-2 focus:ring-emerald-100"
                />
              </label>

              <label className="grid gap-2 text-sm font-medium text-zinc-700">
                {trialFormFields.notes.label}
                <textarea
                  name="notes"
                  rows={4}
                  placeholder={trialFormFields.notes.placeholder}
                  className="rounded-xl border border-zinc-300 px-4 py-3 text-sm font-normal text-zinc-900 outline-none transition placeholder:text-zinc-400 focus:border-emerald-500 focus:ring-2 focus:ring-emerald-100"
                />
              </label>
            </div>

            <div className="mt-7 flex flex-col gap-3 sm:flex-row sm:flex-wrap">
              <button
                type="button"
                className="rounded-full bg-zinc-900 px-5 py-3 text-sm font-semibold text-white transition hover:bg-zinc-700"
              >
                {trialPageContent.submitLabel}
              </button>
              <a
                href={createWhatsAppLink(trialPageContent.confirmMessage)}
                className="rounded-full border border-emerald-600 px-5 py-3 text-center text-sm font-semibold text-emerald-700 transition hover:bg-emerald-50"
              >
                {trialPageContent.confirmLabel}
              </a>
            </div>
          </form>
        </div>
      </section>
    </PublicPageLayout>
  );
}
