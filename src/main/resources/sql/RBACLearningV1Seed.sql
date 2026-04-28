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

    -- Billing
    ('BILLING_READ',     'Billing Read',     'Read invoices/payments',                 'BILLING',    true, now(), now()),
    ('INVOICE_WRITE',    'Invoice Write',    'Create/update invoices',                 'BILLING',    true, now(), now()),
    ('PAYMENT_RECORD',   'Payment Record',   'Record incoming payments',               'BILLING',    true, now(), now()),
    ('REFUND_APPROVE',   'Refund Approve',   'Approve/process refunds',                'BILLING',    true, now(), now())

    on conflict (code) do nothing;

-- REPORT_EXPORT & AUDIT_READ mungkin sudah ada di seed lama.
-- Kalau belum, ini akan insert; kalau sudah ada, aman karena on conflict.
insert into fastworks_springboot.permissions (code, name, description, module, is_active, created_at, updated_at)
values
    ('REPORT_EXPORT', 'Report Export', 'Export reports', 'REPORT', true, now(), now()),
    ('AUDIT_READ',    'Audit Read',    'Read audit logs', 'AUDIT', true, now(), now())
    on conflict (code) do nothing;

-- 3) Map permissions to roles
-- Helper CTE agar tidak ketik insert manual satu-satu
with role_perm(code_role, code_perm) as (
    values
        -- ADMIN -> all permissions
        ('ADMIN','PROGRAM_READ'),('ADMIN','PROGRAM_WRITE'),('ADMIN','COURSE_READ'),('ADMIN','COURSE_WRITE'),
        ('ADMIN','CLASS_READ'),('ADMIN','CLASS_WRITE'),('ADMIN','SCHEDULE_READ'),('ADMIN','SCHEDULE_WRITE'),
        ('ADMIN','SCHEDULE_RESCHEDULE'),('ADMIN','SESSION_READ'),('ADMIN','SESSION_WRITE'),
        ('ADMIN','SESSION_RESCHEDULE'),('ADMIN','STUDENT_READ'),('ADMIN','STUDENT_WRITE'),
        ('ADMIN','GUARDIAN_READ'),('ADMIN','GUARDIAN_WRITE'),('ADMIN','ENROLLMENT_READ'),
        ('ADMIN','ENROLLMENT_WRITE'),('ADMIN','WAITLIST_MANAGE'),('ADMIN','ATTENDANCE_READ'),
        ('ADMIN','ATTENDANCE_MARK'),('ADMIN','PROGRESS_READ'),('ADMIN','PROGRESS_WRITE'),
        ('ADMIN','BILLING_READ'),('ADMIN','INVOICE_WRITE'),('ADMIN','PAYMENT_RECORD'),
        ('ADMIN','REFUND_APPROVE'),('ADMIN','REPORT_EXPORT'),('ADMIN','AUDIT_READ'),

        -- INSTRUCTOR
        ('INSTRUCTOR','CLASS_READ'),('INSTRUCTOR','SCHEDULE_READ'),('INSTRUCTOR','SESSION_READ'),
        ('INSTRUCTOR','ATTENDANCE_READ'),('INSTRUCTOR','ATTENDANCE_MARK'),
        ('INSTRUCTOR','PROGRESS_READ'),('INSTRUCTOR','PROGRESS_WRITE'),
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

commit;