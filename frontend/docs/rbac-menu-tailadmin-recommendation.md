# RBAC Menu Recommendation for TailAdmin Layout

Dokumen ini berisi rekomendasi struktur menu RBAC agar sidebar TailAdmin tampil rapi sebagai accordion dan tetap mengikuti permission backend.

## Prinsip RBAC Menu

- Menu root dipakai sebagai group accordion.
- Child menu dipakai sebagai halaman/module.
- Visibility ditentukan backend berdasarkan role/permission user.
- Frontend tidak memutuskan permission utama; frontend hanya render menu yang dikirim backend.
- Route guard tetap wajib di level page menggunakan `RequirePermission`.

## Recommended Root Menu

### Dashboard

Root langsung ke halaman dashboard.

Permission minimal:

- `DASHBOARD_OWNER_READ`
- fallback sementara: `ACADEMY_READ`, `REPORT_PROGRESS_READ`

Route:

- `/dashboard`

### Admin

Untuk manajemen security dan audit.

Recommended children:

| Label | Route | Permission Read | Permission Write |
| --- | --- | --- | --- |
| User Management | `/admin/users` | `USER_READ` | `USER_WRITE` |
| Role Management | `/admin/roles` | `ROLE_READ` | `ROLE_WRITE` |
| Permission Management | `/admin/permissions` | `PERMISSION_READ` | `PERMISSION_WRITE` |
| Menu Management | `/admin/menus` | `MENU_READ` | `MENU_WRITE` |
| Audit Log | `/admin/audit-log` | `AUDIT_READ` | - |

Catatan:

- CRUD User sebaiknya memiliki assign role dan enable/disable user.
- CRUD Role sebaiknya memiliki assign permissions.
- CRUD Permission biasanya read-only/manual seed, kecuali super admin.
- Menu Management optional, dipakai jika menu ingin configurable dari UI.

### Master Data

Untuk data academy dan profil operasional.

Recommended children:

| Label | Route | Permission Read | Permission Write |
| --- | --- | --- | --- |
| Academies | `/academies` | `ACADEMY_READ` | `ACADEMY_WRITE` |
| Academy Locations | `/academy-locations` | `LOCATION_READ` | `LOCATION_WRITE` |
| Coach Profiles | `/coach-profiles` | `COACH_READ` | `COACH_WRITE` |
| Students | `/students` | `STUDENT_READ` | `STUDENT_WRITE` |
| Assessment Skills | `/assessment-skills` | `ASSESSMENT_READ` | `ASSESSMENT_WRITE` |

### Operations

Untuk flow harian academy.

Recommended children:

| Label | Route | Permission Read | Permission Write |
| --- | --- | --- | --- |
| Catalog | `/catalog` | `CLASS_READ`, `PROGRAM_READ`, `PACKAGE_READ` | corresponding write permissions |
| Scheduling | `/scheduling` | `SCHEDULE_READ`, `SESSION_READ` | `SCHEDULE_WRITE`, `SESSION_WRITE`, `SESSION_RESCHEDULE` |
| Enrollment | `/enrollment` | `ENROLLMENT_READ` | `ENROLLMENT_WRITE` |
| Attendance | `/attendance` | `ATTENDANCE_READ` | `ATTENDANCE_MARK` |
| Billing | `/billing` | `BILLING_READ`, `PAYMENT_RECORD` | `BILLING_WRITE`, `REFUND_APPROVE` |
| Notification | `/notification` | `NOTIFICATION_READ` | `NOTIFICATION_WRITE` |

### Reports

Untuk semua report dan export.

Recommended children:

| Label | Route | Permission |
| --- | --- | --- |
| Student Progress | `/reports/student-progress` | `REPORT_PROGRESS_READ` |
| Export Reports | `/reports/export` | `REPORT_EXPORT` |
| Event Reports | `/reports/events` | `REPORT_EVENT_READ`, `DASHBOARD_EVENT_READ` |

### Developer Tools

Hanya untuk development/internal.

Route:

- `/dev`

Permission:

- `DEV_TOOLS_READ`, atau fallback sementara `ROLE_READ`/`USER_READ` sesuai existing implementation.

## Suggested Backend Menu Tree

Contoh bentuk response yang ideal untuk `/api/me/menu` atau endpoint menu sejenis:

```json
[
  {
    "id": "dashboard",
    "label": "Dashboard",
    "path": "/dashboard",
    "icon": "grid",
    "visible": true,
    "children": []
  },
  {
    "id": "admin",
    "label": "Admin",
    "path": "/admin",
    "icon": "shield",
    "visible": true,
    "children": [
      { "id": "admin-users", "label": "User Management", "path": "/admin/users", "icon": "user", "visible": true, "children": [] },
      { "id": "admin-roles", "label": "Role Management", "path": "/admin/roles", "icon": "key", "visible": true, "children": [] },
      { "id": "admin-permissions", "label": "Permission Management", "path": "/admin/permissions", "icon": "lock", "visible": true, "children": [] },
      { "id": "admin-audit", "label": "Audit Log", "path": "/admin/audit-log", "icon": "audit", "visible": true, "children": [] }
    ]
  },
  {
    "id": "master-data",
    "label": "Master Data",
    "path": null,
    "icon": "database",
    "visible": true,
    "children": [
      { "id": "academies", "label": "Academies", "path": "/academies", "icon": "building", "visible": true, "children": [] },
      { "id": "academy-locations", "label": "Academy Locations", "path": "/academy-locations", "icon": "map", "visible": true, "children": [] },
      { "id": "coach-profiles", "label": "Coach Profiles", "path": "/coach-profiles", "icon": "coach", "visible": true, "children": [] },
      { "id": "students", "label": "Students", "path": "/students", "icon": "students", "visible": true, "children": [] },
      { "id": "assessment-skills", "label": "Assessment Skills", "path": "/assessment-skills", "icon": "check", "visible": true, "children": [] }
    ]
  }
]
```

## Frontend Accordion Behavior

Implemented behavior:

- Root/group menu with children becomes accordion button.
- Active child automatically opens its parent group.
- Collapsed sidebar hides children and shows compact initials.
- Expanded sidebar shows chevron and child menu.
- Menu continues to render backend-provided `MenuItemResponse`.

## Next Implementation Priority

1. Create pages for `/admin/users`, `/admin/roles`, `/admin/permissions`, `/admin/audit-log` as TailAdmin-styled placeholders first.
2. Add services when backend endpoints are ready.
3. Add route guards:
   - User Management: `USER_READ`
   - Role Management: `ROLE_READ`
   - Permission Management: `PERMISSION_READ`
   - Audit Log: `AUDIT_READ`
4. Later add forms/actions based on write permissions.
