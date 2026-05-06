import { trialCtaCopy } from "@/content/public/hero";

export const navItems = [
  { label: "Program", href: "/program" },
  { label: "Events", href: "/events" },
  { label: "Gallery", href: "/gallery" },
  { label: "Testimonials", href: "/testimonials" },
  { label: "Pricing", href: "/pricing" },
  { label: "About", href: "/about" },
  { label: "Contact", href: "/contact" },
] as const;

export const navigationCtas = {
  contact: { label: "Contact", href: "/contact" },
  trial: { label: trialCtaCopy.navbarLabel, href: trialCtaCopy.href },
} as const;
