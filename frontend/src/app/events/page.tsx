import PublicPageLayout from "@/app/(public)/components/public-page-layout";
import { groupedEvents, type EventItem } from "@/content/public/events";

function EventList({ title, items }: { title: string; items: EventItem[] }) {
  return (
    <div className="mt-10">
      <h2 className="text-2xl font-semibold text-zinc-900">{title}</h2>
      <div className="mt-4 overflow-x-auto rounded-xl border border-zinc-200 bg-white">
        <table className="min-w-full text-left text-sm">
          <thead className="bg-zinc-100 text-zinc-700">
            <tr>
              <th className="px-4 py-3 font-semibold">Title</th>
              <th className="px-4 py-3 font-semibold">Date</th>
              <th className="px-4 py-3 font-semibold">Location</th>
              <th className="px-4 py-3 font-semibold">Age Category</th>
              <th className="px-4 py-3 font-semibold">Quota</th>
              <th className="px-4 py-3 font-semibold">Status</th>
            </tr>
          </thead>
          <tbody>
            {items.map((event) => (
              <tr key={event.title} className="border-t border-zinc-200 text-zinc-800">
                <td className="px-4 py-3 font-medium">{event.title}</td>
                <td className="px-4 py-3">{event.date}</td>
                <td className="px-4 py-3">{event.location}</td>
                <td className="px-4 py-3">{event.ageCategory}</td>
                <td className="px-4 py-3">{event.quota}</td>
                <td className="px-4 py-3 capitalize">{event.status}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default function EventsPage() {
  return (
    <PublicPageLayout>
      <section className="container mx-auto w-full px-6 py-16">
        <h1 className="text-3xl font-semibold text-zinc-900">Events</h1>
        <p className="mt-4 max-w-2xl text-zinc-700">
          Lihat jadwal event dan race yang dirancang ramah anak untuk menumbuhkan sportivitas dan pengalaman kompetisi
          positif.
        </p>

        <EventList title="Event Mendatang" items={groupedEvents.mendatang} />
        <EventList title="Event Selesai" items={groupedEvents.selesai} />

        <div className="mt-12 rounded-2xl bg-zinc-900 px-6 py-8 text-white">
          <h3 className="text-2xl font-semibold">Siap ikut event berikutnya?</h3>
          <p className="mt-2 max-w-2xl text-zinc-200">
            Daftarkan si kecil sekarang dan dapatkan pengalaman seru bareng komunitas pushbike di kotamu.
          </p>
          <a
            href="/register"
            className="mt-5 inline-flex rounded-lg bg-lime-400 px-5 py-3 text-sm font-semibold text-zinc-900 transition hover:bg-lime-300"
          >
            Daftar Event
          </a>
        </div>
      </section>
    </PublicPageLayout>
  );
}
