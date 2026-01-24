CREATE SCHEMA IF NOT EXISTS fastworks_golang;
SET search_path TO fastworks_golang;

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

INSERT INTO roles (id, name, description, created_at, updated_at)
VALUES (gen_random_uuid(), 'ADMIN', 'Administrator', NOW(), NOW()),
       (gen_random_uuid(), 'USER', 'Default user', NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

ALTER TABLE IF EXISTS users
    ADD COLUMN IF NOT EXISTS role_id UUID;

UPDATE users
SET role_id = roles.id
FROM roles
WHERE users.role = roles.name
  AND users.role_id IS NULL;

ALTER TABLE IF EXISTS users
    ALTER COLUMN role_id SET NOT NULL;

ALTER TABLE IF EXISTS users
    ADD CONSTRAINT IF NOT EXISTS fk_users_role_id FOREIGN KEY (role_id) REFERENCES roles (id);

CREATE INDEX IF NOT EXISTS idx_users_role_id ON users (role_id);

CREATE TABLE IF NOT EXISTS categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_categories_name ON categories (name);
CREATE UNIQUE INDEX IF NOT EXISTS idx_categories_slug ON categories (slug);

CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    category_id UUID NOT NULL,
    name VARCHAR(255) NOT NULL,
    sku VARCHAR(100) NOT NULL,
    description TEXT,
    price NUMERIC(12,2) NOT NULL,
    stock INTEGER NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_products_category_id FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT fk_products_created_by FOREIGN KEY (created_by) REFERENCES users (id) ON DELETE SET NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_products_sku ON products (sku);
CREATE INDEX IF NOT EXISTS idx_products_category_id ON products (category_id);
