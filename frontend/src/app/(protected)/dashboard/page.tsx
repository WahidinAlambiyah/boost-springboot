"use client";

import { useMemo, useState } from "react";
import Link from "next/link";
import { useQuery, useQueryClient } from "@tanstack/react-query";

import AppShell from "@/app/components/app-shell";
import EmptyState from "@/app/components/empty-state";
import { ErrorMessage } from "@/app/components/error-message";
import LoadingSkeleton from "@/app/components/loading-skeleton";
import { PageHeader } from "@/app/components/page-header";
import RequirePermission from "@/app/components/require-permission";
import { SectionCard } from "@/app/components/section-card";
import { StatCard } from "@/app/components/stat-card";
import { StatusBadge } from "@/app/components/status-badge";
import { DEV_TOOLS_READ_PERMISSIONS } from "@/app/dev/permissions";
import { academyService } from "@/features/academies/academy.service";
import { dashboardService } from "@/features/dashboard/dashboard.service";
import type { OwnerDashboardResponse } from "@/lib/api-types";
import { ENABLE_DEV_TOOLS } from "@/lib/dev-tools";
import { formatCurrencyIDR, formatDate } from "@/lib/formatters";
import { canAny } from "@/lib/permissions";
import { SCHEDULING_SIDEBAR_PERMISSIONS } from "@/lib/permission-mapping";
import { QUERY_KEYS } from "@/lib/query-keys";
import { useAuthStore } from "@/store/auth";

interface ModuleLink {
  label: string;
  href: string;
  permissions: string[];
  description?: string;
}

interface DashboardActionLink {
  label: string;
  href: string;
}

const dashboardActionLinks: DashboardActionLink[] = [
  { label: "Add Student", href: "/students" },
  { label: "Create Session", href: "/class-sessions" },
  { label: "Mark Attendance", href: "/attendance" },
  { label: "Generate Report", href: "/reports/student-progress" },
];

const moduleLinks: ModuleLink[] = [
  {
    label: "Catalog",
    href: "/catalog",
    permissions: ["CLASS_READ", "CLASS_WRITE"],
  },
  {
    label: "Scheduling",
    href: "/scheduling",
    permissions: [...SCHEDULING_SIDEBAR_PERMISSIONS],
  },
  {
    label: "Enrollment",
    href: "/enrollment",
    permissions: ["ENROLLMENT_READ", "ENROLLMENT_WRITE"],
  },
  {
    label: "Attendance",
    href: "/attendance",
    permissions: ["ATTENDANCE_READ", "ATTENDANCE_MARK"],
  },
  {
    label: "Billing",
    href: "/billing",
    permissions: ["BILLING_READ", "BILLING_WRITE"],
  },
  {
    label: "Notification",
    href: "/notification",
    permissions: ["NOTIFICATION_READ", "NOTIFICATION_WRITE"],
  },
  ...(ENABLE_DEV_TOOLS
    ? [
        {
          label: "Developer Tools",
          href: "/dev",
          // TODO: remove ROLE_READ/USER_READ fallback from DEV_TOOLS_READ_PERMISSIONS when backend provides DEV_TOOLS_READ.
          permissions: [...DEV_TOOLS_READ_PERMISSIONS],
          description: "Akses halaman demo internal untuk komponen, CRUD, tabel, dan form.",
        },
      ]
    : []),
];

// TODO: keep ACADEMY_READ/REPORT_PROGRESS_READ fallback until DASHBOARD_OWNER_READ is fully rolled out.
const DASHBOARD_OWNER_READ_PERMISSIONS = ["DASHBOARD_OWNER_READ", "ACADEMY_READ", "REPORT_PROGRESS_READ"] as const;

const inputClassName =
  "min-h-11 w-full rounded-md border border-zinc-300 bg-white px-3 py-2 text-sm text-zinc-900 shadow-sm focus:border-zinc-900 focus:outline-none focus:ring-1 focus:ring-zinc-900 disabled:bg-zinc-100 disabled:text-zinc-500";

