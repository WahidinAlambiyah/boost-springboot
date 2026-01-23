package user

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
// @Summary List users
// @Tags Users
// @Produce json
// @Success 200 {object} response.Response
// @Failure 500 {object} response.Response
// @Router /api/v1/users [get]
func (h *Handler) List(c *gin.Context) {
	users, err := h.svc.List(c.Request.Context())
	if err != nil {
		c.JSON(http.StatusInternalServerError, response.Failure("internal_error", "failed to fetch users", err.Error()))
		return
	}

	responses := make([]Response, 0, len(users))
	for _, entity := range users {
		responses = append(responses, ToResponse(entity))
	}

	c.JSON(http.StatusOK, response.Success(responses, nil))
}

// Create godoc
// @Summary Create user
// @Tags Users
// @Accept json
// @Produce json
// @Param payload body CreateRequest true "Create user payload"
// @Success 201 {object} response.Response
// @Failure 400 {object} response.Response
// @Failure 500 {object} response.Response
// @Router /api/v1/users [post]
func (h *Handler) Create(c *gin.Context) {
	var req CreateRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, response.Failure("validation_error", "invalid payload", err.Error()))
		return
	}

	role := RoleUser
	if req.Role != "" {
		role = Role(req.Role)
	}

	created, err := h.svc.Create(c.Request.Context(), req, role)
	if err != nil {
		c.JSON(http.StatusInternalServerError, response.Failure("internal_error", "failed to create user", err.Error()))
		return
	}

	c.JSON(http.StatusCreated, response.Success(ToResponse(created), nil))
}

// Get godoc
// @Summary Get user by ID
// @Tags Users
// @Produce json
// @Param id path string true "User ID"
// @Success 200 {object} response.Response
// @Failure 400 {object} response.Response
// @Failure 404 {object} response.Response
// @Failure 500 {object} response.Response
// @Router /api/v1/users/{id} [get]
func (h *Handler) Get(c *gin.Context) {
	id, err := uuid.Parse(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, response.Failure("validation_error", "invalid user id", nil))
		return
	}

	userEntity, err := h.svc.GetByID(c.Request.Context(), id)
	if err != nil {
		c.JSON(http.StatusNotFound, response.Failure("not_found", "user not found", nil))
		return
	}

	c.JSON(http.StatusOK, response.Success(ToResponse(userEntity), nil))
}

// Update godoc
// @Summary Update user
// @Tags Users
// @Accept json
// @Produce json
// @Param id path string true "User ID"
// @Param payload body UpdateRequest true "Update payload"
// @Success 200 {object} response.Response
// @Failure 400 {object} response.Response
// @Failure 404 {object} response.Response
// @Failure 500 {object} response.Response
// @Router /api/v1/users/{id} [put]
func (h *Handler) Update(c *gin.Context) {
	id, err := uuid.Parse(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, response.Failure("validation_error", "invalid user id", nil))
		return
	}

	var req UpdateRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, response.Failure("validation_error", "invalid payload", err.Error()))
		return
	}

	current, err := h.svc.GetByID(c.Request.Context(), id)
	if err != nil {
		c.JSON(http.StatusNotFound, response.Failure("not_found", "user not found", nil))
		return
	}

	updated, err := h.svc.Update(c.Request.Context(), current, req, true)
	if err != nil {
		c.JSON(http.StatusInternalServerError, response.Failure("internal_error", "failed to update user", err.Error()))
		return
	}

	c.JSON(http.StatusOK, response.Success(ToResponse(updated), nil))
}

// Delete godoc
// @Summary Delete user
// @Tags Users
// @Produce json
// @Param id path string true "User ID"
// @Success 200 {object} response.Response
// @Failure 400 {object} response.Response
// @Failure 404 {object} response.Response
// @Failure 500 {object} response.Response
// @Router /api/v1/users/{id} [delete]
func (h *Handler) Delete(c *gin.Context) {
	id, err := uuid.Parse(c.Param("id"))
	if err != nil {
		c.JSON(http.StatusBadRequest, response.Failure("validation_error", "invalid user id", nil))
		return
	}

	if err := h.svc.Delete(c.Request.Context(), id); err != nil {
		c.JSON(http.StatusInternalServerError, response.Failure("internal_error", "failed to delete user", err.Error()))
		return
	}

	c.JSON(http.StatusOK, response.Success(gin.H{"message": "user deleted"}, nil))
}
