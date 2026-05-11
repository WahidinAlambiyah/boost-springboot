"use client";

import { useEffect, useMemo, useState, type ReactNode } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";

import {
  BoxCubeIcon,
  CalenderIcon,
  ChevronDownIcon,
  GridIcon,
  HorizontaLDots,
  ListIcon,
  LockIcon,
  PageIcon,
  PieChartIcon,
  PlugInIcon,
  TableIcon,
  UserCircleIcon,
} from "@/app/components/tailadmin";
import { MenuItemResponse } from "@/types/api";
import { useAuthStore } from "@/store/auth";

type FallbackMode = "disabled" | "dev-only" | "safety";

const FALLBACK_MODE = (process.env.NEXT_PUBLIC_MENU_FALLBACK_MODE ?? "dev-only") as FallbackMode;
const ENABLE_MENU_FALLBACK = FALLBACK_MODE === "safety" || (FALLBACK_MODE === "dev-only" && process.env.NODE_ENV !== "production");

const LEGACY_ROUTE_ALIASES: Record<string, string> = {
  "/academies": "/master/academies",
  "/academy-locations": "/master/academy-locations",
  "/students": "/master/students",
  "/coach-profiles": "/master/coaches",
  "/assessment-skills": "/master/assessment-skills",
  "/catalog": "/master/training-packages",
  "/scheduling": "/operations/schedules",
  "/attendance": "/operations/attendance",
  "/enrollment": "/operations/enrollment",
  "/reports/student-progress": "/operations/student-progress",
  "/billing": "/finance/billings",
  "/notification": "/notifications/history",
};

const MENU_LABEL_BY_PATH: Record<string, string> = {
  "/dashboard": "Dashboard",
  "/operations/schedules": "Jadwal Latihan",
  "/operations/attendance": "Absensi",
  "/operations/enrollment": "Pendaftaran",
  "/operations/assessments": "Penilaian Siswa",
  "/operations/student-progress": "Laporan Perkembangan",
  "/master/academies": "Akademi",
  "/master/academy-locations": "Lokasi Akademi",
  "/master/students": "Siswa",
  "/master/coaches": "Coach",
  "/master/training-packages": "Paket Latihan",
  "/master/assessment-skills": "Skill Penilaian",
  "/finance/billings": "Tagihan",
  "/finance/payments": "Pembayaran",
  "/reports/student-progress": "Progress Siswa",
  "/reports/attendance": "Laporan Absensi",
  "/reports/finance": "Laporan Keuangan",
  "/admin/users": "Pengguna",
  "/admin/roles": "Role",
  "/admin/permissions": "Permission",
  "/admin/audit-log": "Audit Log",
  "/notifications/templates": "Template Notifikasi",
  "/notifications/history": "Riwayat Notifikasi",
};

const MENU_LABEL_BY_GROUP: Record<string, string> = {
  Admin: "Administrasi Sistem",
  Operations: "Operasional Latihan",
  Reports: "Laporan",
  Notification: "Notifikasi",
};

