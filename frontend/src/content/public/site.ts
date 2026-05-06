export const siteMetadata = {
  title: "Boost Academy | Trial Kelas Pushbike Anak",
  description:
    "Program pushbike anak usia 2–7 tahun dengan coach berpengalaman, trial class, dan ringkasan progress untuk orang tua.",
  openGraph: {
    title: "Boost Academy | Trial Kelas Pushbike Anak",
    description:
      "Program pushbike anak usia 2–7 tahun dengan coach berpengalaman, trial class, dan ringkasan progress untuk orang tua.",
    type: "website",
    locale: "id_ID",
    siteName: "Boost Academy",
  },
} as const;

export const siteBrand = {
  name: "Boost Academy",
  footerDescription:
    "Kelas pushbike dan aktivitas anak yang dirancang untuk membantu orang tua memantau keberanian, fokus, dan perkembangan anak bersama coach melalui progress report yang mudah dipahami.",
} as const;

export const siteContent = {
  whatsappNumber: "6200000000000",
} as const;

export const siteContact = {
  email: "hello@boostacademy.id",
  phone: "+62 812-3456-7890",
  location: "Jl. Contoh No. 123, Kota Dummy, Indonesia",
  whatsappUrl: "https://wa.me/6200000000000",
  whatsappMessage: "Halo Boost Academy, saya ingin konsultasi kelas pushbike dan progress report untuk anak.",
  operationalHours: ["Senin - Jumat: 08.00 - 20.00", "Sabtu: 08.00 - 16.00", "Minggu: Tutup"],
  socialMedia: [
    { label: "Instagram", handle: "@dummyacademy", href: "https://instagram.com/dummyacademy" },
    { label: "TikTok", handle: "@dummyacademy", href: "https://tiktok.com/@dummyacademy" },
    { label: "YouTube", handle: "Dummy Academy", href: "https://youtube.com/@dummyacademy" },
  ],
} as const;

export const footerLinks = [
  { label: "Program", href: "/program" },
  { label: "Contact", href: "/contact" },
  { label: "Gallery", href: "/gallery" },
  { label: "FAQ", href: "/#faq" },
] as const;

export const aboutContent = {
  title: "Tentang Academy",
  intro:
    "Boost Academy adalah ruang belajar dan aktivitas anak yang menggabungkan olahraga, pembelajaran, serta pendampingan coach berpengalaman agar setiap anak berkembang secara menyeluruh.",
  mission:
    "Membantu anak bertumbuh jadi pribadi yang sehat, percaya diri, dan punya semangat belajar lewat proses yang terarah, menyenangkan, dan aman.",
  values: ["Aman", "Fun", "Progress", "Komunitas positif"],
  progressReason:
    "Pencatatan progress membantu coach dan orang tua memahami perkembangan anak dari waktu ke waktu, melihat kekuatan yang perlu terus didorong, serta area yang perlu pendampingan tambahan supaya tujuan belajar anak lebih jelas dan terukur.",
} as const;