const buttonClassName =
  "inline-flex items-center justify-center rounded-md border border-zinc-300 bg-white px-3 py-2 text-sm font-medium text-zinc-700 transition hover:bg-zinc-50";

const statGridClassName = "grid gap-4 sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-6";

const formatTime = (value?: string | null) => (value ? value.slice(0, 5) : "-");

const formatPercentage = (value?: number | null) => `${(value ?? 0).toFixed(2)}%`;

const normalizeReason = (value?: string | null) => {
  if (!value) {
    return "-";
  }

  return value
    .toLowerCase()
    .split("_")
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
};

const safeNumber = (value?: number | null) => (typeof value === "number" && Number.isFinite(value) ? value : 0);

const buildProgressWidth = (value?: number | null) => {
  const normalized = safeNumber(value);
  const clamped = Math.max(0, Math.min(100, normalized));
  return `${clamped.toFixed(2)}%`;
};

const deriveAssessmentCompletionRate = (dashboard?: OwnerDashboardResponse) => {
  const explicitRate = dashboard?.assessmentCompletion?.completionRate;
  if (typeof explicitRate === "number" && Number.isFinite(explicitRate)) {
    return explicitRate;
  }

  const total = safeNumber(dashboard?.summaryCards.activeStudents);
  const pending = safeNumber(dashboard?.summaryCards.pendingAssessments);
  const assessed = Math.max(0, total - pending);
  if (total <= 0) {
    return 0;
  }
  return Number(((assessed * 100) / total).toFixed(2));
};

