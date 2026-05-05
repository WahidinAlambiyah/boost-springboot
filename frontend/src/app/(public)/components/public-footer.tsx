export default function PublicFooter() {
  return (
    <footer className="border-t border-zinc-200 bg-white">
      <div className="mx-auto grid w-full max-w-6xl gap-10 px-6 py-12 sm:grid-cols-2">
        <div>
          <p className="text-lg font-semibold text-zinc-900">Boost Academy</p>
          <p className="mt-3 max-w-sm text-sm leading-6 text-zinc-600">
            Platform operasional akademi yang ringan, rapi, dan membantu tim fokus ke perkembangan siswa.
          </p>
        </div>
        <div className="sm:justify-self-end">
          <p className="text-sm font-semibold uppercase tracking-wide text-zinc-500">Kontak</p>
          <p className="mt-3 text-sm text-zinc-600">hello@boostacademy.id</p>
          <p className="mt-1 text-sm text-zinc-600">+62 812-3456-7890</p>
        </div>
      </div>
    </footer>
  );
}
