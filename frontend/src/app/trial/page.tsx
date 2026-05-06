import Link from "next/link";

import PublicPageLayout from "@/app/(public)/components/public-page-layout";
import { trialPageSteps } from "@/content/public/hero";
import { createWhatsAppLink } from "@/lib/whatsapp";

export default function TrialPage() {
  return (
    <PublicPageLayout>
      <section className="container mx-auto w-full px-6 py-16">
        <div className="grid gap-10 lg:grid-cols-[1.05fr_0.95fr] lg:items-start">
          <div>
            <p className="inline-flex rounded-full border border-emerald-200 bg-emerald-50 px-3 py-1 text-xs font-semibold uppercase tracking-wide text-emerald-700">
              Trial Class Pushbike
            </p>
            <h1 className="mt-5 text-3xl font-semibold leading-tight tracking-tight text-zinc-900 sm:text-4xl">
              Jadwalkan trial class yang nyaman untuk anak dan orang tua
            </h1>
            <p className="mt-5 max-w-2xl text-base leading-7 text-zinc-700">
              Halaman ini berisi form statis untuk membantu orang tua menyiapkan data awal sebelum tim Boost Academy menghubungi kembali. Trial dirancang fun, aman, dan fokus pada keberanian, keseimbangan, serta rasa percaya diri anak.
            </p>

            <div className="mt-8 flex flex-col gap-3 sm:flex-row sm:flex-wrap">
              <a
                href={createWhatsAppLink("Halo Boost Academy, saya ingin menjadwalkan trial class untuk anak.")}
                className="rounded-full bg-emerald-600 px-5 py-3 text-center text-sm font-semibold text-white transition hover:bg-emerald-700"
              >
                Chat WhatsApp untuk Jadwal
              </a>
              <Link
                href="/program"
                className="rounded-full border border-zinc-300 bg-white px-5 py-3 text-center text-sm font-semibold text-zinc-700 transition hover:bg-zinc-100"
              >
                Lihat Program Dulu
              </Link>
            </div>

            <div className="mt-10 rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm">
              <h2 className="text-xl font-semibold text-zinc-900">Apa yang terjadi setelah daftar?</h2>
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
            <h2 className="text-2xl font-semibold text-zinc-900">Form minat trial</h2>
            <p className="mt-2 text-sm leading-6 text-zinc-600">
              Dummy/static form untuk mencatat kebutuhan keluarga sebelum konfirmasi via WhatsApp.
            </p>

            <div className="mt-6 grid gap-5">
              <label className="grid gap-2 text-sm font-medium text-zinc-700">
                Nama orang tua
                <input
                  type="text"
                  name="parentName"
                  placeholder="Contoh: Ayu Pratama"
                  className="rounded-xl border border-zinc-300 px-4 py-3 text-sm font-normal text-zinc-900 outline-none transition placeholder:text-zinc-400 focus:border-emerald-500 focus:ring-2 focus:ring-emerald-100"
                />
              </label>

              <label className="grid gap-2 text-sm font-medium text-zinc-700">
                Nama anak
                <input
                  type="text"
                  name="childName"
                  placeholder="Contoh: Raka"
                  className="rounded-xl border border-zinc-300 px-4 py-3 text-sm font-normal text-zinc-900 outline-none transition placeholder:text-zinc-400 focus:border-emerald-500 focus:ring-2 focus:ring-emerald-100"
                />
              </label>

              <label className="grid gap-2 text-sm font-medium text-zinc-700">
                Usia anak
                <select
                  name="childAge"
                  defaultValue=""
                  className="rounded-xl border border-zinc-300 px-4 py-3 text-sm font-normal text-zinc-900 outline-none transition focus:border-emerald-500 focus:ring-2 focus:ring-emerald-100"
                >
                  <option value="" disabled>
                    Pilih usia
                  </option>
                  <option value="2-3">2–3 tahun</option>
                  <option value="4-5">4–5 tahun</option>
                  <option value="6-7">6–7 tahun</option>
                </select>
              </label>

              <label className="grid gap-2 text-sm font-medium text-zinc-700">
                Preferensi jadwal
                <input
                  type="text"
                  name="preferredSchedule"
                  placeholder="Contoh: Sabtu pagi"
                  className="rounded-xl border border-zinc-300 px-4 py-3 text-sm font-normal text-zinc-900 outline-none transition placeholder:text-zinc-400 focus:border-emerald-500 focus:ring-2 focus:ring-emerald-100"
                />
              </label>

              <label className="grid gap-2 text-sm font-medium text-zinc-700">
                Catatan untuk coach
                <textarea
                  name="notes"
                  rows={4}
                  placeholder="Contoh: Anak baru pertama kali mencoba pushbike."
                  className="rounded-xl border border-zinc-300 px-4 py-3 text-sm font-normal text-zinc-900 outline-none transition placeholder:text-zinc-400 focus:border-emerald-500 focus:ring-2 focus:ring-emerald-100"
                />
              </label>
            </div>

            <div className="mt-7 flex flex-col gap-3 sm:flex-row sm:flex-wrap">
              <button
                type="button"
                className="rounded-full bg-zinc-900 px-5 py-3 text-sm font-semibold text-white transition hover:bg-zinc-700"
              >
                Simpan Minat Trial
              </button>
              <a
                href={createWhatsAppLink("Halo Boost Academy, saya sudah mengisi minat trial dan ingin konfirmasi jadwal.")}
                className="rounded-full border border-emerald-600 px-5 py-3 text-center text-sm font-semibold text-emerald-700 transition hover:bg-emerald-50"
              >
                Konfirmasi via WhatsApp
              </a>
            </div>
          </form>
        </div>
      </section>
    </PublicPageLayout>
  );
}
