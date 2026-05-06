"use client";

import Image from "next/image";
import { useMemo, useState } from "react";

import PublicPageLayout from "@/app/(public)/components/public-page-layout";
import {
  categoryTabs,
  galleryItems,
  type GalleryCategory,
  type GalleryItem,
} from "@/content/public/gallery";

function GalleryCard({ item }: { item: GalleryItem }) {
  const [hasImageError, setHasImageError] = useState(false);
  const showPlaceholder = hasImageError || !item.imageUrl;

  return (
    <article className="overflow-hidden rounded-2xl border border-zinc-200 bg-white shadow-sm">
      {showPlaceholder ? (
        <div className="flex h-48 items-center justify-center bg-gradient-to-br from-sky-100 via-indigo-100 to-purple-100 text-sm font-medium text-zinc-600">
          Foto akan segera tersedia
        </div>
      ) : (
        <Image
          src={item.imageUrl}
          alt={item.title}
          width={640}
          height={360}
          className="h-48 w-full object-cover"
          onError={() => setHasImageError(true)}
        />
      )}

      <div className="space-y-2 p-5">
        <p className="text-xs font-medium uppercase tracking-wide text-zinc-500">{item.category}</p>
        <h2 className="text-lg font-semibold text-zinc-900">{item.title}</h2>
        <p className="text-sm text-zinc-700">{item.description}</p>
      </div>
    </article>
  );
}

export default function GalleryPage() {
  const [activeCategory, setActiveCategory] = useState<GalleryCategory>("pushbike");

  const filteredItems = useMemo(
    () => galleryItems.filter((item) => item.category === activeCategory),
    [activeCategory],
  );

  return (
    <PublicPageLayout>
      <section className="container mx-auto w-full px-6 py-16">
        <h1 className="text-3xl font-semibold text-zinc-900">Gallery</h1>
        <p className="mt-4 max-w-2xl text-zinc-700">
          Dokumentasi kegiatan kelas, latihan pushbike, dan momen terbaik event akademi.
        </p>

        <div className="mt-8 flex flex-wrap gap-2">
          {categoryTabs.map((tab) => {
            const isActive = tab.value === activeCategory;

            return (
              <button
                key={tab.value}
                type="button"
                onClick={() => setActiveCategory(tab.value)}
                className={`rounded-full px-4 py-2 text-sm font-medium transition ${
                  isActive
                    ? "bg-zinc-900 text-white"
                    : "bg-zinc-100 text-zinc-700 hover:bg-zinc-200"
                }`}
              >
                {tab.label}
              </button>
            );
          })}
        </div>

        <div className="mt-8 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
          {filteredItems.map((item) => (
            <GalleryCard key={item.id} item={item} />
          ))}
        </div>
      </section>
    </PublicPageLayout>
  );
}
