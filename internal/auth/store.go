package auth

import (
	"context"
	"time"

	"github.com/redis/go-redis/v9"
)

type TokenStore interface {
	Set(ctx context.Context, key string, value string, ttl time.Duration) error
	Exists(ctx context.Context, key string) (int64, error)
	Del(ctx context.Context, key string) error
}

type RedisTokenStore struct {
	client *redis.Client
}

func NewRedisTokenStore(client *redis.Client) *RedisTokenStore {
	return &RedisTokenStore{client: client}
}

func (r *RedisTokenStore) Set(ctx context.Context, key string, value string, ttl time.Duration) error {
	return r.client.Set(ctx, key, value, ttl).Err()
}

func (r *RedisTokenStore) Exists(ctx context.Context, key string) (int64, error) {
	return r.client.Exists(ctx, key).Result()
}

func (r *RedisTokenStore) Del(ctx context.Context, key string) error {
	return r.client.Del(ctx, key).Err()
}
