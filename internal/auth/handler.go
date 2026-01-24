package auth

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/yourusername/go-users-api/internal/response"
	"github.com/yourusername/go-users-api/internal/user"
	"go.uber.org/zap"
)

type Handler struct {
	svc *Service
}

func NewHandler(svc *Service) *Handler {
	return &Handler{svc: svc}
}

// Register godoc
// @Summary Register new user
// @Tags Auth
// @Accept json
// @Produce json
// @Param payload body RegisterRequest true "Register payload"
// @Success 201 {object} response.Response
// @Failure 400 {object} response.Response
// @Failure 500 {object} response.Response
// @Router /api/v1/auth/register [post]
func (h *Handler) Register(c *gin.Context) {
	var req RegisterRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		zap.L().Error("register validation failed", zap.Error(err))
		c.JSON(http.StatusBadRequest, response.Failure("validation_error", "invalid payload", err.Error()))
		return
	}

	newUser, err := h.svc.Register(c.Request.Context(), req)
	if err != nil {
		zap.L().Error("register failed", zap.Error(err))
		c.JSON(http.StatusInternalServerError, response.Failure("internal_error", "failed to register", err.Error()))
		return
	}

	c.JSON(http.StatusCreated, response.Success(user.ToResponse(newUser), nil))
}

// Login godoc
// @Summary Login user
// @Tags Auth
// @Accept json
// @Produce json
// @Param payload body LoginRequest true "Login payload"
// @Success 200 {object} response.Response
// @Failure 400 {object} response.Response
// @Failure 401 {object} response.Response
// @Failure 500 {object} response.Response
// @Router /api/v1/auth/login [post]
func (h *Handler) Login(c *gin.Context) {
	var req LoginRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		zap.L().Error("login validation failed", zap.Error(err))
		c.JSON(http.StatusBadRequest, response.Failure("validation_error", "invalid payload", err.Error()))
		return
	}

	result, err := h.svc.Login(c.Request.Context(), req)
	if err != nil {
		if err == ErrInvalidCredentials {
			zap.L().Warn("login invalid credentials", zap.Error(err))
			c.JSON(http.StatusUnauthorized, response.Failure("unauthorized", "invalid credentials", nil))
			return
		}
		zap.L().Error("login failed", zap.Error(err))
		c.JSON(http.StatusInternalServerError, response.Failure("internal_error", "login failed", err.Error()))
		return
	}

	c.JSON(http.StatusOK, response.Success(result, nil))
}

// Refresh godoc
// @Summary Refresh access token
// @Tags Auth
// @Accept json
// @Produce json
// @Param payload body RefreshRequest true "Refresh payload"
// @Success 200 {object} response.Response
// @Failure 400 {object} response.Response
// @Failure 401 {object} response.Response
// @Failure 500 {object} response.Response
// @Router /api/v1/auth/refresh [post]
func (h *Handler) Refresh(c *gin.Context) {
	var req RefreshRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		zap.L().Error("refresh validation failed", zap.Error(err))
		c.JSON(http.StatusBadRequest, response.Failure("validation_error", "invalid payload", err.Error()))
		return
	}

	result, err := h.svc.Refresh(c.Request.Context(), req.RefreshToken)
	if err != nil {
		zap.L().Warn("refresh failed", zap.Error(err))
		c.JSON(http.StatusUnauthorized, response.Failure("unauthorized", "invalid refresh token", nil))
		return
	}

	c.JSON(http.StatusOK, response.Success(result, nil))
}

// Logout godoc
// @Summary Logout user
// @Tags Auth
// @Accept json
// @Produce json
// @Param payload body LogoutRequest true "Logout payload"
// @Success 200 {object} response.Response
// @Failure 400 {object} response.Response
// @Failure 401 {object} response.Response
// @Failure 500 {object} response.Response
// @Router /api/v1/auth/logout [post]
func (h *Handler) Logout(c *gin.Context) {
	var req LogoutRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		zap.L().Error("logout validation failed", zap.Error(err))
		c.JSON(http.StatusBadRequest, response.Failure("validation_error", "invalid payload", err.Error()))
		return
	}

	if err := h.svc.Logout(c.Request.Context(), req.RefreshToken); err != nil {
		zap.L().Warn("logout failed", zap.Error(err))
		c.JSON(http.StatusUnauthorized, response.Failure("unauthorized", "invalid refresh token", nil))
		return
	}

	c.JSON(http.StatusOK, response.Success(gin.H{"message": "logout successful"}, nil))
}
