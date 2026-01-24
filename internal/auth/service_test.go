package auth

import (
	"context"
	"testing"
	"time"

	"github.com/google/uuid"
	"github.com/stretchr/testify/require"
	"github.com/yourusername/go-users-api/internal/config"
	rolepkg "github.com/yourusername/go-users-api/internal/role"
	"github.com/yourusername/go-users-api/internal/user"
	"github.com/yourusername/go-users-api/internal/utils"
)

type mockRepo struct {
	user user.User
	err  error
}

func (m mockRepo) Create(ctx context.Context, u *user.User) error { return m.err }
func (m mockRepo) GetByID(ctx context.Context, id uuid.UUID) (user.User, error) {
	return m.user, m.err
}
func (m mockRepo) GetByEmailOrUsername(ctx context.Context, identifier string) (user.User, error) {
	return m.user, m.err
}
func (m mockRepo) List(ctx context.Context) ([]user.User, error)  { return nil, m.err }
func (m mockRepo) Update(ctx context.Context, u *user.User) error { return m.err }
func (m mockRepo) Delete(ctx context.Context, id uuid.UUID) error { return m.err }

type mockRoleRepo struct {
	role rolepkg.Role
	err  error
}

func (m mockRoleRepo) GetByName(ctx context.Context, name string) (rolepkg.Role, error) {
	if m.err != nil {
		return rolepkg.Role{}, m.err
	}
	return m.role, nil
}

type memoryTokenStore struct {
	items map[string]string
}

func newMemoryTokenStore() *memoryTokenStore {
	return &memoryTokenStore{items: map[string]string{}}
}

func (m *memoryTokenStore) Set(ctx context.Context, key string, value string, ttl time.Duration) error {
	m.items[key] = value
	return nil
}

func (m *memoryTokenStore) Exists(ctx context.Context, key string) (int64, error) {
	if _, ok := m.items[key]; ok {
		return 1, nil
	}
	return 0, nil
}

func (m *memoryTokenStore) Del(ctx context.Context, key string) error {
	delete(m.items, key)
	return nil
}

func TestAuthService_TokenFlow(t *testing.T) {
	hashed, err := utils.HashPassword("secret123")
	require.NoError(t, err)

	sampleUser := user.User{
		ID:           uuid.New(),
		FullName:     "Jane Doe",
		Email:        "jane@example.com",
		Username:     "jane",
		PasswordHash: hashed,
		RoleID:       uuid.New(),
		Role:         rolepkg.Role{ID: uuid.New(), Name: string(user.RoleUser)},
		IsActive:     true,
	}

	cfg := config.JWTConfig{
		Secret:            "supersecret",
		AccessTokenTTL:    15 * time.Minute,
		RefreshTokenTTL:   7 * 24 * time.Hour,
		AccessTokenLabel:  "access",
		RefreshTokenLabel: "refresh",
	}

	store := newMemoryTokenStore()
	roleRepo := mockRoleRepo{role: rolepkg.Role{ID: uuid.New(), Name: string(user.RoleUser)}}
	svc := NewService(mockRepo{user: sampleUser}, roleRepo, store, cfg)

	loginResp, err := svc.Login(context.Background(), LoginRequest{Identifier: "jane@example.com", Password: "secret123"})
	require.NoError(t, err)
	require.NotEmpty(t, loginResp.AccessToken)
	require.NotEmpty(t, loginResp.RefreshToken)

	claims, err := utils.ParseToken(loginResp.AccessToken, cfg.Secret)
	require.NoError(t, err)
	require.Equal(t, sampleUser.ID.String(), claims.Subject)
	require.Equal(t, cfg.AccessTokenLabel, claims.TokenType)

	refreshResp, err := svc.Refresh(context.Background(), loginResp.RefreshToken)
	require.NoError(t, err)
	require.NotEmpty(t, refreshResp.AccessToken)
}

func TestPasswordHashVerify(t *testing.T) {
	hash, err := utils.HashPassword("password123")
	require.NoError(t, err)

	err = utils.VerifyPassword(hash, "password123")
	require.NoError(t, err)
}
