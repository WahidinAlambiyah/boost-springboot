package product

import (
	"errors"
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"github.com/jackc/pgx/v5/pgconn"
	"github.com/yourusername/go-users-api/internal/middleware"
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
// @Summary List products
// @Tags Products
// @Produce json
// @Param category_id query string false "Filter by category ID"
// @Param search query string false "Search by name or SKU"
// @Param sort query string false "Sort by name, price, created_at, updated_at, stock (prefix with - for desc)"
// @Param page query int false "Page number"
// @Param size query int false "Page size"
// @Success 200 {object} response.Response
// @Failure 400 {object} response.Response
// @Failure 500 {object} response.Response
// @Router /api/v1/products [get]
func (h *Handler) List(c *gin.Context) {
	var filter ListFilter

	if value := c.Query("category_id"); value != "" {
		categoryID, err := uuid.Parse(value)
		if err != nil {
			c.JSON(http.StatusBadRequest, response.Failure("validation_error", "invalid category_id", nil))
			return
		}
		filter.CategoryID = &categoryID
	}

	filter.Search = c.Query("search")
	filter.Sort = c.Query("sort")

	page, err := strconv.Atoi(c.DefaultQuery("page", "1"))
	if err != nil || page < 1 {
		c.JSON(http.StatusBadRequest, response.Failure("validation_error", "invalid page", nil))
		return
	}

	size, err := strconv.Atoi(c.DefaultQuery("size", "10"))
	if err != nil || size < 1 {
		c.JSON(http.StatusBadRequest, response.Failure("validation_error", "invalid size", nil))
		return
	}
	if size > 100 {
		size = 100
	}

	filter.Page = page
	filter.Size = size

	products, total, err := h.svc.List(c.Request.Context(), filter)
	if err != nil {
		zap.L().Error("list products failed", zap.Error(err))
		c.JSON(http.StatusInternalServerError, response.Failure("internal_error", "failed to fetch products", err.Error()))
		return
	}

	responses := make([]Response, 0, len(products))
	for _, entity := range products {
		responses = append(responses, ToResponse(entity))
	}

	meta := gin.H{"page": page, "size": size, "total": total}
	c.JSON(http.StatusOK, response.Success(responses, meta))
}

// Create godoc
// @Summary Create product
// @Tags Products
// @Accept json
// @Produce json
// @Param payload body CreateRequest true "Create product payload"
// @Success 201 {object} response.Response
// @Failure 400 {object} response.Response
// @Failure 401 {object} response.Response
// @Failure 409 {object} response.Response
// @Failure 500 {object} response.Response
// @Router /api/v1/products [post]
func (h *Handler) Create(c *gin.Context) {
	var req CreateRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		zap.L().Error("create product validation failed", zap.Error(err))
		c.JSON(http.StatusBadRequest, response.Failure("validation_error", "invalid payload", err.Error()))
		return
	}

	createdBy, err := parseUserID(c)
	if err != nil {
		c.JSON(http.StatusUnauthorized, response.Failure("unauthorized", "invalid user", nil))
		return
	}

	created, err := h.svc.Create(c.Request.Context(), req, createdBy)
	if err != nil {
		if code, message, ok := uniqueConstraintError(err); ok {
			c.JSON(http.StatusConflict, response.Failure(code, message, nil))
			return
		}
		zap.L().Error("create product failed", zap.Error(err))
		c.JSON(http.StatusInternalServerError, response.Failure("internal_error", "failed to create product", err.Error()))
		return
	}

	c.JSON(http.StatusCreated, response.Success(ToResponse(created), nil))
}

