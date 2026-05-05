type EmptyImagePlaceholderProps = {
  category: string;
  title: string;
};

export default function EmptyImagePlaceholder({ category, title }: EmptyImagePlaceholderProps) {
  return (
    <div className="relative overflow-hidden rounded-2xl border border-zinc-200/80 bg-gradient-to-br from-zinc-100 via-slate-100 to-emerald-100/70 p-5">
      <span className="inline-flex rounded-full border border-emerald-200 bg-white/90 px-3 py-1 text-[11px] font-semibold uppercase tracking-wide text-emerald-700">
        {category}
      </span>
      <div className="mt-5 h-28 rounded-xl bg-gradient-to-r from-white/60 to-zinc-200/70" aria-hidden="true" />
      <p className="mt-4 text-sm font-medium text-zinc-700">{title}</p>
    </div>
  );
}
