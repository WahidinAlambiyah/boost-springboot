package category

import (
	"errors"
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"github.com/jackc/pgx/v5/pgconn"
	"github.com/yourusername/go-users-api/internal/response"
	"go.uber.org/zap"
)

type Handler struct {
	svc *Service
}

func NewHandler(svc *Service) *Handler {
	return &Handler{svc: svc}
}

// List godoc
// @Summary List categories
// @Tags Categories
// @Produce json
// @Success 200 {object} response.Response
// @Failure 500 {object} response.Response
// @Router /api/v1/categories [get]
func (h *Handler) List(c *gin.Context) {
	categories, err := h.svc.List(c.Request.Context())
	if err != nil {
		zap.L().Error("list categories failed", zap.Error(err))
		c.JSON(http.StatusInternalServerError, response.Failure("internal_error", "failed to fetch categories", err.Error()))
		return
	}

	responses := make([]Response, 0, len(categories))
	for _, entity := range categories {
		responses = append(responses, ToResponse(entity))
	}

	c.JSON(http.StatusOK, response.Success(responses, nil))
}

// Create godoc
// @Summary Create category
// @Tags Categories
// @Accept json
// @Produce json
// @Param payload body CreateRequest true "Create category payload"
// @Success 201 {object} response.Response
// @Failure 400 {object} response.Response
// @Failure 409 {object} response.Response
// @Failure 500 {object} response.Response
// @Router /api/v1/categories [post]
func (h *Handler) Create(c *gin.Context) {
	var req CreateRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		zap.L().Error("create category validation failed", zap.Error(err))
		c.JSON(http.StatusBadRequest, response.Failure("validation_error", "invalid payload", err.Error()))
		return
	}

	created, err := h.svc.Create(c.Request.Context(), req)
	if err != nil {
		if code, message, ok := uniqueConstraintError(err); ok {
			c.JSON(http.StatusConflict, response.Failure(code, message, nil))
			return
		}
		zap.L().Error("create category failed", zap.Error(err))
		c.JSON(http.StatusInternalServerError, response.Failure("internal_error", "failed to create category", err.Error()))
		return
	}

	c.JSON(http.StatusCreated, response.Success(ToResponse(created), nil))
}

// Get godoc
// @Summary Get category by ID
// @Tags Categories
// @Produce json
// @Param id path string true "Category ID"
// @Success 200 {object} response.Response
// @Failure 400 {object} response.Response
// @Failure 404 {object} response.Response
// @Failure 500 {object} response.Response
// @Router /api/v1/categories/{id} [get]
func (h *Handler) Get(c *gin.Context) {
	id, err := uuid.Parse(c.Param("id"))
	if err != nil {
		zap.L().Error("parse category id failed", zap.Error(err))
		c.JSON(http.StatusBadRequest, response.Failure("validation_error", "invalid category id", nil))
		return
	}

	categoryEntity, err := h.svc.GetByID(c.Request.Context(), id)
	if err != nil {
		zap.L().Warn("get category failed", zap.Error(err))
		c.JSON(http.StatusNotFound, response.Failure("not_found", "category not found", nil))
		return
	}

	c.JSON(http.StatusOK, response.Success(ToResponse(categoryEntity), nil))
}

// Update godoc
// @Summary Update category
// @Tags Categories
// @Accept json
// @Produce json
// @Param id path string true "Category ID"
// @Param payload body UpdateRequest true "Update category payload"
// @Success 200 {object} response.Response
// @Failure 400 {object} response.Response
// @Failure 404 {object} response.Response
// @Failure 409 {object} response.Response
// @Failure 500 {object} response.Response
// @Router /api/v1/categories/{id} [put]
func (h *Handler) Update(c *gin.Context) {
	id, err := uuid.Parse(c.Param("id"))
	if err != nil {
		zap.L().Error("parse category id failed", zap.Error(err))
		c.JSON(http.StatusBadRequest, response.Failure("validation_error", "invalid category id", nil))
		return
	}

	var req UpdateRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		zap.L().Error("update category validation failed", zap.Error(err))
		c.JSON(http.StatusBadRequest, response.Failure("validation_error", "invalid payload", err.Error()))
		return
	}

	current, err := h.svc.GetByID(c.Request.Context(), id)
	if err != nil {
		zap.L().Warn("get category for update failed", zap.Error(err))
		c.JSON(http.StatusNotFound, response.Failure("not_found", "category not found", nil))
		return
	}

	updated, err := h.svc.Update(c.Request.Context(), current, req)
	if err != nil {
		if code, message, ok := uniqueConstraintError(err); ok {
			c.JSON(http.StatusConflict, response.Failure(code, message, nil))
			return
		}
		zap.L().Error("update category failed", zap.Error(err))
		c.JSON(http.StatusInternalServerError, response.Failure("internal_error", "failed to update category", err.Error()))
		return
	}

	c.JSON(http.StatusOK, response.Success(ToResponse(updated), nil))
}

// Delete godoc
// @Summary Delete category
// @Tags Categories
// @Produce json
// @Param id path string true "Category ID"
// @Success 200 {object} response.Response
// @Failure 400 {object} response.Response
// @Failure 500 {object} response.Response
// @Router /api/v1/categories/{id} [delete]
func (h *Handler) Delete(c *gin.Context) {
	id, err := uuid.Parse(c.Param("id"))
	if err != nil {
		zap.L().Error("parse category id failed", zap.Error(err))
		c.JSON(http.StatusBadRequest, response.Failure("validation_error", "invalid category id", nil))
		return
	}

	if err := h.svc.Delete(c.Request.Context(), id); err != nil {
		zap.L().Error("delete category failed", zap.Error(err))
		c.JSON(http.StatusInternalServerError, response.Failure("internal_error", "failed to delete category", err.Error()))
		return
	}

	c.JSON(http.StatusOK, response.Success(gin.H{"message": "category deleted"}, nil))
}

func uniqueConstraintError(err error) (string, string, bool) {
	var pgErr *pgconn.PgError
	if !errors.As(err, &pgErr) {
		return "", "", false
	}

	if pgErr.Code != "23505" {
		return "", "", false
	}

	switch pgErr.ConstraintName {
	case "idx_categories_name":
		return "category_name_exists", "category name already exists", true
	case "idx_categories_slug":
		return "category_slug_exists", "category slug already exists", true
	default:
		return "duplicate_entry", "duplicate entry", true
	}
}
