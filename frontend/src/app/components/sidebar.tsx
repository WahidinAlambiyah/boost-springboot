"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

import { MenuItemResponse } from "@/types/api";
import { useAuthStore } from "@/store/auth";

const LOCAL_FALLBACK_MENU: MenuItemResponse[] = [
  { id: "local-academies", label: "Academies", path: "/academies", visible: true, children: [] },
  { id: "local-academy-locations", label: "Academy Locations", path: "/academy-locations", visible: true, children: [] },
  { id: "local-coach-profiles", label: "Coach Profiles", path: "/coach-profiles", visible: true, children: [] },
  { id: "local-students", label: "Students", path: "/students", visible: true, children: [] },
];

const hasVisibleChildren = (item: MenuItemResponse): boolean =>
  item.children?.some((child) => isMenuVisible(child)) ?? false;

const isMenuVisible = (item: MenuItemResponse): boolean => {
  if (item.visible === false) {
    return false;
  }

  if (!item.children || item.children.length === 0) {
    return true;
  }

  return hasVisibleChildren(item);
};

const filterVisibleMenu = (items: MenuItemResponse[]): MenuItemResponse[] =>
  items
    .filter((item) => isMenuVisible(item))
    .map((item) => ({
      ...item,
      children: item.children ? filterVisibleMenu(item.children) : [],
    }));

const mergeMenuWithLocalFallback = (items: MenuItemResponse[]): MenuItemResponse[] => {
  const knownPaths = new Set(items.flatMap((item) => flattenMenu(item).map((node) => node.path)).filter(Boolean));
  const fallbackItems = LOCAL_FALLBACK_MENU.filter((item) => item.path && !knownPaths.has(item.path));
  return [...items, ...fallbackItems];
};

const flattenMenu = (item: MenuItemResponse): MenuItemResponse[] => [
  item,
  ...(item.children?.flatMap((child) => flattenMenu(child)) ?? []),
];

interface SidebarItemProps {
  item: MenuItemResponse;
  pathname: string;
  level?: number;
}

function SidebarItem({ item, pathname, level = 0 }: SidebarItemProps) {
  const children = item.children ?? [];
  const hasChildren = children.length > 0;
  const isActive = Boolean(item.path) && pathname === item.path;

  return (
    <div className="flex flex-col gap-1">
      {item.path ? (
        <Link
          href={item.path}
          className={`rounded-md px-3 py-2 text-sm transition-colors ${
            isActive ? "bg-zinc-900 text-white" : "text-zinc-700 hover:bg-zinc-100"
          }`}
          style={{ paddingLeft: `${0.75 + level * 0.75}rem` }}
        >
          {item.label}
        </Link>
      ) : (
        <p
          className="px-3 py-2 text-sm font-medium text-zinc-600"
          style={{ paddingLeft: `${0.75 + level * 0.75}rem` }}
        >
          {item.label}
        </p>
      )}

      {hasChildren ? (
        <div className="flex flex-col gap-1">
          {children.map((child) => (
            <SidebarItem key={child.id} item={child} pathname={pathname} level={level + 1} />
          ))}
        </div>
      ) : null}
    </div>
  );
}

export default function Sidebar() {
  const pathname = usePathname();
  const menu = useAuthStore((state) => state.menu);
  const visibleMenu = filterVisibleMenu(mergeMenuWithLocalFallback(menu));

  return (
    <aside className="w-64 border-r border-zinc-200 bg-white p-4">
      <p className="mb-3 text-sm font-semibold uppercase text-zinc-500">Navigation</p>
      {visibleMenu.length === 0 ? (
        <div className="rounded-md border border-dashed border-zinc-300 bg-zinc-50 px-3 py-4 text-sm text-zinc-500">
          Tidak ada menu yang tersedia.
        </div>
      ) : (
        <nav className="flex flex-col gap-2">
          {visibleMenu.map((item) => (
            <SidebarItem key={item.id} item={item} pathname={pathname} />
          ))}
        </nav>
      )}
    </aside>
  );
}
