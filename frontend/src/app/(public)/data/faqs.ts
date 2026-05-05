export type FaqCategory = "pushbike" | "umum";

export type FaqItem = {
  id: string;
  category: FaqCategory;
  question: string;
  answer: string;
};

export const faqs: FaqItem[] = [
  {
    id: "faq-pushbike-usia",
    category: "pushbike",
    question: "Usia berapa anak bisa mulai kelas pushbike?",
    answer: "Program pushbike tersedia untuk anak usia 2–7 tahun dengan pembagian level sesuai kemampuan.",
  },
  {
    id: "faq-pushbike-peralatan",
    category: "pushbike",
    question: "Apakah harus membawa sepeda dan perlengkapan sendiri?",
    answer:
      "Anak disarankan membawa pushbike pribadi. Helm, sarung tangan, dan pelindung lutut wajib digunakan di setiap sesi.",
  },
  {
    id: "faq-pushbike-keselamatan",
    category: "pushbike",
    question: "Bagaimana standar keamanan saat latihan?",
    answer:
      "Setiap latihan dipandu coach dan asisten coach. Area latihan ditata aman, serta ada briefing keselamatan sebelum sesi dimulai.",
  },
  {
    id: "faq-umum-jadwal",
    category: "umum",
    question: "Bagaimana cara melihat jadwal dan event terbaru?",
    answer: "Jadwal program, event mendatang, dan pengumuman tersedia di halaman event serta dashboard orang tua.",
  },
  {
    id: "faq-umum-progres",
    category: "umum",
    question: "Apakah orang tua mendapat laporan perkembangan anak?",
    answer: "Ya. Orang tua menerima laporan progres rutin berisi evaluasi coach, catatan capaian, dan rekomendasi latihan rumah.",
  },
  {
    id: "faq-umum-trial",
    category: "umum",
    question: "Apakah tersedia kelas trial?",
    answer: "Tersedia sesi trial terbatas sesuai kuota. Silakan daftar lebih awal untuk mendapatkan slot.",
  },
];

export const pushbikeFaqs = faqs.filter((item) => item.category === "pushbike");
export const generalFaqs = faqs.filter((item) => item.category === "umum");
