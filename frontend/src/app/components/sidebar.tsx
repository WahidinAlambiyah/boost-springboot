"use client";

import { useEffect, useMemo, useState } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";

import { MenuItemResponse } from "@/types/api";
import { useAuthStore } from "@/store/auth";

type FallbackMode = "disabled" | "dev-only" | "safety";

const FALLBACK_MODE = (process.env.NEXT_PUBLIC_MENU_FALLBACK_MODE ?? "dev-only") as FallbackMode;
const ENABLE_MENU_FALLBACK = FALLBACK_MODE === "safety" || (FALLBACK_MODE === "dev-only" && process.env.NODE_ENV !== "production");

const LOCAL_FALLBACK_MENU: MenuItemResponse[] = [
  { id: "local-dashboard", label: "Dashboard", path: "/dashboard", visible: true, children: [] },
  {
    id: "local-admin",
    label: "Admin",
    path: "/admin",
    visible: true,
    children: [
      { id: "local-admin-users", label: "User Management", path: "/admin/users", visible: true, children: [] },
      { id: "local-admin-roles", label: "Role Management", path: "/admin/roles", visible: true, children: [] },
      { id: "local-admin-permissions", label: "Permission Management", path: "/admin/permissions", visible: true, children: [] },
      { id: "local-admin-audit", label: "Audit Log", path: "/admin/audit-log", visible: true, children: [] },
    ],
  },
  {
    id: "local-master-data",
    label: "Master Data",
    path: null,
    visible: true,
    children: [
      { id: "local-academies", label: "Academies", path: "/academies", visible: true, children: [] },
      { id: "local-academy-locations", label: "Academy Locations", path: "/academy-locations", visible: true, children: [] },
      { id: "local-coach-profiles", label: "Coach Profiles", path: "/coach-profiles", visible: true, children: [] },
      { id: "local-students", label: "Students", path: "/students", visible: true, children: [] },
      { id: "local-assessment-skills", label: "Assessment Skills", path: "/assessment-skills", visible: true, children: [] },
    ],
  },
  {
    id: "local-operations",
    label: "Operations",
    path: null,
    visible: true,
    children: [
      { id: "local-catalog", label: "Catalog", path: "/catalog", visible: true, children: [] },
      { id: "local-scheduling", label: "Scheduling", path: "/scheduling", visible: true, children: [] },
      { id: "local-enrollment", label: "Enrollment", path: "/enrollment", visible: true, children: [] },
      { id: "local-attendance", label: "Attendance", path: "/attendance", visible: true, children: [] },
      { id: "local-billing", label: "Billing", path: "/billing", visible: true, children: [] },
      { id: "local-notification", label: "Notification", path: "/notification", visible: true, children: [] },
    ],
  },
  {
    id: "local-reports",
    label: "Reports",
    path: null,
    visible: true,
    children: [
      { id: "local-report-student-progress", label: "Student Progress", path: "/reports/student-progress", visible: true, children: [] },
    ],
  },
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
  const fallbackItems = LOCAL_FALLBACK_MENU.filter((item) => {
    if (!item.path) {
      return true;
    }
    return !knownPaths.has(item.path);
  });

  if (fallbackItems.length > 0) {
    console.info("[sidebar] Local menu fallback active.", {
      mode: FALLBACK_MODE,
      backendCount: items.length,
      fallbackCount: fallbackItems.length,
      fallbackPaths: fallbackItems.flatMap((item) => flattenMenu(item).map((node) => node.path)).filter(Boolean),
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

const isPathActive = (pathname: string, path?: string | null) => Boolean(path) && (pathname === path || pathname.startsWith(`${path}/`));

const hasActiveDescendant = (item: MenuItemResponse, pathname: string): boolean => {
  if (isPathActive(pathname, item.path)) {
    return true;
  }

  return item.children?.some((child) => hasActiveDescendant(child, pathname)) ?? false;
};

const findActiveAccordionIds = (items: MenuItemResponse[], pathname: string): string[] => {
  const ids: string[] = [];

  const walk = (item: MenuItemResponse) => {
    const children = item.children ?? [];
    if (children.length > 0 && children.some((child) => hasActiveDescendant(child, pathname))) {
      ids.push(item.id);
    }
    children.forEach(walk);
  };

  items.forEach(walk);
  return ids;
};

interface SidebarItemProps {
  item: MenuItemResponse;
  pathname: string;
  level?: number;
  collapsed?: boolean;
  openItemIds: Set<string>;
  onToggleItem: (itemId: string) => void;
}

function SidebarItem({ item, pathname, level = 0, collapsed = false, openItemIds, onToggleItem }: SidebarItemProps) {
  const children = item.children ?? [];
  const hasChildren = children.length > 0;
  const isActive = isPathActive(pathname, item.path);
  const isGroupActive = hasActiveDescendant(item, pathname);
  const isOpen = openItemIds.has(item.id);
  const itemTitle = collapsed && level === 0 ? item.label : undefined;
  const showText = !collapsed || level > 0;
  const itemBaseClassName =
    "group flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-all duration-200";
  const activeClassName = isActive || isGroupActive
    ? "bg-blue-50 text-blue-700"
    : "text-gray-700 hover:bg-gray-50 hover:text-blue-700";
  const collapsedClassName = collapsed && level === 0 ? "justify-center" : "justify-start";
  const iconClassName = isActive || isGroupActive
    ? "bg-blue-100 text-blue-700"
    : "bg-gray-100 text-gray-500 group-hover:bg-blue-50 group-hover:text-blue-700";

  const itemIcon = (
    <span className={`flex h-8 w-8 shrink-0 items-center justify-center rounded-lg text-xs font-semibold ${iconClassName}`}>
      {getMenuInitials(item.label)}
    </span>
  );

  const itemText = showText ? <span className="truncate">{item.label}</span> : null;

  return (
    <div className="flex flex-col gap-1">
      {hasChildren ? (
        <button
          type="button"
          title={itemTitle}
          aria-label={collapsed && level === 0 ? item.label : undefined}
          aria-expanded={isOpen}
          aria-controls={`sidebar-accordion-${item.id}`}
          onClick={() => onToggleItem(item.id)}
          className={`${itemBaseClassName} ${activeClassName} ${collapsedClassName}`}
          style={collapsed && level === 0 ? undefined : { paddingLeft: `${0.75 + level * 0.75}rem` }}
        >
          {itemIcon}
          {itemText}
          {showText ? (
            <span className={`ml-auto text-gray-400 transition-transform duration-200 ${isOpen ? "rotate-180 text-blue-600" : ""}`}>
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none" aria-hidden="true">
                <path d="M5 7.5L10 12.5L15 7.5" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" />
              </svg>
            </span>
          ) : null}
        </button>
      ) : item.path ? (
        <Link
          href={item.path}
          title={itemTitle}
          aria-label={collapsed && level === 0 ? item.label : undefined}
          className={`${itemBaseClassName} ${activeClassName} ${collapsedClassName}`}
          style={collapsed && level === 0 ? undefined : { paddingLeft: `${0.75 + level * 0.75}rem` }}
        >
          {itemIcon}
          {itemText}
        </Link>
      ) : (
        <p
          title={itemTitle}
          className={`${itemBaseClassName} ${collapsedClassName} text-gray-500`}
          style={collapsed && level === 0 ? undefined : { paddingLeft: `${0.75 + level * 0.75}rem` }}
        >
          {itemIcon}
          {itemText}
        </p>
      )}

      {hasChildren && !collapsed ? (
        <div
          id={`sidebar-accordion-${item.id}`}
          className={`grid transition-all duration-300 ${isOpen ? "grid-rows-[1fr]" : "grid-rows-[0fr]"}`}
        >
          <div className="overflow-hidden">
            <div className="ml-4 flex flex-col gap-1 border-l border-gray-100 pl-2 pt-1">
              {children.map((child) => (
                <SidebarItem
                  key={child.id}
                  item={child}
                  pathname={pathname}
                  level={level + 1}
                  collapsed={collapsed}
                  openItemIds={openItemIds}
                  onToggleItem={onToggleItem}
                />
              ))}
            </div>
          </div>
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
  const visibleMenu = useMemo(() => filterVisibleMenu(mergeMenuWithLocalFallback(menu)), [menu]);
  const [openItemIds, setOpenItemIds] = useState<Set<string>>(() => new Set(findActiveAccordionIds(visibleMenu, pathname)));

  useEffect(() => {
    setOpenItemIds((current) => {
      const next = new Set(current);
      findActiveAccordionIds(visibleMenu, pathname).forEach((itemId) => next.add(itemId));
      return next;
    });
  }, [pathname, visibleMenu]);

  const handleToggleItem = (itemId: string) => {
    setOpenItemIds((current) => {
      const next = new Set(current);
      if (next.has(itemId)) {
        next.delete(itemId);
      } else {
        next.add(itemId);
      }
      return next;
    });
  };

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
                <SidebarItem
                  key={item.id}
                  item={item}
                  pathname={pathname}
                  collapsed={collapsed}
                  openItemIds={openItemIds}
                  onToggleItem={handleToggleItem}
                />
              ))}
            </div>
          )}
        </nav>
      </div>
    </aside>
  );
}
