import PublicPageLayout from "@/app/(public)/components/public-page-layout";
import { pushbikeFaqs } from "@/content/public/faqs";
import { pushbikeProgramContent } from "@/content/public/programs";

export default function PushbikeProgramPage() {
  return (
    <PublicPageLayout>
      <section className="container mx-auto w-full px-6 py-16">
        <h1 className="text-3xl font-semibold text-zinc-900">Program Pushbike</h1>
        <p className="mt-4 max-w-3xl text-zinc-700">
          Program pushbike adalah latihan sepeda tanpa pedal untuk anak usia dini yang berfokus pada koordinasi gerak
          dan kontrol tubuh. Melalui sesi fun-drill dan game, anak belajar teknik dasar berkendara secara bertahap,
          aman, dan menyenangkan.
        </p>

        <div className="mt-6 grid gap-4 sm:grid-cols-3">
          {pushbikeProgramContent.benefits.map((benefit) => (
            <article key={benefit} className="rounded-2xl border border-zinc-200 bg-white p-4 shadow-sm">
              <p className="text-sm font-medium text-zinc-700">{benefit}</p>
            </article>
          ))}
        </div>

        <section className="mt-12">
          <h2 className="text-2xl font-semibold text-zinc-900">Level Kelas</h2>
          <div className="mt-5 grid gap-4 sm:grid-cols-3">
            {pushbikeProgramContent.levels.map((level) => (
              <article key={level} className="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm">
                <h3 className="text-base font-semibold text-zinc-900">{level}</h3>
              </article>
            ))}
          </div>
        </section>

        <section className="mt-12">
          <h2 className="text-2xl font-semibold text-zinc-900">Skill yang Dinilai</h2>
          <div className="mt-5 grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
            {pushbikeProgramContent.assessedSkills.map((skill) => (
              <div key={skill} className="rounded-xl border border-zinc-200 bg-white px-4 py-3 text-sm text-zinc-700 shadow-sm">
                {skill}
              </div>
            ))}
          </div>
        </section>

        <section className="mt-12">
          <h2 className="text-2xl font-semibold text-zinc-900">Jadwal Trial (Dummy)</h2>
          <div className="mt-5 hidden overflow-x-auto rounded-2xl border border-zinc-200 bg-white shadow-sm md:block">
            <table className="min-w-full text-sm">
              <thead className="bg-zinc-50 text-left text-zinc-600">
                <tr>
                  <th className="px-4 py-3 font-semibold">Hari</th>
                  <th className="px-4 py-3 font-semibold">Jam</th>
                  <th className="px-4 py-3 font-semibold">Level</th>
                  <th className="px-4 py-3 font-semibold">Lokasi</th>
                </tr>
              </thead>
              <tbody>
                {pushbikeProgramContent.schedules.map((row) => (
                  <tr key={`${row.day}-${row.level}`} className="border-t border-zinc-100 text-zinc-700">
                    <td className="px-4 py-3">{row.day}</td>
                    <td className="px-4 py-3">{row.time}</td>
                    <td className="px-4 py-3">{row.level}</td>
                    <td className="px-4 py-3">{row.location}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="mt-5 grid gap-3 md:hidden">
            {pushbikeProgramContent.schedules.map((row) => (
              <article key={`${row.day}-${row.level}-card`} className="rounded-2xl border border-zinc-200 bg-white p-4 shadow-sm">
                <p className="text-sm font-semibold text-zinc-900">{row.day}</p>
                <p className="mt-1 text-sm text-zinc-700">{row.time}</p>
                <p className="mt-1 text-sm text-zinc-700">{row.level}</p>
                <p className="mt-1 text-sm text-zinc-600">{row.location}</p>
              </article>
            ))}
          </div>
        </section>

        <section className="mt-12">
          <h2 className="text-2xl font-semibold text-zinc-900">FAQ Pushbike</h2>
          <div className="mt-5 space-y-4">
            {pushbikeFaqs.map((faq) => (
              <article key={faq.id} className="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm">
                <h3 className="text-sm font-semibold text-zinc-900">{faq.question}</h3>
                <p className="mt-2 text-sm leading-6 text-zinc-600">{faq.answer}</p>
              </article>
            ))}
          </div>
        </section>

        <div className="mt-12">
          <a
            href="/contact"
            className="inline-flex w-full items-center justify-center rounded-xl bg-emerald-600 px-5 py-3 text-sm font-semibold text-white transition hover:bg-emerald-700 sm:w-auto"
          >
            Daftar Trial
          </a>
        </div>
      </section>
    </PublicPageLayout>
  );
}