const LOCAL_FALLBACK_MENU: MenuItemResponse[] = [
  { id: "local-dashboard", label: "Dashboard", path: "/dashboard", icon: "grid", visible: true, children: [] },
  {
    id: "local-operations",
    label: "Operasional Latihan",
    path: null,
    icon: "calendar",
    visible: true,
    children: [
      { id: "local-operation-schedules", label: "Jadwal Latihan", path: "/operations/schedules", icon: "calendar", visible: true, children: [] },
      { id: "local-operation-attendance", label: "Absensi", path: "/operations/attendance", icon: "table", visible: true, children: [] },
      { id: "local-operation-enrollment", label: "Pendaftaran", path: "/operations/enrollment", icon: "list", visible: true, children: [] },
      { id: "local-operation-student-progress", label: "Laporan Perkembangan", path: "/operations/student-progress", icon: "page", visible: true, children: [] },
    ],
  },
  {
    id: "local-master-data",
    label: "Master Data",
    path: null,
    icon: "box",
    visible: true,
    children: [
      { id: "local-master-academies", label: "Akademi", path: "/master/academies", icon: "box", visible: true, children: [] },
      { id: "local-master-academy-locations", label: "Lokasi Akademi", path: "/master/academy-locations", icon: "page", visible: true, children: [] },
      { id: "local-master-students", label: "Siswa", path: "/master/students", icon: "user", visible: true, children: [] },
      { id: "local-master-coaches", label: "Coach", path: "/master/coaches", icon: "user", visible: true, children: [] },
      { id: "local-master-training-packages", label: "Paket Latihan", path: "/master/training-packages", icon: "table", visible: true, children: [] },
      { id: "local-master-assessment-skills", label: "Skill Penilaian", path: "/master/assessment-skills", icon: "list", visible: true, children: [] },
    ],
  },
  {
    id: "local-finance",
    label: "Keuangan",
    path: null,
    icon: "pie",
    visible: true,
    children: [
      { id: "local-finance-billings", label: "Tagihan", path: "/finance/billings", icon: "pie", visible: true, children: [] },
      { id: "local-finance-payments", label: "Pembayaran", path: "/finance/payments", icon: "table", visible: true, children: [] },
    ],
  },
  {
    id: "local-reports",
    label: "Laporan",
    path: null,
    icon: "pie",
    visible: true,
    children: [
      { id: "local-report-student-progress", label: "Progress Siswa", path: "/reports/student-progress", icon: "page", visible: true, children: [] },
      { id: "local-report-attendance", label: "Laporan Absensi", path: "/reports/attendance", icon: "table", visible: true, children: [] },
      { id: "local-report-finance", label: "Laporan Keuangan", path: "/reports/finance", icon: "pie", visible: true, children: [] },
    ],
  },
  {
    id: "local-admin",
    label: "Administrasi Sistem",
    path: "/admin",
    icon: "lock",
    visible: true,
    children: [
      { id: "local-admin-users", label: "Pengguna", path: "/admin/users", icon: "user", visible: true, children: [] },
      { id: "local-admin-roles", label: "Role", path: "/admin/roles", icon: "lock", visible: true, children: [] },
      { id: "local-admin-permissions", label: "Permission", path: "/admin/permissions", icon: "lock", visible: true, children: [] },
      { id: "local-admin-audit", label: "Audit Log", path: "/admin/audit-log", icon: "page", visible: true, children: [] },
    ],
  },
  {
    id: "local-notifications",
    label: "Notifikasi",
    path: null,
    icon: "plugin",
    visible: true,
    children: [
      { id: "local-notification-templates", label: "Template Notifikasi", path: "/notifications/templates", icon: "plugin", visible: true, children: [] },
      { id: "local-notification-history", label: "Riwayat Notifikasi", path: "/notifications/history", icon: "plugin", visible: true, children: [] },
    ],
  },
];

const normalizeRoutePath = (path?: string | null) => (path ? LEGACY_ROUTE_ALIASES[path] ?? path : path);

const normalizeMenuItem = (item: MenuItemResponse): MenuItemResponse => {
  const normalizedPath = normalizeRoutePath(item.path);
  return {
    ...item,
    path: normalizedPath,
    label: normalizedPath ? MENU_LABEL_BY_PATH[normalizedPath] ?? item.label : MENU_LABEL_BY_GROUP[item.label] ?? item.label,
    children: item.children?.map(normalizeMenuItem) ?? [],
  };
};

const hasVisibleChildren = (item: MenuItemResponse): boolean => item.children?.some((child) => isMenuVisible(child)) ?? false;

const isMenuVisible = (item: MenuItemResponse): boolean => {
  if (item.visible === false) return false;
  if (!item.children || item.children.length === 0) return true;
  return hasVisibleChildren(item);
};

const filterVisibleMenu = (items: MenuItemResponse[]): MenuItemResponse[] =>
  items.filter((item) => isMenuVisible(item)).map((item) => ({ ...item, children: item.children ? filterVisibleMenu(item.children) : [] }));

const flattenMenu = (item: MenuItemResponse): MenuItemResponse[] => [item, ...(item.children?.flatMap((child) => flattenMenu(child)) ?? [])];

const filterMissingFallbackItem = (item: MenuItemResponse, knownPaths: Set<string>): MenuItemResponse | null => {
  if (item.path) {
    return knownPaths.has(normalizeRoutePath(item.path) ?? item.path) ? null : item;
  }

  const children = (item.children ?? []).map((child) => filterMissingFallbackItem(child, knownPaths)).filter(Boolean) as MenuItemResponse[];
  if (children.length === 0) return null;
  return { ...item, children };
};

