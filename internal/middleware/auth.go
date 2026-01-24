package middleware

import (
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
	"github.com/yourusername/go-users-api/internal/config"
	"github.com/yourusername/go-users-api/internal/response"
	"github.com/yourusername/go-users-api/internal/utils"
)

const (
	ContextUserID = "userID"
	ContextRole   = "role"
)

func Auth(cfg config.JWTConfig) gin.HandlerFunc {
	return func(c *gin.Context) {
		authHeader := c.GetHeader("Authorization")
		if authHeader == "" {
			c.AbortWithStatusJSON(http.StatusUnauthorized, response.Failure("unauthorized", "missing authorization header", nil))
			return
		}

		parts := strings.SplitN(authHeader, " ", 2)
		if len(parts) != 2 || !strings.EqualFold(parts[0], "Bearer") {
			c.AbortWithStatusJSON(http.StatusUnauthorized, response.Failure("unauthorized", "invalid authorization header", nil))
			return
		}

		claims, err := utils.ParseToken(parts[1], cfg.Secret)
		if err != nil || claims.TokenType != cfg.AccessTokenLabel {
			c.AbortWithStatusJSON(http.StatusUnauthorized, response.Failure("unauthorized", "invalid token", nil))
			return
		}

		c.Set(ContextUserID, claims.Subject)
		c.Set(ContextRole, claims.Role)
		c.Next()
	}
}
