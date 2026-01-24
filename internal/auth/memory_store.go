package auth

import (
	"context"
	"sync"
	"time"
)

type memoryEntry struct {
	value     string
	expiresAt time.Time
}

type MemoryTokenStore struct {
	mu    sync.RWMutex
	items map[string]memoryEntry
}

func NewMemoryTokenStore() *MemoryTokenStore {
	return &MemoryTokenStore{items: make(map[string]memoryEntry)}
}

func (m *MemoryTokenStore) Set(ctx context.Context, key string, value string, ttl time.Duration) error {
	expiresAt := time.Now().Add(ttl)
	m.mu.Lock()
	m.items[key] = memoryEntry{value: value, expiresAt: expiresAt}
	m.mu.Unlock()
	return nil
}

func (m *MemoryTokenStore) Exists(ctx context.Context, key string) (int64, error) {
	m.mu.RLock()
	entry, ok := m.items[key]
	m.mu.RUnlock()
	if !ok {
		return 0, nil
	}
	if time.Now().After(entry.expiresAt) {
		m.mu.Lock()
		delete(m.items, key)
		m.mu.Unlock()
		return 0, nil
	}
	return 1, nil
}

func (m *MemoryTokenStore) Del(ctx context.Context, key string) error {
	m.mu.Lock()
	delete(m.items, key)
	m.mu.Unlock()
	return nil
}
