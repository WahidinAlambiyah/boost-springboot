export const heroContent = {
  eyebrow: "Pushbike & Aktivitas Anak",
  headline: "Tempat Anak Belajar Berani, Fokus, dan Percaya Diri Lewat Kelas Pushbike",
  subheadline:
    "Program terstruktur untuk usia 2–7 tahun dengan coach berpengalaman, suasana fun learning, dan ringkasan progress yang mudah dipahami orang tua.",
  progressCardTitle: "Ringkasan progress mingguan",
  progressCardDescription: "Coach mencatat kehadiran, skill, fokus, dan rekomendasi latihan selanjutnya.",
  primaryCta: { label: "Daftar Trial Class", href: "/trial" },
  secondaryCta: { label: "Konsultasi via WhatsApp" },
  trialFlowCta: { label: "Lihat Alur Trial", href: "#alur-trial" },
} as const;

export const trustBadges = [
  "Untuk usia 2–7 tahun",
  "Didampingi coach berpengalaman",
  "Progress anak tercatat",
  "Laporan untuk orang tua",
] as const;

export const reasons = [
  "Kurikulum terstruktur sesuai usia dan level anak",
  "Laporan progres mingguan dengan insight coach",
  "Komunikasi orang tua dan coach lebih cepat",
] as const;

export const trialSteps = ["Pilih program", "Konsultasi via WhatsApp", "Ikut trial class", "Anak dinilai oleh coach", "Orang tua mendapat ringkasan progress"] as const;

export const trialPageSteps = [
  "Isi data singkat anak dan pilihan jadwal yang paling nyaman untuk keluarga.",
  "Tim kami mengonfirmasi slot trial, lokasi, dan perlengkapan yang perlu dibawa.",
  "Anak mencoba kelas bersama coach, lalu orang tua mendapat ringkasan observasi awal.",
] as const;

export const progressPreviews = [
  {
    studentName: "Rafa",
    level: "Beginner",
    balance: "4/5",
    braking: "3/5",
    confidence: "4/5",
    coachNote:
      "Rafa menunjukkan progres baik di lintasan lurus dan mulai berani mengambil tikungan. Lanjutkan latihan braking bertahap agar kontrol kecepatan makin stabil.",
  },
] as const;

export const galleryPreviewItems = [
  { category: "Pushbike", title: "Drill start gate dan latihan tikungan" },
  { category: "Belajar", title: "Aktivitas fokus, motorik, dan koordinasi" },
  { category: "Event", title: "Fun race ramah anak bersama orang tua" },
] as const;
