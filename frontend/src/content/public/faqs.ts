export type FaqCategory = "trial" | "pushbike" | "umum";

export type FaqItem = {
  id: string;
  question: string;
  answer: string;
};

export type FaqGroup = {
  id: FaqCategory;
  label: string;
  items: FaqItem[];
};

export const faqGroups: FaqGroup[] = [
  {
    id: "trial",
    label: "Trial Class",
    items: [
      {
        id: "faq-trial-usia",
        question: "Anak usia berapa bisa ikut?",
        answer:
          "Program dirancang untuk anak usia 2–7 tahun dengan pembagian level agar latihan tetap aman dan menyenangkan.",
      },
      {
        id: "faq-trial-pushbike",
        question: "Harus punya pushbike sendiri?",
        answer:
          "Tidak wajib. Untuk trial class, anak bisa menggunakan unit latihan yang disediakan sesuai ketersediaan di lokasi.",
      },
      {
        id: "faq-trial-orang-tua",
        question: "Apakah orang tua wajib mendampingi?",
        answer:
          "Disarankan mendampingi saat trial agar orang tua bisa memahami arahan coach dan perkembangan awal anak.",
      },
    ],
  },
  {
    id: "pushbike",
    label: "Pushbike",
    items: [
      {
        id: "faq-pushbike-cuaca",
        question: "Kalau hujan bagaimana?",
        answer:
          "Tim akan menginformasikan penjadwalan ulang atau opsi sesi indoor sesuai kondisi cuaca dan lokasi latihan.",
      },
      {
        id: "faq-pushbike-race",
        question: "Apakah anak langsung ikut race?",
        answer:
          "Tidak. Coach akan menilai kesiapan anak terlebih dahulu dari skill dasar, fokus, dan keberanian sebelum ikut event race.",
      },
      {
        id: "faq-pushbike-peralatan",
        question: "Apakah harus membawa sepeda dan perlengkapan sendiri?",
        answer:
          "Anak disarankan membawa pushbike pribadi. Helm, sarung tangan, dan pelindung lutut wajib digunakan di setiap sesi.",
      },
    ],
  },
  {
    id: "umum",
    label: "Umum",
    items: [
      {
        id: "faq-umum-laporan",
        question: "Apakah ada laporan perkembangan?",
        answer:
          "Ada. Orang tua menerima ringkasan progres anak yang mencakup kehadiran, kemampuan teknis, dan rekomendasi latihan berikutnya.",
      },
      {
        id: "faq-umum-trial",
        question: "Apakah bisa trial dulu?",
        answer: "Bisa. Anda bisa mulai dari trial class agar anak mencoba suasana latihan sebelum memilih program reguler.",
      },
      {
        id: "faq-umum-jadwal",
        question: "Bagaimana cara melihat jadwal dan event terbaru?",
        answer: "Jadwal program, event mendatang, dan pengumuman tersedia di halaman event serta dashboard orang tua.",
      },
    ],
  },
];

export const faqs = faqGroups.flatMap((group) => group.items.map((item) => ({ ...item, category: group.id })));
export const pushbikeFaqs = faqs.filter((item) => item.category === "pushbike");
export const generalFaqs = faqs.filter((item) => item.category === "umum");
