export type EventStatus = "upcoming" | "completed";

export type PublicEvent = {
  id: string;
  title: string;
  category: "pushbike" | "learning" | "race" | "community";
  status: EventStatus;
  date: string;
  location: string;
  description: string;
  registrationOpen: boolean;
};

export const events: PublicEvent[] = [
  {
    id: "ev-upcoming-bootcamp-june",
    title: "Pushbike Weekend Bootcamp",
    category: "pushbike",
    status: "upcoming",
    date: "2026-06-14",
    location: "Mini Velodrome BSD",
    description: "Sesi intensif teknik cornering, braking, dan race simulation untuk level beginner–intermediate.",
    registrationOpen: true,
  },
  {
    id: "ev-upcoming-family-day",
    title: "Family Community Day",
    category: "community",
    status: "upcoming",
    date: "2026-06-28",
    location: "Taman Kota 2",
    description: "Kegiatan komunitas keluarga: fun games, sharing coach, dan mini challenge anak.",
    registrationOpen: true,
  },
  {
    id: "ev-completed-race-cup",
    title: "Regional Pushbike Cup",
    category: "race",
    status: "completed",
    date: "2026-04-20",
    location: "GOR Serbaguna Tangerang",
    description: "Kompetisi regional dengan kategori usia dan level; fokus sportivitas serta pengalaman race positif.",
    registrationOpen: false,
  },
  {
    id: "ev-completed-learning-showcase",
    title: "Learning Showcase Batch 1",
    category: "learning",
    status: "completed",
    date: "2026-03-29",
    location: "Boost Learning Studio",
    description: "Presentasi capaian kelas belajar anak, termasuk aktivitas motorik, fokus, dan kerja sama tim.",
    registrationOpen: false,
  },
];

export const upcomingEvents = events.filter((event) => event.status === "upcoming");
export const completedEvents = events.filter((event) => event.status === "completed");
