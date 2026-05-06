export const galleryPageContent = {
  title: "Gallery",
  description: "Dokumentasi kegiatan kelas, latihan pushbike, dan momen terbaik event akademi.",
  unavailableImageLabel: "Foto akan segera tersedia",
} as const;

export type GalleryCategory = "pushbike" | "learning" | "race" | "community";

export type GalleryItem = {
  id: string;
  category: GalleryCategory;
  title: string;
  description: string;
  imageUrl: string;
  takenAt: string;
};

export const categoryTabs: Array<{ label: string; value: GalleryCategory }> = [
  { label: "Pushbike", value: "pushbike" },
  { label: "Learning", value: "learning" },
  { label: "Race", value: "race" },
  { label: "Community", value: "community" },
];

export const galleryItems: GalleryItem[] = [
  {
    id: "gl-pushbike-01",
    category: "pushbike",
    title: "Balance Drill Session",
    description: "Anak berlatih menjaga keseimbangan di lintasan lurus bersama coach.",
    imageUrl: "/images/gallery/pushbike-balance-drill.jpg",
    takenAt: "2026-04-12",
  },
  {
    id: "gl-learning-01",
    category: "learning",
    title: "Focus & Motoric Class",
    description: "Sesi pembelajaran motorik halus dengan aktivitas tematik interaktif.",
    imageUrl: "/images/gallery/learning-focus-motoric.jpg",
    takenAt: "2026-04-09",
  },
  {
    id: "gl-race-01",
    category: "race",
    title: "Weekend Fun Race",
    description: "Momen start race kategori beginner dengan suasana aman dan positif.",
    imageUrl: "/images/gallery/race-weekend-fun.jpg",
    takenAt: "2026-03-23",
  },
  {
    id: "gl-community-01",
    category: "community",
    title: "Parent Sharing Circle",
    description: "Diskusi komunitas orang tua dan coach tentang perkembangan anak.",
    imageUrl: "/images/gallery/community-parent-sharing.jpg",
    takenAt: "2026-02-16",
  },
];

export const galleryByCategory: Record<GalleryCategory, GalleryItem[]> = {
  pushbike: galleryItems.filter((item) => item.category === "pushbike"),
  learning: galleryItems.filter((item) => item.category === "learning"),
  race: galleryItems.filter((item) => item.category === "race"),
  community: galleryItems.filter((item) => item.category === "community"),
};
