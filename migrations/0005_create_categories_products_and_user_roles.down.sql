CREATE SCHEMA IF NOT EXISTS fastworks_golang;
SET search_path TO fastworks_golang;

DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS categories;

ALTER TABLE IF EXISTS users
    DROP CONSTRAINT IF EXISTS fk_users_role_id;

DROP INDEX IF EXISTS idx_users_role_id;

ALTER TABLE IF EXISTS users
    DROP COLUMN IF EXISTS role_id;
