export const eventsPageContent = {
  title: "Events",
  description:
    "Lihat jadwal event dan race yang dirancang ramah anak untuk menumbuhkan sportivitas dan pengalaman kompetisi positif.",
  upcomingTitle: "Event Mendatang",
  pastTitle: "Event Selesai",
  ctaTitle: "Siap ikut event berikutnya?",
  ctaDescription:
    "Daftarkan si kecil sekarang dan dapatkan pengalaman seru bareng komunitas pushbike di kotamu.",
  ctaLabel: "Daftar Event",
  ctaHref: "/register",
} as const;

export type EventStatus = "mendatang" | "selesai";

export type EventItem = {
  title: string;
  date: string;
  location: string;
  ageCategory: string;
  quota: string;
  status: EventStatus;
};

export const events: EventItem[] = [
  {
    title: "Mini Fun Race BSD",
    date: "18 Mei 2026",
    location: "QBig BSD City, Tangerang",
    ageCategory: "2-4 tahun",
    quota: "60 peserta",
    status: "mendatang",
  },
  {
    title: "Pushbike Beginner Challenge",
    date: "25 Mei 2026",
    location: "Lapangan Arcamanik, Bandung",
    ageCategory: "3-5 tahun",
    quota: "80 peserta",
    status: "mendatang",
  },
  {
    title: "Parent & Kids Activity Day",
    date: "14 April 2026",
    location: "Alun-Alun Kota Bogor",
    ageCategory: "2-6 tahun",
    quota: "100 peserta",
    status: "selesai",
  },
  {
    title: "Community Race Day",
    date: "22 Maret 2026",
    location: "GOR Saparua, Bandung",
    ageCategory: "3-7 tahun",
    quota: "120 peserta",
    status: "selesai",
  },
];

export const groupedEvents: Record<EventStatus, EventItem[]> = {
  mendatang: events.filter((event) => event.status === "mendatang"),
  selesai: events.filter((event) => event.status === "selesai"),
};
