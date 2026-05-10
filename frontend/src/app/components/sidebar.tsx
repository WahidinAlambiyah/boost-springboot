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
  const itemContent = collapsed && level === 0 ? (
    <span aria-hidden="true" className="text-xs font-semibold uppercase">
      {getMenuInitials(item.label)}
    </span>
  ) : (
    <span className="truncate">{item.label}</span>
  );
  const itemBaseClassName =
    "group flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-all duration-200";
  const activeClassName = isActive
    ? "bg-blue-50 text-blue-700"
    : "text-gray-700 hover:bg-gray-50 hover:text-blue-700";
  const collapsedClassName = collapsed && level === 0 ? "justify-center" : "justify-start";

  return (
    <div className="flex flex-col gap-1">
      {item.path ? (
        <Link
          href={item.path}
          title={itemTitle}
          aria-label={collapsed && level === 0 ? item.label : undefined}
          className={`${itemBaseClassName} ${activeClassName} ${collapsedClassName}`}
          style={collapsed && level === 0 ? undefined : { paddingLeft: `${0.75 + level * 0.75}rem` }}
        >
          <span
            className={`flex h-8 w-8 shrink-0 items-center justify-center rounded-lg text-xs font-semibold ${
              isActive ? "bg-blue-100 text-blue-700" : "bg-gray-100 text-gray-500 group-hover:bg-blue-50 group-hover:text-blue-700"
            }`}
          >
            {getMenuInitials(item.label)}
          </span>
          {!collapsed || level > 0 ? itemContent : null}
        </Link>
      ) : (
        <p
          title={itemTitle}
          className={`${itemBaseClassName} ${collapsedClassName} text-gray-500`}
          style={collapsed && level === 0 ? undefined : { paddingLeft: `${0.75 + level * 0.75}rem` }}
        >
          <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-gray-100 text-xs font-semibold text-gray-500">
            {getMenuInitials(item.label)}
          </span>
          {!collapsed || level > 0 ? itemContent : null}
        </p>
      )}

      {hasChildren && !collapsed ? (
        <div className="ml-4 flex flex-col gap-1 border-l border-gray-100 pl-2">
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
      className={`fixed left-0 top-0 z-50 flex h-screen flex-col border-r border-gray-200 bg-white px-5 text-gray-900 transition-all duration-300 ease-in-out ${
        collapsed ? "w-[90px]" : "w-[290px]"
      }`}
      aria-label="Sidebar navigation"
    >
      <div className={`flex py-8 ${collapsed ? "justify-center" : "justify-start"}`}>
        <Link href="/dashboard" className="flex items-center gap-3">
          <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-600 text-sm font-bold text-white shadow-sm">
            B
          </span>
          {!collapsed ? (
            <span>
              <span className="block text-sm font-semibold uppercase tracking-wide text-gray-400">Boost</span>
              <span className="block text-lg font-semibold text-gray-900">Admin</span>
            </span>
          ) : null}
        </Link>
      </div>

      <div className="flex flex-1 flex-col overflow-y-auto pb-6">
        <nav className="mb-6">
          <h2 className={`mb-4 flex text-xs uppercase leading-5 text-gray-400 ${collapsed ? "justify-center" : "justify-start"}`}>
            {collapsed ? "•••" : "Menu"}
          </h2>
          {visibleMenu.length === 0 ? (
            <div className="rounded-lg border border-dashed border-gray-200 bg-gray-50 px-3 py-4 text-sm text-gray-500">
              {collapsed ? "-" : "Tidak ada menu yang tersedia."}
            </div>
          ) : (
            <div className="flex flex-col gap-1.5">
              {visibleMenu.map((item) => (
                <SidebarItem key={item.id} item={item} pathname={pathname} collapsed={collapsed} />
              ))}
            </div>
          )}
        </nav>
      </div>
    </aside>
  );
}
