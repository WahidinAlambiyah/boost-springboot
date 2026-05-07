"use client";

import { FormEvent, useState } from "react";

import { contactFormContent } from "@/content/public/contact";

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
      setFeedback(contactFormContent.requiredFeedback);
      return;
    }

    setFeedback(contactFormContent.successFeedback);
    setForm(initialFormState);
  };

  return (
    <div className="rounded-xl border border-zinc-200 p-6">
      <h2 className="text-xl font-semibold text-zinc-900">{contactFormContent.title}</h2>
      <p className="mt-2 text-zinc-700">
        {contactFormContent.description}
      </p>

      <form className="mt-6 space-y-4" onSubmit={onSubmit}>
        <div>
          <label htmlFor="name" className="mb-1 block text-sm font-medium text-zinc-800">
            {contactFormContent.fields.name.label}
          </label>
          <input
            id="name"
            name="name"
            type="text"
            placeholder={contactFormContent.fields.name.placeholder}
            value={form.name}
            onChange={(event) => setForm((prev) => ({ ...prev, name: event.target.value }))}
            className="w-full rounded-lg border border-zinc-300 px-3 py-2 text-zinc-900 focus:border-emerald-500 focus:outline-none"
          />
        </div>

        <div>
          <label htmlFor="email" className="mb-1 block text-sm font-medium text-zinc-800">
            {contactFormContent.fields.email.label}
          </label>
          <input
            id="email"
            name="email"
            type="email"
            placeholder={contactFormContent.fields.email.placeholder}
            value={form.email}
            onChange={(event) => setForm((prev) => ({ ...prev, email: event.target.value }))}
            className="w-full rounded-lg border border-zinc-300 px-3 py-2 text-zinc-900 focus:border-emerald-500 focus:outline-none"
          />
        </div>

        <div>
          <label htmlFor="message" className="mb-1 block text-sm font-medium text-zinc-800">
            {contactFormContent.fields.message.label}
          </label>
          <textarea
            id="message"
            name="message"
            rows={4}
            placeholder={contactFormContent.fields.message.placeholder}
            value={form.message}
            onChange={(event) => setForm((prev) => ({ ...prev, message: event.target.value }))}
            className="w-full rounded-lg border border-zinc-300 px-3 py-2 text-zinc-900 focus:border-emerald-500 focus:outline-none"
          />
        </div>

        <button
          type="submit"
          className="inline-flex rounded-lg bg-zinc-900 px-4 py-2 font-medium text-white transition hover:bg-zinc-800"
        >
          {contactFormContent.submitLabel}
        </button>
      </form>

      {feedback && <p className="mt-4 text-sm text-emerald-700">{feedback}</p>}
    </div>
  );
}
