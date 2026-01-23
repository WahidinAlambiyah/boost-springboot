package user

import (
	"bytes"
	"context"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"github.com/stretchr/testify/require"
)

type memoryUserRepo struct {
	items map[uuid.UUID]User
}

func newMemoryUserRepo() *memoryUserRepo {
	return &memoryUserRepo{items: make(map[uuid.UUID]User)}
}

func (m *memoryUserRepo) Create(ctx context.Context, user *User) error {
	m.items[user.ID] = *user
	return nil
}

func (m *memoryUserRepo) GetByID(ctx context.Context, id uuid.UUID) (User, error) {
	user, ok := m.items[id]
	if !ok {
		return User{}, ErrUserNotFound
	}
	return user, nil
}

func (m *memoryUserRepo) GetByEmailOrUsername(ctx context.Context, identifier string) (User, error) {
	for _, user := range m.items {
		if user.Email == identifier || user.Username == identifier {
			return user, nil
		}
	}
	return User{}, ErrUserNotFound
}

func (m *memoryUserRepo) List(ctx context.Context) ([]User, error) {
	users := make([]User, 0, len(m.items))
	for _, user := range m.items {
		users = append(users, user)
	}
	return users, nil
}

func (m *memoryUserRepo) Update(ctx context.Context, user *User) error {
	m.items[user.ID] = *user
	return nil
}

func (m *memoryUserRepo) Delete(ctx context.Context, id uuid.UUID) error {
	delete(m.items, id)
	return nil
}

func TestUserHandlersCRUD(t *testing.T) {
	gin.SetMode(gin.TestMode)
	repo := newMemoryUserRepo()
	svc := NewService(repo)
	h := NewHandler(svc)

	router := gin.New()
	router.POST("/users", h.Create)
	router.GET("/users", h.List)
	router.GET("/users/:id", h.Get)
	router.PUT("/users/:id", h.Update)
	router.DELETE("/users/:id", h.Delete)

	payload := CreateRequest{FullName: "Jane Doe", Email: "jane@example.com", Username: "jane", Password: "secret123"}
	body, err := json.Marshal(payload)
	require.NoError(t, err)

	req := httptest.NewRequest(http.MethodPost, "/users", bytes.NewBuffer(body))
	req.Header.Set("Content-Type", "application/json")
	res := httptest.NewRecorder()
	router.ServeHTTP(res, req)
	require.Equal(t, http.StatusCreated, res.Code)

	var createResp map[string]interface{}
	require.NoError(t, json.NewDecoder(res.Body).Decode(&createResp))
	data := createResp["data"].(map[string]interface{})
	id := data["id"].(string)
	require.NotEmpty(t, id)

	listReq := httptest.NewRequest(http.MethodGet, "/users", nil)
	listRes := httptest.NewRecorder()
	router.ServeHTTP(listRes, listReq)
	require.Equal(t, http.StatusOK, listRes.Code)

	getReq := httptest.NewRequest(http.MethodGet, "/users/"+id, nil)
	getRes := httptest.NewRecorder()
	router.ServeHTTP(getRes, getReq)
	require.Equal(t, http.StatusOK, getRes.Code)

	updatePayload := UpdateRequest{FullName: "Jane Updated"}
	updateBody, err := json.Marshal(updatePayload)
	require.NoError(t, err)

	updateReq := httptest.NewRequest(http.MethodPut, "/users/"+id, bytes.NewBuffer(updateBody))
	updateReq.Header.Set("Content-Type", "application/json")
	updateRes := httptest.NewRecorder()
	router.ServeHTTP(updateRes, updateReq)
	require.Equal(t, http.StatusOK, updateRes.Code)

	deleteReq := httptest.NewRequest(http.MethodDelete, "/users/"+id, nil)
	deleteRes := httptest.NewRecorder()
	router.ServeHTTP(deleteRes, deleteReq)
	require.Equal(t, http.StatusOK, deleteRes.Code)
}
