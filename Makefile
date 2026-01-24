APP_NAME=go-users-api

run:
	go run ./cmd/api

test:
	go test ./...

swag:
	swag init -g cmd/api/main.go

# ===== Load .env (kalau ada) =====
ifneq (,$(wildcard .env))
include .env
export APP_PORT DB_HOST DB_PORT DB_NAME DB_USER DB_PASSWORD DB_SSLMODE
endif

MIGRATE ?= migrate

DB_SCHEMA ?= fastworks_golang
MIGRATIONS_TABLE ?= fastworks_golang_migrations

DB_URL := postgres://$(DB_USER):$(DB_PASSWORD)@$(DB_HOST):$(DB_PORT)/$(DB_NAME)?sslmode=$(DB_SSLMODE)&search_path=$(DB_SCHEMA)&x-migrations-table=$(MIGRATIONS_TABLE)

.PHONY: migrate-up migrate-down migrate-version

migrate-version:
	$(MIGRATE) -version

migrate-up:
	$(MIGRATE) -path ./migrations -database "$(DB_URL)" up

migrate-down:
	$(MIGRATE) -path ./migrations -database "$(DB_URL)" down 1

# migrate-up:
# 	migrate -path ./migrations -database "postgres://$(DB_USER):$(DB_PASSWORD)@$(DB_HOST):$(DB_PORT)/$(DB_NAME)?sslmode=$(DB_SSLMODE)" up

# migrate-down:
# 	migrate -path ./migrations -database "postgres://$(DB_USER):$(DB_PASSWORD)@$(DB_HOST):$(DB_PORT)/$(DB_NAME)?sslmode=$(DB_SSLMODE)" down
