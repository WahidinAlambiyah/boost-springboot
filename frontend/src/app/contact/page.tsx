import ContactForm from "@/app/(public)/components/contact-form";
import PublicPageLayout from "@/app/(public)/components/public-page-layout";

export default function ContactPage() {
  return (
    <PublicPageLayout>
      <section className="container mx-auto w-full space-y-10 px-6 py-16">
        <div>
          <h1 className="text-3xl font-semibold text-zinc-900">Contact</h1>
          <p className="mt-4 max-w-2xl text-zinc-700">
            Hubungi tim kami untuk konsultasi program, jadwal trial class, dan informasi
            pendaftaran.
          </p>
        </div>

        <div className="grid gap-8 lg:grid-cols-2">
          <div className="space-y-6 rounded-xl border border-zinc-200 p-6">
            <div>
              <h2 className="text-xl font-semibold text-zinc-900">Kontak Cepat</h2>
              <p className="mt-2 text-zinc-700">
                Butuh respons cepat? Klik tombol WhatsApp di bawah ini.
              </p>
              <a
                href="https://wa.me/6200000000000"
                target="_blank"
                rel="noreferrer"
                className="mt-4 inline-flex rounded-lg bg-emerald-600 px-4 py-2 font-medium text-white transition hover:bg-emerald-700"
              >
                Chat via WhatsApp
              </a>
            </div>

            <div>
              <h3 className="text-lg font-semibold text-zinc-900">Lokasi</h3>
              <p className="mt-2 text-zinc-700">Jl. Contoh No. 123, Kota Dummy, Indonesia</p>
            </div>

            <div>
              <h3 className="text-lg font-semibold text-zinc-900">Jam Operasional</h3>
              <ul className="mt-2 space-y-1 text-zinc-700">
                <li>Senin - Jumat: 08.00 - 20.00</li>
                <li>Sabtu: 08.00 - 16.00</li>
                <li>Minggu: Tutup</li>
              </ul>
            </div>

            <div>
              <h3 className="text-lg font-semibold text-zinc-900">Sosial Media</h3>
              <ul className="mt-2 space-y-1 text-zinc-700">
                <li>
                  Instagram: <a href="https://instagram.com/dummyacademy" className="text-emerald-700 hover:underline">@dummyacademy</a>
                </li>
                <li>
                  TikTok: <a href="https://tiktok.com/@dummyacademy" className="text-emerald-700 hover:underline">@dummyacademy</a>
                </li>
                <li>
                  YouTube: <a href="https://youtube.com/@dummyacademy" className="text-emerald-700 hover:underline">Dummy Academy</a>
                </li>
              </ul>
            </div>
          </div>

          <ContactForm />
        </div>
      </section>
    </PublicPageLayout>
  );
}
