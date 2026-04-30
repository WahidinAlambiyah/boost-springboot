-- =========================================================
-- RBAC Learning v1 Seed
-- =========================================================
-- Jalankan dalam transaction (opsional)
begin;

-- 1) Ensure roles exist
insert into fastworks_springboot.roles (code, name, description, is_active, created_at, updated_at)
values
    ('ADMIN',      'Administrator', 'Full access', true, now(), now()),
    ('INSTRUCTOR', 'Instructor',    'Teaching and class operations', true, now(), now()),
    ('GUARDIAN',   'Guardian',      'Parent/guardian access to child data', true, now(), now()),
    ('OPS',        'Operations',    'Operational and scheduling management', true, now(), now()),
    ('FINANCE',    'Finance',       'Billing and payment management', true, now(), now())
on conflict (code) do nothing;

-- 2) Insert permissions (Learning domain)
insert into fastworks_springboot.permissions (code, name, description, module, is_active, created_at, updated_at)
values
    -- Catalog
    ('PROGRAM_READ',     'Program Read',     'Read programs',                          'CATALOG',    true, now(), now()),
    ('PROGRAM_WRITE',    'Program Write',    'Create/update programs',                 'CATALOG',    true, now(), now()),
    ('COURSE_READ',      'Course Read',      'Read courses',                           'CATALOG',    true, now(), now()),
    ('COURSE_WRITE',     'Course Write',     'Create/update courses',                  'CATALOG',    true, now(), now()),
    ('CLASS_READ',       'Class Read',       'Read class groups',                      'DELIVERY',   true, now(), now()),
    ('CLASS_WRITE',      'Class Write',      'Create/update class groups',             'DELIVERY',   true, now(), now()),

    -- Scheduling canonical
    ('SCHEDULE_READ',       'Schedule Read',       'Read class schedules',                  'SCHEDULING', true, now(), now()),
    ('SCHEDULE_WRITE',      'Schedule Write',      'Create/update class schedules',         'SCHEDULING', true, now(), now()),
    ('SCHEDULE_RESCHEDULE', 'Schedule Reschedule', 'Reschedule or cancel class schedules',  'SCHEDULING', true, now(), now()),

    -- Scheduling legacy alias (deprecated, transitional compatibility)
    ('SESSION_READ',        'Session Read (Deprecated)',        'Deprecated alias for SCHEDULE_READ',        'SCHEDULING', true, now(), now()),
    ('SESSION_WRITE',       'Session Write (Deprecated)',       'Deprecated alias for SCHEDULE_WRITE',       'SCHEDULING', true, now(), now()),
    ('SESSION_RESCHEDULE',  'Session Reschedule (Deprecated)',  'Deprecated alias for SCHEDULE_RESCHEDULE',  'SCHEDULING', true, now(), now()),

    -- Student / guardian
    ('STUDENT_READ',     'Student Read',     'Read student profiles',                  'STUDENT',    true, now(), now()),
    ('STUDENT_WRITE',    'Student Write',    'Create/update student profiles',         'STUDENT',    true, now(), now()),
    ('GUARDIAN_READ',    'Guardian Read',    'Read guardian profiles',                 'STUDENT',    true, now(), now()),
    ('GUARDIAN_WRITE',   'Guardian Write',   'Create/update guardian profiles',        'STUDENT',    true, now(), now()),

    -- Enrollment
    ('ENROLLMENT_READ',  'Enrollment Read',  'Read enrollments',                       'ENROLLMENT', true, now(), now()),
    ('ENROLLMENT_WRITE', 'Enrollment Write', 'Create/update enrollment status',        'ENROLLMENT', true, now(), now()),
    ('WAITLIST_MANAGE',  'Waitlist Manage',  'Manage waitlist entries',                'ENROLLMENT', true, now(), now()),

    -- Attendance & progress
    ('ATTENDANCE_READ',  'Attendance Read',  'Read attendance records',                'ATTENDANCE', true, now(), now()),
    ('ATTENDANCE_MARK',  'Attendance Mark',  'Mark attendance',                        'ATTENDANCE', true, now(), now()),
    ('PROGRESS_READ',    'Progress Read',    'Read progress notes/results',            'PROGRESS',   true, now(), now()),
    ('PROGRESS_WRITE',   'Progress Write',   'Write progress notes/results',           'PROGRESS',   true, now(), now()),
    ('ASSESSMENT_READ',  'Assessment Read',  'Read assessment records and results',    'ASSESSMENT', true, now(), now()),
    ('ASSESSMENT_WRITE', 'Assessment Write', 'Create/update assessment records/results','ASSESSMENT', true, now(), now()),

    -- Billing
    ('BILLING_READ',     'Billing Read',     'Read invoices/payments',                 'BILLING',    true, now(), now()),
    ('INVOICE_WRITE',    'Invoice Write',    'Create/update invoices',                 'BILLING',    true, now(), now()),
    ('PAYMENT_RECORD',   'Payment Record',   'Record incoming payments',               'BILLING',    true, now(), now()),
    ('REFUND_APPROVE',   'Refund Approve',   'Approve/process refunds',                'BILLING',    true, now(), now())
