"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";

import { MenuItemResponse } from "@/types/api";
import { useAuthStore } from "@/store/auth";

type FallbackMode = "disabled" | "dev-only" | "safety";

const FALLBACK_MODE = (process.env.NEXT_PUBLIC_MENU_FALLBACK_MODE ?? "dev-only") as FallbackMode;
const ENABLE_MENU_FALLBACK = FALLBACK_MODE === "safety" || (FALLBACK_MODE === "dev-only" && process.env.NODE_ENV !== "production");
const ENABLED_FALLBACK_ROUTES = new Set(["/academies", "/academy-locations", "/coach-profiles", "/students", "/assessment-skills"]);

const LOCAL_FALLBACK_MENU: MenuItemResponse[] = [
  { id: "local-academies", label: "Academies", path: "/academies", visible: true, children: [] },
  { id: "local-academy-locations", label: "Academy Locations", path: "/academy-locations", visible: true, children: [] },
  { id: "local-coach-profiles", label: "Coach Profiles", path: "/coach-profiles", visible: true, children: [] },
  { id: "local-students", label: "Students", path: "/students", visible: true, children: [] },
  { id: "local-fe-4", label: "Assessment Skills", path: "/assessment-skills", visible: true, children: [] },
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

const flattenMenu = (item: MenuItemResponse): MenuItemResponse[] => [
  item,
  ...(item.children?.flatMap((child) => flattenMenu(child)) ?? []),
];

const mergeMenuWithLocalFallback = (items: MenuItemResponse[]): MenuItemResponse[] => {
  if (!ENABLE_MENU_FALLBACK) {
    return items;
  }

  const knownPaths = new Set(items.flatMap((item) => flattenMenu(item).map((node) => node.path)).filter(Boolean));
  const fallbackItems = LOCAL_FALLBACK_MENU.filter(
    (item) => item.path && ENABLED_FALLBACK_ROUTES.has(item.path) && !knownPaths.has(item.path),
  );

  if (fallbackItems.length > 0) {
    console.info("[sidebar] Local menu fallback active.", {
      mode: FALLBACK_MODE,
      backendCount: items.length,
      fallbackCount: fallbackItems.length,
      fallbackPaths: fallbackItems.map((item) => item.path),
    });
  }

  return [...items, ...fallbackItems];
};

const getMenuInitials = (label: string) => {
  const words = label.trim().split(/\s+/).filter(Boolean);
  if (words.length === 0) return "?";
  if (words.length === 1) return words[0].slice(0, 2).toUpperCase();
  return words
    .slice(0, 2)
    .map((word) => word[0])
    .join("")
    .toUpperCase();
};

interface SidebarItemProps {
  item: MenuItemResponse;
  pathname: string;
  level?: number;
  collapsed?: boolean;
}

function SidebarItem({ item, pathname, level = 0, collapsed = false }: SidebarItemProps) {
  const children = item.children ?? [];
  const hasChildren = children.length > 0;
  const isActive = Boolean(item.path) && pathname === item.path;
  const itemTitle = collapsed && level === 0 ? item.label : undefined;
  const collapsedClassName = collapsed && level === 0 ? "justify-center px-2" : "px-3";
  const itemContent = collapsed && level === 0 ? (
    <span aria-hidden="true" className="text-xs font-semibold uppercase">
      {getMenuInitials(item.label)}
    </span>
  ) : (
    item.label
  );

  return (
    <div className="flex flex-col gap-1">
      {item.path ? (
        <Link
          href={item.path}
          title={itemTitle}
          aria-label={collapsed && level === 0 ? item.label : undefined}
          className={`flex rounded-md py-2 text-sm transition-colors ${collapsedClassName} ${
            isActive ? "bg-zinc-900 text-white" : "text-zinc-700 hover:bg-zinc-100"
          }`}
          style={collapsed && level === 0 ? undefined : { paddingLeft: `${0.75 + level * 0.75}rem` }}
        >
          {itemContent}
        </Link>
      ) : (
        <p
          title={itemTitle}
          className={`py-2 text-sm font-medium text-zinc-600 ${collapsedClassName}`}
          style={collapsed && level === 0 ? undefined : { paddingLeft: `${0.75 + level * 0.75}rem` }}
        >
          {itemContent}
        </p>
      )}

      {hasChildren && !collapsed ? (
        <div className="flex flex-col gap-1">
          {children.map((child) => (
            <SidebarItem key={child.id} item={child} pathname={pathname} level={level + 1} collapsed={collapsed} />
          ))}
        </div>
      ) : null}
    </div>
  );
}

interface SidebarProps {
  collapsed?: boolean;
}

export default function Sidebar({ collapsed = false }: SidebarProps) {
  const pathname = usePathname();
  const menu = useAuthStore((state) => state.menu);
  const visibleMenu = filterVisibleMenu(mergeMenuWithLocalFallback(menu));

  return (
    <aside
      className={`border-r border-zinc-200 bg-white p-4 transition-all duration-200 ${collapsed ? "w-20" : "w-64"}`}
      aria-label="Sidebar navigation"
    >
      <p className={`mb-3 text-sm font-semibold uppercase text-zinc-500 ${collapsed ? "text-center text-xs" : ""}`}>
        {collapsed ? "Nav" : "Navigation"}
      </p>
      {visibleMenu.length === 0 ? (
        <div className="rounded-md border border-dashed border-zinc-300 bg-zinc-50 px-3 py-4 text-sm text-zinc-500">
          {collapsed ? "-" : "Tidak ada menu yang tersedia."}
        </div>
      ) : (
        <nav className="flex flex-col gap-2">
          {visibleMenu.map((item) => (
            <SidebarItem key={item.id} item={item} pathname={pathname} collapsed={collapsed} />
          ))}
        </nav>
      )}
    </aside>
  );
}
