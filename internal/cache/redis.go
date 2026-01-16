package cache

import (
	"context"
	"fmt"

	"github.com/redis/go-redis/v9"
	"github.com/yourusername/go-users-api/internal/config"
)

func NewRedis(cfg config.RedisConfig) *redis.Client {
	addr := fmt.Sprintf("%s:%s", cfg.Host, cfg.Port)
	return redis.NewClient(&redis.Options{
		Addr:     addr,
		Password: cfg.Password,
		DB:       cfg.DB,
	})
}

func Ping(ctx context.Context, client *redis.Client) error {
	return client.Ping(ctx).Err()
}
