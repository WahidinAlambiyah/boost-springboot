# syntax=docker/dockerfile:1
FROM golang:1.25.5-alpine AS builder

WORKDIR /app
RUN apk add --no-cache git

COPY go.mod go.sum ./
RUN go mod download

COPY . .
RUN CGO_ENABLED=0 GOOS=linux go build -o /bin/go-users-api ./cmd/api

FROM alpine:3.20
RUN adduser -D -g '' appuser

WORKDIR /app
COPY --from=builder /bin/go-users-api /app/go-users-api
COPY migrations /app/migrations

USER appuser

EXPOSE 8080
ENTRYPOINT ["/app/go-users-api"]
