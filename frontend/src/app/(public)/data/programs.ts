export type ProgramSlug = "pushbike" | "kelas-belajar";

export type ProgramOverview = {
  slug: ProgramSlug;
  title: string;
  level: string;
  description: string;
  ageRange: string;
  duration: string;
  schedule: string;
};

export type ProgramDetail = {
  slug: ProgramSlug;
  headline: string;
  summary: string;
  goals: string[];
  activities: string[];
  benefits: string[];
  notes: string[];
};

export const programOverviews: ProgramOverview[] = [
  {
    slug: "pushbike",
    title: "Pushbike Class",
    level: "Program utama",
    description: "Latihan keseimbangan, kontrol arah, dan teknik dasar pushbike lewat metode fun learning.",
    ageRange: "Usia 2–7 tahun",
    duration: "60 menit/sesi",
    schedule: "Selasa, Kamis, Sabtu",
  },
  {
    slug: "kelas-belajar",
    title: "Kelas Belajar Anak",
    level: "Program pendamping",
    description: "Aktivitas belajar terarah untuk fokus, motorik, dan kerja sama anak dalam suasana menyenangkan.",
    ageRange: "Usia 3–8 tahun",
    duration: "90 menit/sesi",
    schedule: "Rabu & Jumat",
  },
];

export const programDetails: Record<ProgramSlug, ProgramDetail> = {
  pushbike: {
    slug: "pushbike",
    headline: "Bangun percaya diri anak lewat aktivitas pushbike yang aman dan terstruktur",
    summary:
      "Kurikulum pushbike dirancang bertahap dari level beginner sampai advance agar anak berkembang secara konsisten.",
    goals: [
      "Meningkatkan keseimbangan dan koordinasi tubuh",
      "Melatih kontrol kecepatan dan pengereman dasar",
      "Membangun keberanian di lintasan dan saat mini race",
    ],
    activities: ["Drill start gate", "Mini obstacle run", "Cornering challenge", "Practice lap berkelompok"],
    benefits: [
      "Anak lebih aktif secara fisik",
      "Refleks dan fokus meningkat",
      "Lebih siap ikut event pushbike ramah anak",
    ],
    notes: [
      "Setiap sesi didampingi coach bersertifikat",
      "Wajib menggunakan helm, sarung tangan, dan pelindung lutut",
    ],
  },
  "kelas-belajar": {
    slug: "kelas-belajar",
    headline: "Kelas belajar yang menstimulasi fokus, kemandirian, dan interaksi sosial",
    summary:
      "Program ini menggabungkan aktivitas kognitif dan motorik agar anak siap menghadapi rutinitas belajar sehari-hari.",
    goals: [
      "Melatih atensi terhadap instruksi",
      "Mengembangkan motorik halus dan kasar",
      "Membiasakan anak berkolaborasi dalam tim kecil",
    ],
    activities: [
      "Latihan fokus instruksi",
      "Permainan motorik tematik",
      "Sesi kreativitas dan problem solving sederhana",
      "Kolaborasi tim kecil",
    ],
    benefits: [
      "Anak lebih percaya diri saat belajar",
      "Kemampuan komunikasi meningkat",
      "Kebiasaan disiplin terbentuk secara bertahap",
    ],
    notes: [
      "Materi dibagi berdasarkan kelompok usia",
      "Laporan progres dibagikan rutin ke orang tua",
    ],
  },
};


export type ProgramHighlight = {
  title: string;
  description: string;
};

export const programHighlights: ProgramHighlight[] = [
  {
    title: "Pushbike Class",
    description: "Latihan keseimbangan, teknik dasar, dan keberanian anak di lintasan lewat sesi fun learning.",
  },
  {
    title: "Kegiatan Belajar Anak",
    description: "Aktivitas fokus, motorik, dan kolaborasi untuk membentuk kebiasaan belajar yang menyenangkan.",
  },
  {
    title: "Event & Race",
    description: "Kesempatan anak mencoba mini race dan event ramah keluarga untuk menumbuhkan percaya diri.",
  },
];
