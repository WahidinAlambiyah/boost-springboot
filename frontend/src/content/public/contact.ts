export const contactPageContent = {
  title: "Contact",
  description: "Hubungi tim kami untuk konsultasi program, jadwal trial class, dan informasi pendaftaran.",
  quickContactTitle: "Kontak Cepat",
  quickContactDescription: "Butuh respons cepat? Klik tombol WhatsApp di bawah ini.",
  whatsappLabel: "Chat via WhatsApp",
  locationTitle: "Lokasi",
  operationalHoursTitle: "Jam Operasional",
  socialMediaTitle: "Sosial Media",
} as const;


export const contactFormContent = {
  title: "Form Kontak",
  description: "Form ini bersifat dummy untuk tampilan UI. Data tidak dikirim ke backend.",
  requiredFeedback: "Mohon lengkapi nama, email, dan pesan terlebih dahulu.",
  successFeedback: "Pesan dummy berhasil dikirim. Tim kami akan segera menghubungi Anda.",
  submitLabel: "Kirim (Dummy)",
  fields: {
    name: { label: "Nama", placeholder: "Nama lengkap" },
    email: { label: "Email", placeholder: "nama@email.com" },
    message: { label: "Pesan", placeholder: "Tulis pertanyaan Anda" },
  },
} as const;
