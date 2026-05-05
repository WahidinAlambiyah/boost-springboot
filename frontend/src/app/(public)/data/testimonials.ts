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
    quote: "Anak jadi lebih disiplin latihan, dan kami selalu dapat update progres mingguan dari coach.",
    name: "Dina P.",
    role: "Orang Tua Siswa",
    childName: "Rafa",
    program: "pushbike",
  },
  {
    id: "ts-coach-ario",
    category: "coach",
    quote: "Pendampingan orang tua lebih terarah karena target latihan anak bisa dipantau bersama.",
    name: "Coach Ario",
    role: "Head Coach",
    program: "pushbike",
  },
  {
    id: "ts-progress-mila",
    category: "progress-story",
    quote: "Dalam 2 bulan, Mila jadi lebih berani ambil tikungan dan mau mencoba mini race pertamanya.",
    name: "Santi K.",
    role: "Orang Tua Mila",
    childName: "Mila",
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