const mergeMenuWithLocalFallback = (items: MenuItemResponse[]): MenuItemResponse[] => {
  if (!ENABLE_MENU_FALLBACK) return items;

  const knownPaths = new Set(items.flatMap((item) => flattenMenu(item).map((node) => normalizeRoutePath(node.path))).filter(Boolean) as string[]);
  const fallbackItems = LOCAL_FALLBACK_MENU.map((item) => filterMissingFallbackItem(item, knownPaths)).filter(Boolean) as MenuItemResponse[];

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

const isPathActive = (pathname: string, path?: string | null) => {
  const normalizedPathname = normalizeRoutePath(pathname) ?? pathname;
  const normalizedPath = normalizeRoutePath(path);
  return Boolean(normalizedPath) && (normalizedPathname === normalizedPath || normalizedPathname.startsWith(`${normalizedPath}/`));
};

const hasActiveDescendant = (item: MenuItemResponse, pathname: string): boolean => {
  if (isPathActive(pathname, item.path)) return true;
  return item.children?.some((child) => hasActiveDescendant(child, pathname)) ?? false;
};

const findActiveAccordionIds = (items: MenuItemResponse[], pathname: string): string[] => {
  const ids: string[] = [];
  const walk = (item: MenuItemResponse) => {
    const children = item.children ?? [];
    if (children.length > 0 && children.some((child) => hasActiveDescendant(child, pathname))) ids.push(item.id);
    children.forEach(walk);
  };
  items.forEach(walk);
  return ids;
};

const normalizeIconKey = (value?: string | null) => (value ?? "").toLowerCase().replace(/[_\s-]/g, "");

const getMenuIcon = (item: MenuItemResponse): ReactNode => {
  const key = normalizeIconKey(item.icon || item.label);

  if (key.includes("dashboard") || key.includes("grid")) return <GridIcon />;
  if (key.includes("admin") || key.includes("role") || key.includes("permission") || key.includes("lock") || key.includes("shield") || key.includes("administrasi")) return <LockIcon />;
  if (key.includes("user") || key.includes("student") || key.includes("coach") || key.includes("profile") || key.includes("siswa") || key.includes("pengguna")) return <UserCircleIcon />;
  if (key.includes("calendar") || key.includes("schedule") || key.includes("session") || key.includes("jadwal")) return <CalenderIcon />;
  if (key.includes("table") || key.includes("attendance") || key.includes("absensi") || key.includes("payment") || key.includes("pembayaran")) return <TableIcon />;
  if (key.includes("report") || key.includes("chart") || key.includes("billing") || key.includes("finance") || key.includes("laporan") || key.includes("keuangan") || key.includes("tagihan")) return <PieChartIcon />;
  if (key.includes("catalog") || key.includes("master") || key.includes("academy") || key.includes("akademi") || key.includes("box") || key.includes("paket")) return <BoxCubeIcon />;
  if (key.includes("notification") || key.includes("notifikasi") || key.includes("plugin")) return <PlugInIcon />;
  if (key.includes("page") || key.includes("audit") || key.includes("progress")) return <PageIcon />;
  return <ListIcon />;
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
  const itemBaseClassName = "menu-item group flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-all duration-200";
  const activeClassName = isActive || isGroupActive ? "bg-blue-50 text-blue-700" : "text-gray-700 hover:bg-gray-50 hover:text-blue-700";
  const collapsedClassName = collapsed && level === 0 ? "justify-center" : "justify-start";
  const iconClassName = isActive || isGroupActive ? "text-blue-700" : "text-gray-500 group-hover:text-blue-700";

  const itemIcon = <span className={`menu-item-icon flex h-6 w-6 shrink-0 items-center justify-center ${iconClassName}`}>{getMenuIcon(item)}</span>;
  const itemText = showText ? <span className="menu-item-text truncate">{item.label}</span> : null;

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
          {showText ? <ChevronDownIcon className={`ml-auto h-5 w-5 transition-transform duration-200 ${isOpen ? "rotate-180 text-blue-600" : "text-gray-400"}`} /> : null}
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
        <div id={`sidebar-accordion-${item.id}`} className={`grid transition-all duration-300 ${isOpen ? "grid-rows-[1fr]" : "grid-rows-[0fr]"}`}>
          <div className="overflow-hidden">
            <div className="ml-9 flex flex-col gap-1 pt-1">
              {children.map((child) => (
                <SidebarItem key={child.id} item={child} pathname={pathname} level={level + 1} collapsed={collapsed} openItemIds={openItemIds} onToggleItem={onToggleItem} />
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
  const visibleMenu = useMemo(() => filterVisibleMenu(mergeMenuWithLocalFallback(menu.map(normalizeMenuItem))), [menu]);
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
      if (next.has(itemId)) next.delete(itemId);
      else next.add(itemId);
      return next;
    });
  };

  return (
    <aside className={`fixed left-0 top-0 z-50 flex h-screen flex-col border-r border-gray-200 bg-white px-5 text-gray-900 transition-all duration-300 ease-in-out ${collapsed ? "w-[90px]" : "w-[290px]"}`} aria-label="Sidebar navigation">
      <div className={`flex py-8 ${collapsed ? "justify-center" : "justify-start"}`}>
        <Link href="/dashboard" className="flex items-center gap-3">
          <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-600 text-sm font-bold text-white shadow-sm">B</span>
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
            {collapsed ? <HorizontaLDots className="h-6 w-6" /> : "Menu Utama"}
          </h2>
          {visibleMenu.length === 0 ? (
            <div className="rounded-lg border border-dashed border-gray-200 bg-gray-50 px-3 py-4 text-sm text-gray-500">{collapsed ? "-" : "Tidak ada menu yang tersedia."}</div>
          ) : (
            <div className="flex flex-col gap-1.5">
              {visibleMenu.map((item) => (
                <SidebarItem key={item.id} item={item} pathname={pathname} collapsed={collapsed} openItemIds={openItemIds} onToggleItem={handleToggleItem} />
              ))}
            </div>
          )}
        </nav>
      </div>
    </aside>
  );
}