on conflict (code) do nothing;

-- Permission dari seed lain, upsert-safe.
insert into fastworks_springboot.permissions (code, name, description, module, is_active, created_at, updated_at)
values
    ('USER_READ',        'User Read',        'Read users',                       'USER',         true, now(), now()),
    ('USER_WRITE',       'User Write',       'Create or update users',           'USER',         true, now(), now()),
    ('USER_DELETE',      'User Delete',      'Delete users',                     'USER',         true, now(), now()),
    ('ROLE_READ',        'Role Read',        'Read roles',                       'ROLE',         true, now(), now()),
    ('ROLE_WRITE',       'Role Write',       'Create or update roles',           'ROLE',         true, now(), now()),
    ('ROLE_DELETE',      'Role Delete',      'Delete roles',                     'ROLE',         true, now(), now()),
    ('PERMISSION_READ',  'Permission Read',  'Read permissions',                 'PERMISSION',   true, now(), now()),
    ('PERMISSION_WRITE', 'Permission Write', 'Create or update permissions',     'PERMISSION',   true, now(), now()),
    ('PERMISSION_DELETE','Permission Delete','Delete permissions',               'PERMISSION',   true, now(), now()),
    ('NOTIFICATION_READ','Notification Read','Read notification data',           'NOTIFICATION', true, now(), now()),
    ('NOTIFICATION_WRITE','Notification Write','Manage notification delivery data','NOTIFICATION', true, now(), now()),
    ('REPORT_EXPORT',    'Report Export',    'Export reports',                   'REPORT',       true, now(), now()),
    ('AUDIT_READ',       'Audit Read',       'Read audit logs',                  'AUDIT',        true, now(), now())
on conflict (code) do nothing;

-- 3) Map permissions to roles
-- ADMIN sengaja diberikan SELURUH permission yang ada di tabel agar otomatis dapat menu baru
-- tanpa hardcode ROLE_ADMIN di frontend.
insert into fastworks_springboot.role_permissions (role_id, permission_id, assigned_at)
select r.id, p.id, now()
from fastworks_springboot.roles r
cross join fastworks_springboot.permissions p
where r.code = 'ADMIN'
on conflict (role_id, permission_id) do nothing;

-- Role lainnya tetap granular.
with role_perm(code_role, code_perm) as (
    values
        -- INSTRUCTOR
        ('INSTRUCTOR','CLASS_READ'),('INSTRUCTOR','SCHEDULE_READ'),('INSTRUCTOR','SESSION_READ'),
        ('INSTRUCTOR','ATTENDANCE_READ'),('INSTRUCTOR','ATTENDANCE_MARK'),
        ('INSTRUCTOR','PROGRESS_READ'),('INSTRUCTOR','PROGRESS_WRITE'),
        ('INSTRUCTOR','ASSESSMENT_READ'),('INSTRUCTOR','ASSESSMENT_WRITE'),
        ('INSTRUCTOR','STUDENT_READ'),

        -- GUARDIAN
        ('GUARDIAN','CLASS_READ'),('GUARDIAN','ENROLLMENT_READ'),
        ('GUARDIAN','ATTENDANCE_READ'),('GUARDIAN','PROGRESS_READ'),
        ('GUARDIAN','BILLING_READ'),

        -- OPS
        ('OPS','PROGRAM_READ'),('OPS','COURSE_READ'),
        ('OPS','CLASS_READ'),('OPS','CLASS_WRITE'),
        ('OPS','SCHEDULE_READ'),('OPS','SCHEDULE_WRITE'),('OPS','SCHEDULE_RESCHEDULE'),
        ('OPS','SESSION_READ'),('OPS','SESSION_WRITE'),('OPS','SESSION_RESCHEDULE'),
        ('OPS','STUDENT_READ'),('OPS','STUDENT_WRITE'),
        ('OPS','GUARDIAN_READ'),('OPS','GUARDIAN_WRITE'),
        ('OPS','ENROLLMENT_READ'),('OPS','ENROLLMENT_WRITE'),('OPS','WAITLIST_MANAGE'),
        ('OPS','ATTENDANCE_READ'),('OPS','REPORT_EXPORT'),
        ('OPS','ASSESSMENT_READ'),

        -- FINANCE
        ('FINANCE','BILLING_READ'),('FINANCE','INVOICE_WRITE'),
        ('FINANCE','PAYMENT_RECORD'),('FINANCE','REFUND_APPROVE'),
        ('FINANCE','ENROLLMENT_READ'),('FINANCE','REPORT_EXPORT')
)
insert into fastworks_springboot.role_permissions (role_id, permission_id, assigned_at)
select r.id, p.id, now()
from role_perm rp
join fastworks_springboot.roles r on r.code = rp.code_role
join fastworks_springboot.permissions p on p.code = rp.code_perm
on conflict (role_id, permission_id) do nothing;

