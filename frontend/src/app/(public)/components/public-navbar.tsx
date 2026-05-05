import Link from "next/link";

const navItems = [
  { label: "Program", href: "#program" },
  { label: "Progres", href: "#progres" },
  { label: "Testimoni", href: "#testimoni" },
  { label: "FAQ", href: "#faq" },
];

export default function PublicNavbar() {
  return (
    <header className="sticky top-0 z-20 border-b border-zinc-200/80 bg-white/85 backdrop-blur">
      <div className="mx-auto flex w-full max-w-6xl items-center justify-between px-6 py-4">
        <Link href="/" className="text-lg font-semibold tracking-tight text-zinc-900">
          Boost Academy
        </Link>

        <nav className="hidden items-center gap-6 md:flex">
          {navItems.map((item) => (
            <a key={item.label} href={item.href} className="text-sm font-medium text-zinc-600 hover:text-zinc-900">
              {item.label}
            </a>
          ))}
        </nav>

        <Link
          href="/login"
          className="inline-flex rounded-full bg-zinc-900 px-4 py-2 text-sm font-semibold text-white transition hover:bg-zinc-700"
        >
          Masuk
        </Link>
      </div>
    </header>
  );
}
