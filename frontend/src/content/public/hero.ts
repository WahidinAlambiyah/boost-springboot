export const trialCtaCopy = {
  href: "/trial",
  label: "Daftar Trial Class",
  navbarLabel: "Free Trial",
  sectionLabel: "Jadwalkan Trial Class",
  whatsappLabel: "Chat WhatsApp Sekarang",
  whatsappMessage: "Halo Boost Academy, saya ingin menjadwalkan trial class untuk anak.",
  programLabel: "Lihat Program",
  programHref: "#program",
} as const;

export const heroContent = {
  eyebrow: "Pushbike & Aktivitas Anak",
  headline: "Tempat Anak Belajar Berani, Fokus, dan Percaya Diri Lewat Kelas Pushbike",
  subheadline:
    "Program terstruktur untuk usia 2–7 tahun dengan coach berpengalaman, suasana fun learning, dan ringkasan progress yang mudah dipahami orang tua.",
  progressCardTitle: "Ringkasan progress mingguan",
  progressCardDescription: "Coach mencatat kehadiran, skill, fokus, dan rekomendasi latihan selanjutnya.",
  primaryCta: trialCtaCopy,
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

export const homePageContent = {
  reasonsTitle: "Kenapa Memilih Kami",
  programsTitle: "Program Utama",
  trialFlowTitle: "Bagaimana Alur Ikut Trial",
  trialStepLabel: "Step",
  coachNotesTitle: "Yang Dicatat Coach",
  audienceTitle: "Untuk Siapa Program Ini",
  progressPreviewTitle: "Preview Progress Anak",
  galleryPreviewTitle: "Preview Aktivitas Anak",
  testimonialsTitle: "Testimoni",
} as const;

export const trialPageContent = {
  eyebrow: "Trial Class Pushbike",
  title: "Jadwalkan trial class yang nyaman untuk anak dan orang tua",
  description:
    "Halaman ini berisi form statis untuk membantu orang tua menyiapkan data awal sebelum tim Boost Academy menghubungi kembali. Trial dirancang fun, aman, dan fokus pada keberanian, keseimbangan, serta rasa percaya diri anak.",
  whatsappLabel: "Chat WhatsApp untuk Jadwal",
  whatsappMessage: "Halo Boost Academy, saya ingin menjadwalkan trial class untuk anak.",
  programLabel: "Lihat Program Dulu",
  programHref: "/program",
  nextStepsTitle: "Apa yang terjadi setelah daftar?",
  formTitle: "Form minat trial",
  formDescription: "Dummy/static form untuk mencatat kebutuhan keluarga sebelum konfirmasi via WhatsApp.",
  submitLabel: "Simpan Minat Trial",
  confirmLabel: "Konfirmasi via WhatsApp",
  confirmMessage: "Halo Boost Academy, saya sudah mengisi minat trial dan ingin konfirmasi jadwal.",
} as const;

export const trialFormFields = {
  parentName: { label: "Nama orang tua", placeholder: "Contoh: Ayu Pratama" },
  childName: { label: "Nama anak", placeholder: "Contoh: Raka" },
  childAge: {
    label: "Usia anak",
    placeholder: "Pilih usia",
    options: [
      { value: "2-3", label: "2–3 tahun" },
      { value: "4-5", label: "4–5 tahun" },
      { value: "6-7", label: "6–7 tahun" },
    ],
  },
  preferredSchedule: { label: "Preferensi jadwal", placeholder: "Contoh: Sabtu pagi" },
  notes: { label: "Catatan untuk coach", placeholder: "Contoh: Anak baru pertama kali mencoba pushbike." },
} as const;

export const trialPageSteps = [
  "Isi data singkat anak dan pilihan jadwal yang paling nyaman untuk keluarga.",
  "Tim kami mengonfirmasi slot trial, lokasi, dan perlengkapan yang perlu dibawa.",
  "Anak mencoba kelas bersama coach, lalu orang tua mendapat ringkasan observasi awal.",
] as const;

export const ctaSectionContent = {
  title: "Siap coba trial class untuk anak Anda?",
  description:
    "Pilih program yang cocok, konsultasi singkat via WhatsApp, lalu jadwalkan trial class bersama coach.",
} as const;

export const progressPreviewLabels = {
  levelPrefix: "Level",
  balance: "Balance",
  braking: "Braking",
  confidence: "Confidence",
  coachNote: "Catatan coach",
} as const;

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
