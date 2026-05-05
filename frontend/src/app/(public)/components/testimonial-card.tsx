type TestimonialCardProps = {
  quote: string;
  name: string;
  role: string;
};

export default function TestimonialCard({ quote, name, role }: TestimonialCardProps) {
  return (
    <article className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm">
      <p className="text-sm leading-7 text-zinc-700">“{quote}”</p>
      <div className="mt-5">
        <p className="text-sm font-semibold text-zinc-900">{name}</p>
        <p className="text-xs text-zinc-500">{role}</p>
      </div>
    </article>
  );
}