export default function DashboardPage() {
  const authorities = useAuthStore((state) => state.authorities);
  const queryClient = useQueryClient();

  const [academyId, setAcademyId] = useState("");
  const [fromDate, setFromDate] = useState("");
  const [toDate, setToDate] = useState("");

  const dashboardFilters = useMemo(
    () => ({
      academyId: academyId || undefined,
      from: fromDate || undefined,
      to: toDate || undefined,
    }),
    [academyId, fromDate, toDate],
  );

  const allowedModules = useMemo(
    () => moduleLinks.filter((module) => canAny(authorities, module.permissions)),
    [authorities],
  );

  const academiesQuery = useQuery({
    queryKey: QUERY_KEYS.academies.list(),
    queryFn: () => academyService.list(),
  });

  const dashboardQuery = useQuery({
    queryKey: QUERY_KEYS.dashboard.owner(dashboardFilters),
    queryFn: () => dashboardService.getOwnerDashboard(dashboardFilters),
  });

  const dashboardData = dashboardQuery.data;
  const assessmentRate = deriveAssessmentCompletionRate(dashboardData);

  const assessedStudents = safeNumber(dashboardData?.assessmentCompletion?.assessedStudents);
  const totalStudentsForAssessment = safeNumber(
    dashboardData?.assessmentCompletion?.totalActiveStudents ?? dashboardData?.summaryCards.activeStudents,
  );
  const notAssessedStudents = safeNumber(
    dashboardData?.assessmentCompletion?.notAssessedStudents ??
      Math.max(0, totalStudentsForAssessment - assessedStudents),
  );

  const attendanceTrendRows = dashboardData?.attendanceTrend ?? [];
  const levelDistributionRows = dashboardData?.levelDistribution ?? [];
  const packageSummary = dashboardData?.packageSummary;
  const financeSummary = dashboardData?.financeSummary;
  const payrollSummary = dashboardData?.payrollSummary;

  return (
    <RequirePermission permissions={[...DASHBOARD_OWNER_READ_PERMISSIONS]} mode="any">
      <AppShell>
        <PageHeader
          title="Dashboard Academy"
          description="Ringkasan operasional latihan, progress anak, event, dan keuangan."
          actions={
            <div className="grid w-full grid-cols-2 gap-2 sm:flex sm:w-auto sm:flex-wrap">
              {dashboardActionLinks.map((action) => (
                <Link key={action.href} href={action.href} className={buttonClassName}>
                  {action.label}
                </Link>
              ))}
            </div>
          }
        />

        <SectionCard title="Filter" description="Filter academy dan periode untuk mengatur cakupan data dashboard.">
          <div className="grid grid-cols-1 gap-3 md:grid-cols-2 xl:grid-cols-4">
            <label className="space-y-1.5">
              <span className="text-sm font-medium text-zinc-800">Academy</span>
              <select
                value={academyId}
                onChange={(event) => setAcademyId(event.target.value)}
                className={inputClassName}
                disabled={academiesQuery.isLoading}
              >
                <option value="">Semua academy</option>
                {(academiesQuery.data ?? []).map((academy) => (
                  <option key={academy.id} value={academy.id}>
                    {academy.name}
                  </option>
                ))}
              </select>
              {academiesQuery.isError ? <p className="text-xs text-red-600">Gagal memuat academy list.</p> : null}
            </label>

            <label className="space-y-1.5">
              <span className="text-sm font-medium text-zinc-800">From</span>
              <input type="date" value={fromDate} onChange={(event) => setFromDate(event.target.value)} className={inputClassName} />
            </label>

            <label className="space-y-1.5">
              <span className="text-sm font-medium text-zinc-800">To</span>
              <input type="date" value={toDate} onChange={(event) => setToDate(event.target.value)} className={inputClassName} />
            </label>

            <div className="space-y-1.5">
              <span className="text-sm font-medium text-zinc-800">Aksi</span>
              <button
                type="button"
                className={`${buttonClassName} min-h-11 w-full justify-center`}
                onClick={() => {
                  void dashboardQuery.refetch();
                  void queryClient.invalidateQueries({ queryKey: QUERY_KEYS.dashboard.owner(dashboardFilters) });
                }}
              >
                Refresh
              </button>
            </div>
          </div>
        </SectionCard>

        {dashboardQuery.isLoading ? (
          <SectionCard className="mt-6" title="Memuat Dashboard">
            <LoadingSkeleton rows={8} />
          </SectionCard>
        ) : null}

        {dashboardQuery.isError ? (
          <SectionCard className="mt-6">
            <ErrorMessage
              title="Gagal memuat dashboard"
              message="Data dashboard owner belum bisa diambil. Coba refresh atau ubah filter periode."
            />
          </SectionCard>
        ) : null}

        {dashboardData ? (
          <div className="mt-6 space-y-6">
            <section className={statGridClassName}>
              <StatCard title="Murid Aktif" value={safeNumber(dashboardData.summaryCards.activeStudents)} />
              <StatCard title="Coach Aktif" value={safeNumber(dashboardData.summaryCards.activeCoaches)} />
              <StatCard title="Jadwal Hari Ini" value={safeNumber(dashboardData.summaryCards.todaySessions)} />
              <StatCard title="Attendance Rate" value={formatPercentage(safeNumber(dashboardData.summaryCards.attendanceRate))} />
              <StatCard title="Pending Assessment" value={safeNumber(dashboardData.summaryCards.pendingAssessments)} />
              <StatCard title="Event Mendatang" value={safeNumber(dashboardData.summaryCards.upcomingEvents)} />
            </section>

            <SectionCard title="Today Sessions" description="Pantau jadwal latihan hari ini beserta status attendance.">
              {(dashboardData.todaySessions ?? []).length === 0 ? (
                <EmptyState title="Belum ada sesi hari ini" description="Tidak ada class session untuk tanggal hari ini." />
              ) : (
                <div className="overflow-x-auto">
                  <table className="min-w-full text-left text-sm">
                    <thead className="border-b border-zinc-200 text-xs uppercase tracking-wide text-zinc-500">
                      <tr>
                        <th className="px-3 py-2">Time</th>
                        <th className="px-3 py-2">Class</th>
                        <th className="px-3 py-2">Location</th>
                        <th className="px-3 py-2">Coach</th>
                        <th className="px-3 py-2">Attendance Status</th>
                        <th className="px-3 py-2">Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      {dashboardData.todaySessions.map((session) => (
                        <tr key={session.id} className="border-b border-zinc-100">
                          <td className="px-3 py-2 font-medium text-zinc-900">
                            {formatTime(session.startTime)} - {formatTime(session.endTime)}
                          </td>
                          <td className="px-3 py-2 text-zinc-700">{session.classGroupName || session.title}</td>
                          <td className="px-3 py-2 text-zinc-700">{session.locationName || "-"}</td>
                          <td className="px-3 py-2 text-zinc-700">{session.coachNames || "-"}</td>
                          <td className="px-3 py-2">
                            <StatusBadge status={session.attendanceStatus} />
                          </td>
                          <td className="px-3 py-2">
                            <StatusBadge status={session.status} />
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </SectionCard>

            <div className="grid gap-6 xl:grid-cols-2">
              <SectionCard title="Students Need Attention" description="Murid yang belum memiliki assessment pada periode berjalan.">
                {(dashboardData.studentsNeedAttention ?? []).length === 0 ? (
                  <EmptyState title="Semua murid aman" description="Tidak ada murid yang memerlukan perhatian khusus saat ini." />
                ) : (
                  <ul className="space-y-3">
                    {dashboardData.studentsNeedAttention.map((student) => (
                      <li key={student.studentId} className="rounded-md border border-zinc-200 bg-zinc-50 p-3">
                        <div className="flex flex-wrap items-start justify-between gap-2">
                          <div>
                            <p className="text-sm font-semibold text-zinc-900">{student.studentName}</p>
                            <p className="text-xs text-zinc-600">{normalizeReason(student.reason)}</p>
                          </div>
                          <Link href={`/reports/student-progress?studentId=${student.studentId}`} className={buttonClassName}>
                            Lihat Report
                          </Link>
                        </div>
                        <div className="mt-2 grid gap-1 text-xs text-zinc-600 sm:grid-cols-2">
                          <p>Last attendance: {formatDate(student.lastAttendanceDate)}</p>
                          <p>Last assessment: {formatDate(student.lastAssessmentDate)}</p>
                        </div>
                      </li>
                    ))}
                  </ul>
                )}
              </SectionCard>

              <SectionCard title="Assessment Completion" description="Progress completion assessment murid aktif pada periode terpilih.">
                <div className="space-y-3">
                  <div className="h-3 w-full overflow-hidden rounded-full bg-zinc-200">
                    <div className="h-full rounded-full bg-zinc-900 transition-all" style={{ width: buildProgressWidth(assessmentRate) }} />
                  </div>
                  <div className="flex flex-wrap items-center justify-between gap-2 text-sm text-zinc-700">
                    <span>
                      {assessedStudents} assessed / {totalStudentsForAssessment} active students
                    </span>
                    <span className="font-semibold text-zinc-900">{formatPercentage(assessmentRate)}</span>
                  </div>
                  <div className="grid gap-2 sm:grid-cols-2">
                    <div className="rounded-md border border-zinc-200 bg-zinc-50 p-3">
                      <p className="text-xs uppercase tracking-wide text-zinc-500">Assessed</p>
                      <p className="mt-1 text-lg font-semibold text-zinc-900">{assessedStudents}</p>
                    </div>
                    <div className="rounded-md border border-zinc-200 bg-zinc-50 p-3">
                      <p className="text-xs uppercase tracking-wide text-zinc-500">Not Assessed</p>
                      <p className="mt-1 text-lg font-semibold text-zinc-900">{notAssessedStudents}</p>
                    </div>
                  </div>
                </div>
              </SectionCard>
            </div>

            <div className="grid gap-6 xl:grid-cols-2">
              <SectionCard title="Upcoming Events" description="Agenda event mendatang untuk academy terpilih.">
                {(dashboardData.upcomingEvents ?? []).length === 0 ? (
                  <EmptyState title="Belum ada event" description="Belum ada event mendatang untuk periode ini." />
                ) : (
                  <ul className="space-y-3">
                    {dashboardData.upcomingEvents.map((event) => (
                      <li key={event.id} className="rounded-md border border-zinc-200 bg-zinc-50 p-3">
                        <div className="flex items-start justify-between gap-3">
                          <div>
                            <p className="text-sm font-semibold text-zinc-900">{event.title}</p>
                            <p className="text-xs text-zinc-600">
                              {formatDate(event.eventDate)} • {event.locationName || "-"}
                            </p>
                          </div>
                          <StatusBadge status={event.status} />
                        </div>
                        <p className="mt-2 text-xs text-zinc-600">
                          Quota/Registered: {safeNumber(event.quota)} / {safeNumber(event.registeredCount)}
                        </p>
                      </li>
                    ))}
                  </ul>
                )}
              </SectionCard>

              <SectionCard title="Recent Assessments" description="Assessment terbaru untuk monitoring kualitas coaching.">
                {(dashboardData.recentAssessments ?? []).length === 0 ? (
                  <EmptyState title="Belum ada assessment" description="Belum ada data assessment terbaru di periode ini." />
                ) : (
                  <ul className="space-y-3">
                    {dashboardData.recentAssessments.map((assessment) => (
                      <li key={assessment.assessmentId} className="rounded-md border border-zinc-200 bg-zinc-50 p-3">
                        <div className="flex flex-wrap items-center justify-between gap-2">
                          <p className="text-sm font-semibold text-zinc-900">{assessment.studentName}</p>
                          <span className="text-xs text-zinc-600">{formatDate(assessment.sessionDate)}</span>
                        </div>
                        <p className="mt-1 text-xs text-zinc-600">Coach: {assessment.coachName || "-"}</p>
                        <p className="mt-2 text-xs text-zinc-700">Notes: {assessment.overallNotes || "-"}</p>
                        <p className="mt-1 text-xs text-zinc-700">Recommendation: {assessment.recommendation || "-"}</p>
                      </li>
                    ))}
                  </ul>
                )}
              </SectionCard>
            </div>

            <div className="grid gap-6 xl:grid-cols-3">
              <SectionCard title="Attendance Trend" description="Ringkasan trend kehadiran harian sesuai periode filter.">
                {attendanceTrendRows.length === 0 ? (
                  <EmptyState title="Belum ada trend" description="Data attendance trend belum tersedia pada periode ini." />
                ) : (
                  <div className="max-h-64 overflow-auto">
                    <table className="min-w-full text-left text-xs">
                      <thead className="border-b border-zinc-200 text-zinc-500">
                        <tr>
                          <th className="px-2 py-2">Date</th>
                          <th className="px-2 py-2">Present</th>
                          <th className="px-2 py-2">Absent</th>
                          <th className="px-2 py-2">Permit</th>
                          <th className="px-2 py-2">Sick</th>
                          <th className="px-2 py-2">Late</th>
                          <th className="px-2 py-2">Total</th>
                        </tr>
                      </thead>
                      <tbody>
                        {attendanceTrendRows.map((item) => (
                          <tr key={item.date} className="border-b border-zinc-100">
                            <td className="px-2 py-2">{formatDate(item.date)}</td>
                            <td className="px-2 py-2">{safeNumber(item.present)}</td>
                            <td className="px-2 py-2">{safeNumber(item.absent)}</td>
                            <td className="px-2 py-2">{safeNumber(item.permit)}</td>
                            <td className="px-2 py-2">{safeNumber(item.sick)}</td>
                            <td className="px-2 py-2">{safeNumber(item.late)}</td>
                            <td className="px-2 py-2 font-semibold">{safeNumber(item.total)}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </SectionCard>

              <SectionCard title="Level Distribution" description="Distribusi murid aktif berdasarkan level saat ini.">
                {levelDistributionRows.length === 0 ? (
                  <EmptyState title="Belum ada distribusi" description="Belum ada data level murid aktif." />
                ) : (
                  <ul className="space-y-2">
                    {levelDistributionRows.map((item) => (
                      <li key={`${item.level}-${item.totalStudents}`} className="flex items-center justify-between rounded-md border border-zinc-200 px-3 py-2 text-sm">
                        <span className="font-medium text-zinc-800">{item.level || "UNKNOWN"}</span>
                        <span className="text-zinc-600">{safeNumber(item.totalStudents)} murid</span>
                      </li>
                    ))}
                  </ul>
                )}
              </SectionCard>

              <SectionCard title="Package, Finance, Payroll" description="Snapshot singkat operasional paket, keuangan, dan payroll bulan ini.">
                <div className="space-y-3">
                  <div className="rounded-md border border-zinc-200 bg-zinc-50 p-3">
                    <p className="text-xs uppercase tracking-wide text-zinc-500">Package Summary</p>
                    <p className="mt-2 text-sm text-zinc-700">Active: {safeNumber(packageSummary?.activeSubscriptions)}</p>
                    <p className="text-sm text-zinc-700">Expiring Soon: {safeNumber(packageSummary?.expiringSoon)}</p>
                    <p className="text-sm text-zinc-700">Low Remaining Sessions: {safeNumber(packageSummary?.lowRemainingSessions)}</p>
                  </div>

                  <div className="rounded-md border border-zinc-200 bg-zinc-50 p-3">
                    <p className="text-xs uppercase tracking-wide text-zinc-500">Finance Summary</p>
                    <p className="mt-2 text-sm text-zinc-700">Paid: {formatCurrencyIDR(financeSummary?.paidAmount)}</p>
                    <p className="text-sm text-zinc-700">Unpaid: {formatCurrencyIDR(financeSummary?.unpaidAmount)}</p>
                    <p className="text-sm text-zinc-700">Overdue: {formatCurrencyIDR(financeSummary?.overdueAmount)}</p>
                    <p className="text-sm text-zinc-700">Total Invoices: {safeNumber(financeSummary?.totalInvoices)}</p>
                  </div>

                  <div className="rounded-md border border-zinc-200 bg-zinc-50 p-3">
                    <p className="text-xs uppercase tracking-wide text-zinc-500">Payroll Summary</p>
                    <div className="mt-2 flex items-center justify-between gap-2">
                      <p className="text-sm text-zinc-700">
                        Period: {safeNumber(payrollSummary?.periodMonth)}/{safeNumber(payrollSummary?.periodYear)}
                      </p>
                      <StatusBadge status={payrollSummary?.status || "NOT_GENERATED"} />
                    </div>
                    <p className="mt-1 text-sm text-zinc-700">Total Coaches: {safeNumber(payrollSummary?.totalCoaches)}</p>
                    <p className="text-sm text-zinc-700">Total Amount: {formatCurrencyIDR(payrollSummary?.totalAmount)}</p>
                  </div>
                </div>
              </SectionCard>
            </div>

          </div>
        ) : null}

        <SectionCard className="mt-6" title="Quick Links" description="Akses cepat ke modul operasional utama.">
          <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
            {allowedModules.length > 0 ? (
              allowedModules.map((module) => (
                <Link
                  key={module.href}
                  href={module.href}
                  className="rounded-lg border border-zinc-200 bg-zinc-50 px-4 py-3 text-sm font-medium text-zinc-800 transition-colors hover:bg-zinc-100"
                >
                  <span>{module.label}</span>
                  {module.description ? (
                    <span className="mt-1 block text-xs font-normal leading-5 text-zinc-600">{module.description}</span>
                  ) : null}
                </Link>
              ))
            ) : (
              <p className="text-sm text-zinc-600">Tidak ada modul yang dapat diakses.</p>
            )}
          </div>
        </SectionCard>
      </AppShell>
    </RequirePermission>
  );
}