-- 4) Seed menus utama
insert into fastworks_springboot.menus (code, name, path, icon, order_no, is_active, is_visible, created_at, updated_at)
values
    ('DASHBOARD',    'Dashboard',   '/dashboard',   'layout-dashboard', 10, true, true, now(), now()),
    ('CATALOG',      'Catalog',     '/catalog',     'book-open',        20, true, true, now(), now()),
    ('SCHEDULING',   'Scheduling',  '/scheduling',  'calendar-days',    30, true, true, now(), now()),
    ('ENROLLMENT',   'Enrollment',  '/enrollment',  'clipboard-check',  40, true, true, now(), now()),
    ('ATTENDANCE',   'Attendance',  '/attendance',  'user-check',       50, true, true, now(), now()),
    ('BILLING',      'Billing',     '/billing',     'wallet',           60, true, true, now(), now()),
    ('NOTIFICATION', 'Notification','/notification','bell',             70, true, true, now(), now()),
    ('ADMIN',        'Admin',       '/admin',       'shield',           80, true, true, now(), now())
on conflict (code) do update
set name = excluded.name,
    path = excluded.path,
    icon = excluded.icon,
    order_no = excluded.order_no,
    is_active = excluded.is_active,
    is_visible = excluded.is_visible,
    updated_at = now();

-- Submenu di bawah ADMIN: user / role / permission / audit
insert into fastworks_springboot.menus (parent_id, code, name, path, icon, order_no, is_active, is_visible, created_at, updated_at)
select m_admin.id, v.code, v.name, v.path, v.icon, v.order_no, true, true, now(), now()
from fastworks_springboot.menus m_admin
cross join (
    values
        ('ADMIN_USER',       'User Management',       '/admin/users',       'users',      10),
        ('ADMIN_ROLE',       'Role Management',       '/admin/roles',       'user-cog',   20),
        ('ADMIN_PERMISSION', 'Permission Management', '/admin/permissions', 'key-round',  30),
        ('ADMIN_AUDIT',      'Audit Log',             '/admin/audit',       'history',    40)
) as v(code, name, path, icon, order_no)
where m_admin.code = 'ADMIN'
on conflict (code) do update
set parent_id = excluded.parent_id,
    name = excluded.name,
    path = excluded.path,
    icon = excluded.icon,
    order_no = excluded.order_no,
    is_active = excluded.is_active,
    is_visible = excluded.is_visible,
    updated_at = now();

-- 5) Map menu -> permission (match_mode = ANY)
with menu_perm(menu_code, perm_code) as (
    values
        ('DASHBOARD',      'REPORT_EXPORT'),
        ('CATALOG',        'CLASS_READ'),
        ('SCHEDULING',     'SCHEDULE_READ'),
        ('ENROLLMENT',     'ENROLLMENT_READ'),
        ('ATTENDANCE',     'ATTENDANCE_READ'),
        ('BILLING',        'BILLING_READ'),
        ('NOTIFICATION',   'NOTIFICATION_READ'),
        ('ADMIN',          'USER_READ'),
        ('ADMIN_USER',     'USER_READ'),
        ('ADMIN_ROLE',     'ROLE_READ'),
        ('ADMIN_PERMISSION','PERMISSION_READ'),
        ('ADMIN_AUDIT',    'AUDIT_READ')
)
insert into fastworks_springboot.menu_permissions (menu_id, permission_id, match_mode, created_at)
select m.id, p.id, 'ANY', now()
from menu_perm mp
join fastworks_springboot.menus m on m.code = mp.menu_code
join fastworks_springboot.permissions p on p.code = mp.perm_code
on conflict (menu_id, permission_id) do nothing;

commit;