// Get godoc
// @Summary Get product by ID
// @Tags Products
// @Produce json
// @Param id path string true "Product ID"
// @Success 200 {object} response.Response
// @Failure 400 {object} response.Response
// @Failure 404 {object} response.Response
// @Failure 500 {object} response.Response
// @Router /api/v1/products/{id} [get]
func (h *Handler) Get(c *gin.Context) {
	id, err := uuid.Parse(c.Param("id"))
	if err != nil {
		zap.L().Error("parse product id failed", zap.Error(err))
		c.JSON(http.StatusBadRequest, response.Failure("validation_error", "invalid product id", nil))
		return
	}

	productEntity, err := h.svc.GetByID(c.Request.Context(), id)
	if err != nil {
		zap.L().Warn("get product failed", zap.Error(err))
		c.JSON(http.StatusNotFound, response.Failure("not_found", "product not found", nil))
		return
	}

	c.JSON(http.StatusOK, response.Success(ToResponse(productEntity), nil))
}

// Update godoc
// @Summary Update product
// @Tags Products
// @Accept json
// @Produce json
// @Param id path string true "Product ID"
// @Param payload body UpdateRequest true "Update product payload"
// @Success 200 {object} response.Response
// @Failure 400 {object} response.Response
// @Failure 404 {object} response.Response
// @Failure 409 {object} response.Response
// @Failure 500 {object} response.Response
// @Router /api/v1/products/{id} [put]
func (h *Handler) Update(c *gin.Context) {
	id, err := uuid.Parse(c.Param("id"))
	if err != nil {
		zap.L().Error("parse product id failed", zap.Error(err))
		c.JSON(http.StatusBadRequest, response.Failure("validation_error", "invalid product id", nil))
		return
	}

	var req UpdateRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		zap.L().Error("update product validation failed", zap.Error(err))
		c.JSON(http.StatusBadRequest, response.Failure("validation_error", "invalid payload", err.Error()))
		return
	}

	current, err := h.svc.GetByID(c.Request.Context(), id)
	if err != nil {
		zap.L().Warn("get product for update failed", zap.Error(err))
		c.JSON(http.StatusNotFound, response.Failure("not_found", "product not found", nil))
		return
	}

	updated, err := h.svc.Update(c.Request.Context(), current, req)
	if err != nil {
		if code, message, ok := uniqueConstraintError(err); ok {
			c.JSON(http.StatusConflict, response.Failure(code, message, nil))
			return
		}
		zap.L().Error("update product failed", zap.Error(err))
		c.JSON(http.StatusInternalServerError, response.Failure("internal_error", "failed to update product", err.Error()))
		return
	}

	c.JSON(http.StatusOK, response.Success(ToResponse(updated), nil))
}

// Delete godoc
// @Summary Delete product
// @Tags Products
// @Produce json
// @Param id path string true "Product ID"
// @Success 200 {object} response.Response
// @Failure 400 {object} response.Response
// @Failure 500 {object} response.Response
// @Router /api/v1/products/{id} [delete]
func (h *Handler) Delete(c *gin.Context) {
	id, err := uuid.Parse(c.Param("id"))
	if err != nil {
		zap.L().Error("parse product id failed", zap.Error(err))
		c.JSON(http.StatusBadRequest, response.Failure("validation_error", "invalid product id", nil))
		return
	}

	if err := h.svc.Delete(c.Request.Context(), id); err != nil {
		zap.L().Error("delete product failed", zap.Error(err))
		c.JSON(http.StatusInternalServerError, response.Failure("internal_error", "failed to delete product", err.Error()))
		return
	}

	c.JSON(http.StatusOK, response.Success(gin.H{"message": "product deleted"}, nil))
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
	case "idx_products_sku":
		return "product_sku_exists", "product SKU already exists", true
	default:
		return "duplicate_entry", "duplicate entry", true
	}
}

func parseUserID(c *gin.Context) (*uuid.UUID, error) {
	value, exists := c.Get(middleware.ContextUserID)
	if !exists {
		return nil, errors.New("missing user id")
	}

	raw, ok := value.(string)
	if !ok {
		return nil, errors.New("invalid user id")
	}

	id, err := uuid.Parse(raw)
	if err != nil {
		return nil, err
	}

	return &id, nil
}
