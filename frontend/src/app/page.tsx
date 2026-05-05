import CtaSection from "@/app/(public)/components/cta-section";
import FeaturesSection from "@/app/(public)/components/features-section";
import HeroSection from "@/app/(public)/components/hero-section";

export default function HomePage() {
  return (
    <main className="min-h-screen bg-zinc-50">
      <HeroSection />
      <FeaturesSection />
      <CtaSection />
    </main>
  );
}
