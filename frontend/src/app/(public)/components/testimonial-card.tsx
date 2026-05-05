type TestimonialCardProps = {
  quote: string;
  name: string;
  role: string;
};

export default function TestimonialCard({ quote, name, role }: TestimonialCardProps) {
  return (
    <article className="surface-card p-6">
      <p className="text-body leading-7 text-zinc-700">“{quote}”</p>
      <div className="mt-5">
        <p className="text-sm font-semibold text-zinc-900">{name}</p>
        <p className="text-xs text-zinc-500">{role}</p>
      </div>
    </article>
  );
}
