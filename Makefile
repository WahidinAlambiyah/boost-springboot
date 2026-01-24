APP_NAME=go-users-api

run:
	go run ./cmd/api

test:
	go test ./...

swag:
	swag init -g cmd/api/main.go

migrate-up:
	migrate -path ./migrations -database "postgres://$(DB_USER):$(DB_PASSWORD)@$(DB_HOST):$(DB_PORT)/$(DB_NAME)?sslmode=$(DB_SSLMODE)&search_path=$(DB_SCHEMA)" up

migrate-down:
	migrate -path ./migrations -database "postgres://$(DB_USER):$(DB_PASSWORD)@$(DB_HOST):$(DB_PORT)/$(DB_NAME)?sslmode=$(DB_SSLMODE)&search_path=$(DB_SCHEMA)" down
