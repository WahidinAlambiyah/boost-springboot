export type TestimonialCategory = "orang-tua" | "coach" | "progress-story";

export type Testimonial = {
  id: string;
  category: TestimonialCategory;
  quote: string;
  name: string;
  role: string;
  childName?: string;
  program?: "pushbike" | "kelas-belajar";
};

export const testimonials: Testimonial[] = [
  {
    id: "ts-parent-dina",
    category: "orang-tua",
    quote: "Anak jadi lebih disiplin latihan dan kami selalu update progresnya tiap minggu.",
    name: "Dina P.",
    role: "Orang Tua Siswa",
    childName: "Rafa",
    program: "pushbike",
  },
  {
    id: "ts-coach-ario",
    category: "coach",
    quote: "Manajemen kelas jauh lebih rapi, absensi dan evaluasi coach bisa langsung dipantau.",
    name: "Coach Ario",
    role: "Head Coach",
    program: "pushbike",
  },
  {
    id: "ts-progress-nadya",
    category: "progress-story",
    quote: "Event balapan terasa lebih tertata karena semua data peserta dan jadwal sudah terintegrasi.",
    name: "Nadya R.",
    role: "Event Coordinator",
    program: "pushbike",
  },
  {
    id: "ts-parent-rani",
    category: "orang-tua",
    quote: "Kelas belajar bikin anak lebih fokus mendengarkan instruksi dan lebih percaya diri saat presentasi kecil.",
    name: "Rani L.",
    role: "Orang Tua Siswa",
    childName: "Keano",
    program: "kelas-belajar",
  },
];

export const parentTestimonials = testimonials.filter((item) => item.category === "orang-tua");
export const coachTestimonials = testimonials.filter((item) => item.category === "coach");
export const progressStories = testimonials.filter((item) => item.category === "progress-story");

export const testimonialHighlightPoints = [
  "Lebih percaya diri",
  "Lebih fokus",
  "Lebih berani",
  "Progress lebih mudah dipantau",
] as const;
