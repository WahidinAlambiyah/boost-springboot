package role

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

type memoryRoleRepo struct {
	items map[uuid.UUID]Role
}

func newMemoryRoleRepo() *memoryRoleRepo {
	return &memoryRoleRepo{items: make(map[uuid.UUID]Role)}
}

func (m *memoryRoleRepo) Create(ctx context.Context, role *Role) error {
	m.items[role.ID] = *role
	return nil
}

func (m *memoryRoleRepo) GetByID(ctx context.Context, id uuid.UUID) (Role, error) {
	role, ok := m.items[id]
	if !ok {
		return Role{}, ErrRoleNotFound
	}
	return role, nil
}

func (m *memoryRoleRepo) GetByName(ctx context.Context, name string) (Role, error) {
	for _, role := range m.items {
		if role.Name == name {
			return role, nil
		}
	}
	return Role{}, ErrRoleNotFound
}

func (m *memoryRoleRepo) List(ctx context.Context) ([]Role, error) {
	roles := make([]Role, 0, len(m.items))
	for _, role := range m.items {
		roles = append(roles, role)
	}
	return roles, nil
}

func (m *memoryRoleRepo) Update(ctx context.Context, role *Role) error {
	m.items[role.ID] = *role
	return nil
}

func (m *memoryRoleRepo) Delete(ctx context.Context, id uuid.UUID) error {
	delete(m.items, id)
	return nil
}

func TestRoleHandlersCRUD(t *testing.T) {
	gin.SetMode(gin.TestMode)
	repo := newMemoryRoleRepo()
	svc := NewService(repo)
	h := NewHandler(svc)

	router := gin.New()
	router.POST("/roles", h.Create)
	router.GET("/roles", h.List)
	router.GET("/roles/:id", h.Get)
	router.PUT("/roles/:id", h.Update)
	router.DELETE("/roles/:id", h.Delete)

	payload := CreateRequest{Name: "ADMIN", Description: "Administrator"}
	body, err := json.Marshal(payload)
	require.NoError(t, err)

	req := httptest.NewRequest(http.MethodPost, "/roles", bytes.NewBuffer(body))
	req.Header.Set("Content-Type", "application/json")
	res := httptest.NewRecorder()
	router.ServeHTTP(res, req)
	require.Equal(t, http.StatusCreated, res.Code)

	var createResp map[string]interface{}
	require.NoError(t, json.NewDecoder(res.Body).Decode(&createResp))
	data := createResp["data"].(map[string]interface{})
	id := data["id"].(string)
	require.NotEmpty(t, id)

	listReq := httptest.NewRequest(http.MethodGet, "/roles", nil)
	listRes := httptest.NewRecorder()
	router.ServeHTTP(listRes, listReq)
	require.Equal(t, http.StatusOK, listRes.Code)

	getReq := httptest.NewRequest(http.MethodGet, "/roles/"+id, nil)
	getRes := httptest.NewRecorder()
	router.ServeHTTP(getRes, getReq)
	require.Equal(t, http.StatusOK, getRes.Code)

	updatePayload := UpdateRequest{Description: "Updated"}
	updateBody, err := json.Marshal(updatePayload)
	require.NoError(t, err)

	updateReq := httptest.NewRequest(http.MethodPut, "/roles/"+id, bytes.NewBuffer(updateBody))
	updateReq.Header.Set("Content-Type", "application/json")
	updateRes := httptest.NewRecorder()
	router.ServeHTTP(updateRes, updateReq)
	require.Equal(t, http.StatusOK, updateRes.Code)

	deleteReq := httptest.NewRequest(http.MethodDelete, "/roles/"+id, nil)
	deleteRes := httptest.NewRecorder()
	router.ServeHTTP(deleteRes, deleteReq)
	require.Equal(t, http.StatusOK, deleteRes.Code)
}
