package role

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"github.com/yourusername/go-users-api/internal/response"
)

type Handler struct {
	svc *Service
}

func NewHandler(svc *Service) *Handler {
	return &Handler{svc: svc}
}

// List godoc
// @Summary List roles
// @Tags Roles
// @Produce json
// @Success 200 {object} response.Response
// @Failure 500 {object} response.Response
// @Router /api/v1/roles [get]
func (h *Handler) List(c *gin.Context) {
	roles, err := h.svc.List(c.Request.Context())
	if err != nil {
		c.JSON(http.StatusInternalServerError, response.Failure("internal_error", "failed to fetch roles", err.Error()))
		return
	}

	responses := make([]Response, 0, len(roles))
	for _, entity := range roles {
		responses = append(responses, ToResponse(entity))
	}

	c.JSON(http.StatusOK, response.Success(responses, nil))
}

// Create godoc
// @Summary Create role
// @Tags Roles
// @Accept json
// @Produce json
// @Param payload body CreateRequest true "Create role payload"
// @Success 201 {object} response.Response
// @Failure 400 {object} response.Response
// @Failure 500 {object} response.Response
// @Router /api/v1/roles [post]
func (h *Handler) Create(c *gin.Context) {
	var req CreateRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, response.Failure("validation_error", "invalid payload", err.Error()))
		return
	}

	created, err := h.svc.Create(c.Request.Context(), req)
	if err != nil {
		c.JSON(http.StatusInternalServerError, response.Failure("internal_error", "failed to create role", err.Error()))
		return
	}

	c.JSON(http.StatusCreated, response.Success(ToResponse(created), nil))
}

// Get godoc
// @Summary Get role by ID
// @Tags Roles
// @Produce json
// @Param id path string true "Role ID"
// @Success 200 {object} response.Response
// @Failure 400 {object} response.Response
// @Failure 404 {object} response.Response
// @Failure 500 {object} response.Response
// @Router /api/v1/roles/{id} [get]
func (h *Handler) Get(c *gin.Context) {
	id, err := uuid.Parse(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, response.Failure("validation_error", "invalid role id", nil))
		return
	}

	roleEntity, err := h.svc.GetByID(c.Request.Context(), id)
	if err != nil {
		c.JSON(http.StatusNotFound, response.Failure("not_found", "role not found", nil))
		return
	}

	c.JSON(http.StatusOK, response.Success(ToResponse(roleEntity), nil))
}

// Update godoc
// @Summary Update role
// @Tags Roles
// @Accept json
// @Produce json
// @Param id path string true "Role ID"
// @Param payload body UpdateRequest true "Update payload"
// @Success 200 {object} response.Response
// @Failure 400 {object} response.Response
// @Failure 404 {object} response.Response
// @Failure 500 {object} response.Response
// @Router /api/v1/roles/{id} [put]
func (h *Handler) Update(c *gin.Context) {
	id, err := uuid.Parse(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, response.Failure("validation_error", "invalid role id", nil))
		return
	}

	var req UpdateRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, response.Failure("validation_error", "invalid payload", err.Error()))
		return
	}

	current, err := h.svc.GetByID(c.Request.Context(), id)
	if err != nil {
		c.JSON(http.StatusNotFound, response.Failure("not_found", "role not found", nil))
		return
	}

	updated, err := h.svc.Update(c.Request.Context(), current, req)
	if err != nil {
		c.JSON(http.StatusInternalServerError, response.Failure("internal_error", "failed to update role", err.Error()))
		return
	}

	c.JSON(http.StatusOK, response.Success(ToResponse(updated), nil))
}

// Delete godoc
// @Summary Delete role
// @Tags Roles
// @Produce json
// @Param id path string true "Role ID"
// @Success 200 {object} response.Response
// @Failure 400 {object} response.Response
// @Failure 404 {object} response.Response
// @Failure 500 {object} response.Response
// @Router /api/v1/roles/{id} [delete]
func (h *Handler) Delete(c *gin.Context) {
	id, err := uuid.Parse(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, response.Failure("validation_error", "invalid role id", nil))
		return
	}

	if err := h.svc.Delete(c.Request.Context(), id); err != nil {
		c.JSON(http.StatusInternalServerError, response.Failure("internal_error", "failed to delete role", err.Error()))
		return
	}

	c.JSON(http.StatusOK, response.Success(gin.H{"message": "role deleted"}, nil))
}
