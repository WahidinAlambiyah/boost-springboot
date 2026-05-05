"use client";

import { FormEvent, useState } from "react";

type ContactFormState = {
  name: string;
  email: string;
  message: string;
};

const initialFormState: ContactFormState = {
  name: "",
  email: "",
  message: "",
};

export default function ContactForm() {
  const [form, setForm] = useState<ContactFormState>(initialFormState);
  const [feedback, setFeedback] = useState<string>("");

  const onSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (!form.name.trim() || !form.email.trim() || !form.message.trim()) {
      setFeedback("Mohon lengkapi nama, email, dan pesan terlebih dahulu.");
      return;
    }

    setFeedback("Pesan dummy berhasil dikirim. Tim kami akan segera menghubungi Anda.");
    setForm(initialFormState);
  };

  return (
    <div className="rounded-xl border border-zinc-200 p-6">
      <h2 className="text-xl font-semibold text-zinc-900">Form Kontak</h2>
      <p className="mt-2 text-zinc-700">
        Form ini bersifat dummy untuk tampilan UI. Data tidak dikirim ke backend.
      </p>

      <form className="mt-6 space-y-4" onSubmit={onSubmit}>
        <div>
          <label htmlFor="name" className="mb-1 block text-sm font-medium text-zinc-800">
            Nama
          </label>
          <input
            id="name"
            name="name"
            type="text"
            placeholder="Nama lengkap"
            value={form.name}
            onChange={(event) => setForm((prev) => ({ ...prev, name: event.target.value }))}
            className="w-full rounded-lg border border-zinc-300 px-3 py-2 text-zinc-900 focus:border-emerald-500 focus:outline-none"
          />
        </div>

        <div>
          <label htmlFor="email" className="mb-1 block text-sm font-medium text-zinc-800">
            Email
          </label>
          <input
            id="email"
            name="email"
            type="email"
            placeholder="nama@email.com"
            value={form.email}
            onChange={(event) => setForm((prev) => ({ ...prev, email: event.target.value }))}
            className="w-full rounded-lg border border-zinc-300 px-3 py-2 text-zinc-900 focus:border-emerald-500 focus:outline-none"
          />
        </div>

        <div>
          <label htmlFor="message" className="mb-1 block text-sm font-medium text-zinc-800">
            Pesan
          </label>
          <textarea
            id="message"
            name="message"
            rows={4}
            placeholder="Tulis pertanyaan Anda"
            value={form.message}
            onChange={(event) => setForm((prev) => ({ ...prev, message: event.target.value }))}
            className="w-full rounded-lg border border-zinc-300 px-3 py-2 text-zinc-900 focus:border-emerald-500 focus:outline-none"
          />
        </div>

        <button
          type="submit"
          className="inline-flex rounded-lg bg-zinc-900 px-4 py-2 font-medium text-white transition hover:bg-zinc-800"
        >
          Kirim (Dummy)
        </button>
      </form>

      {feedback && <p className="mt-4 text-sm text-emerald-700">{feedback}</p>}
    </div>
  );
}
