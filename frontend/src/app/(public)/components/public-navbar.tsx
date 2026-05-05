import Link from "next/link";

const navItems = [
  { label: "Program", href: "/program" },
  { label: "Events", href: "/events" },
  { label: "Gallery", href: "/gallery" },
  { label: "Testimonials", href: "/testimonials" },
  { label: "Pricing", href: "/pricing" },
  { label: "About", href: "/about" },
  { label: "Contact", href: "/contact" },
];

export default function PublicNavbar() {
  return (
    <header className="sticky top-0 z-20 border-b border-zinc-200/80 bg-white/85 backdrop-blur">
      <div className="layout-container flex items-center justify-between py-4">
        <Link href="/" className="text-lg font-semibold tracking-tight text-zinc-900">
          Boost Academy
        </Link>

        <nav className="hidden items-center gap-6 md:flex" aria-label="Main navigation">
          {navItems.map((item) => (
            <Link key={item.label} href={item.href} className="text-sm font-medium text-zinc-600 hover:text-zinc-900">
              {item.label}
            </Link>
          ))}
        </nav>

        <div className="hidden items-center gap-3 md:flex">
          <Link
            href="/contact"
            className="inline-flex rounded-full border border-zinc-300 px-4 py-2 text-sm font-semibold text-zinc-700 transition hover:border-zinc-400 hover:text-zinc-900"
          >
            Contact
          </Link>
          <Link
            href="/trial"
            className="inline-flex rounded-full bg-zinc-900 px-4 py-2 text-sm font-semibold text-white transition hover:bg-zinc-700"
          >
            Free Trial
          </Link>
        </div>

        <details className="relative md:hidden">
          <summary className="flex cursor-pointer list-none items-center rounded-md border border-zinc-300 px-3 py-2 text-sm font-medium text-zinc-700 marker:content-none hover:border-zinc-400 hover:text-zinc-900">
            Menu
          </summary>
          <div className="absolute right-0 mt-2 w-56 rounded-xl border border-zinc-200 bg-white p-3 shadow-lg">
            <nav className="flex flex-col gap-1" aria-label="Mobile navigation">
              {navItems.map((item) => (
                <Link
                  key={`mobile-${item.label}`}
                  href={item.href}
                  className="rounded-md px-3 py-2 text-sm font-medium text-zinc-700 hover:bg-zinc-100 hover:text-zinc-900"
                >
                  {item.label}
                </Link>
              ))}
              <div className="my-1 h-px bg-zinc-200" />
              <Link
                href="/contact"
                className="rounded-md px-3 py-2 text-sm font-semibold text-zinc-700 hover:bg-zinc-100 hover:text-zinc-900"
              >
                Contact
              </Link>
              <Link
                href="/trial"
                className="rounded-md bg-zinc-900 px-3 py-2 text-center text-sm font-semibold text-white hover:bg-zinc-700"
              >
                Free Trial
              </Link>
            </nav>
          </div>
        </details>
      </div>
    </header>
  );
}
