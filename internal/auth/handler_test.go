package auth

import (
	"bytes"
	"context"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"github.com/stretchr/testify/require"
	"github.com/yourusername/go-users-api/internal/config"
	rolepkg "github.com/yourusername/go-users-api/internal/role"
	"github.com/yourusername/go-users-api/internal/user"
	"github.com/yourusername/go-users-api/internal/utils"
)

type memoryAuthRepo struct {
	items map[string]user.User
}

func newMemoryAuthRepo() *memoryAuthRepo {
	return &memoryAuthRepo{items: make(map[string]user.User)}
}

func (m *memoryAuthRepo) Create(ctx context.Context, u *user.User) error {
	m.items[u.Email] = *u
	m.items[u.Username] = *u
	return nil
}

func (m *memoryAuthRepo) GetByID(ctx context.Context, id uuid.UUID) (user.User, error) {
	for _, entity := range m.items {
		if entity.ID == id {
			return entity, nil
		}
	}
	return user.User{}, user.ErrUserNotFound
}

func (m *memoryAuthRepo) GetByEmailOrUsername(ctx context.Context, identifier string) (user.User, error) {
	entity, ok := m.items[identifier]
	if !ok {
		return user.User{}, user.ErrUserNotFound
	}
	return entity, nil
}

func (m *memoryAuthRepo) List(ctx context.Context) ([]user.User, error) {
	return nil, nil
}

func (m *memoryAuthRepo) Update(ctx context.Context, u *user.User) error { return nil }
func (m *memoryAuthRepo) Delete(ctx context.Context, id uuid.UUID) error { return nil }

type memoryRoleRepo struct {
	roles map[string]rolepkg.Role
}

func newMemoryRoleRepo() *memoryRoleRepo {
	return &memoryRoleRepo{roles: map[string]rolepkg.Role{
		string(user.RoleUser):  {ID: uuid.New(), Name: string(user.RoleUser)},
		string(user.RoleAdmin): {ID: uuid.New(), Name: string(user.RoleAdmin)},
	}}
}

func (m *memoryRoleRepo) GetByName(ctx context.Context, name string) (rolepkg.Role, error) {
	roleEntity, ok := m.roles[name]
	if !ok {
		return rolepkg.Role{}, user.ErrRoleNotFound
	}
	return roleEntity, nil
}

func TestAuthHandlersFlow(t *testing.T) {
	gin.SetMode(gin.TestMode)
	repo := newMemoryAuthRepo()
	roleRepo := newMemoryRoleRepo()
	store := NewMemoryTokenStore()
	jwtCfg := config.JWTConfig{
		Secret:            "secret",
		AccessTokenTTL:    15 * time.Minute,
		RefreshTokenTTL:   7 * 24 * time.Hour,
		AccessTokenLabel:  "access",
		RefreshTokenLabel: "refresh",
	}
	svc := NewService(repo, roleRepo, store, jwtCfg)
	h := NewHandler(svc)

	router := gin.New()
	router.POST("/auth/register", h.Register)
	router.POST("/auth/login", h.Login)
	router.POST("/auth/refresh", h.Refresh)
	router.POST("/auth/logout", h.Logout)

	registerPayload := RegisterRequest{
		FullName: "Jane Doe",
		Email:    "jane@example.com",
		Username: "jane",
		Password: "secret123",
	}
	registerBody, err := json.Marshal(registerPayload)
	require.NoError(t, err)

	registerReq := httptest.NewRequest(http.MethodPost, "/auth/register", bytes.NewBuffer(registerBody))
	registerReq.Header.Set("Content-Type", "application/json")
	registerRes := httptest.NewRecorder()
	router.ServeHTTP(registerRes, registerReq)
	require.Equal(t, http.StatusCreated, registerRes.Code)

	loginPayload := LoginRequest{Identifier: "jane@example.com", Password: "secret123"}
	loginBody, err := json.Marshal(loginPayload)
	require.NoError(t, err)

	loginReq := httptest.NewRequest(http.MethodPost, "/auth/login", bytes.NewBuffer(loginBody))
	loginReq.Header.Set("Content-Type", "application/json")
	loginRes := httptest.NewRecorder()
	router.ServeHTTP(loginRes, loginReq)
	require.Equal(t, http.StatusOK, loginRes.Code)

	var loginResp map[string]interface{}
	require.NoError(t, json.NewDecoder(loginRes.Body).Decode(&loginResp))
	loginData := loginResp["data"].(map[string]interface{})
	refreshToken := loginData["refresh_token"].(string)
	require.NotEmpty(t, refreshToken)

	refreshPayload := RefreshRequest{RefreshToken: refreshToken}
	refreshBody, err := json.Marshal(refreshPayload)
	require.NoError(t, err)

	refreshReq := httptest.NewRequest(http.MethodPost, "/auth/refresh", bytes.NewBuffer(refreshBody))
	refreshReq.Header.Set("Content-Type", "application/json")
	refreshRes := httptest.NewRecorder()
	router.ServeHTTP(refreshRes, refreshReq)
	require.Equal(t, http.StatusOK, refreshRes.Code)

	logoutPayload := LogoutRequest{RefreshToken: refreshToken}
	logoutBody, err := json.Marshal(logoutPayload)
	require.NoError(t, err)

	logoutReq := httptest.NewRequest(http.MethodPost, "/auth/logout", bytes.NewBuffer(logoutBody))
	logoutReq.Header.Set("Content-Type", "application/json")
	logoutRes := httptest.NewRecorder()
	router.ServeHTTP(logoutRes, logoutReq)
	require.Equal(t, http.StatusOK, logoutRes.Code)
}

func TestAuthHandlersLoginFailure(t *testing.T) {
	gin.SetMode(gin.TestMode)
	repo := newMemoryAuthRepo()
	roleRepo := newMemoryRoleRepo()
	store := NewMemoryTokenStore()
	jwtCfg := config.JWTConfig{
		Secret:            "secret",
		AccessTokenTTL:    15 * time.Minute,
		RefreshTokenTTL:   7 * 24 * time.Hour,
		AccessTokenLabel:  "access",
		RefreshTokenLabel: "refresh",
	}
	svc := NewService(repo, roleRepo, store, jwtCfg)
	h := NewHandler(svc)

	router := gin.New()
	router.POST("/auth/login", h.Login)

	hash, err := utils.HashPassword("secret123")
	require.NoError(t, err)

	userEntity := user.User{
		ID:           uuid.New(),
		FullName:     "Jane Doe",
		Email:        "jane@example.com",
		Username:     "jane",
		PasswordHash: hash,
		RoleID:       roleRepo.roles[string(user.RoleUser)].ID,
		Role:         roleRepo.roles[string(user.RoleUser)],
		IsActive:     true,
	}
	require.NoError(t, repo.Create(context.Background(), &userEntity))

	loginPayload := LoginRequest{Identifier: "jane@example.com", Password: "wrong"}
	loginBody, err := json.Marshal(loginPayload)
	require.NoError(t, err)

	loginReq := httptest.NewRequest(http.MethodPost, "/auth/login", bytes.NewBuffer(loginBody))
	loginReq.Header.Set("Content-Type", "application/json")
	loginRes := httptest.NewRecorder()
	router.ServeHTTP(loginRes, loginReq)
	require.Equal(t, http.StatusUnauthorized, loginRes.Code)
}
