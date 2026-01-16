package main

import (
	"context"
	"log"
	"net/http"
	"time"

	"github.com/gin-gonic/gin"
	swaggerFiles "github.com/swaggo/files"
	"github.com/swaggo/gin-swagger"
	"github.com/yourusername/go-users-api/docs"
	"github.com/yourusername/go-users-api/internal/auth"
	"github.com/yourusername/go-users-api/internal/cache"
	"github.com/yourusername/go-users-api/internal/config"
	"github.com/yourusername/go-users-api/internal/db"
	"github.com/yourusername/go-users-api/internal/middleware"
	"github.com/yourusername/go-users-api/internal/response"
	"github.com/yourusername/go-users-api/internal/user"
	"go.uber.org/zap"
)

// @title Go Users API
// @version 1.0
// @description User management API built with Gin and GORM.
// @host localhost:8080
// @BasePath /
// @schemes http
func main() {
	cfg, err := config.Load()
	if err != nil {
		log.Fatalf("failed to load config: %v", err)
	}

	logger, _ := zap.NewProduction()
	defer func() {
		_ = logger.Sync()
	}()

	postgres, err := db.NewPostgres(cfg.DB)
	if err != nil {
		logger.Fatal("failed to connect postgres", zap.Error(err))
	}

	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	if err := db.Ping(ctx, postgres); err != nil {
		logger.Fatal("failed to ping postgres", zap.Error(err))
	}

	redisClient := cache.NewRedis(cfg.Redis)
	if err := cache.Ping(ctx, redisClient); err != nil {
		logger.Fatal("failed to ping redis", zap.Error(err))
	}

	userRepo := user.NewGormRepository(postgres)
	userService := user.NewService(userRepo)
	userHandler := user.NewHandler(userService)

	tokenStore := auth.NewRedisTokenStore(redisClient)
	authService := auth.NewService(userRepo, tokenStore, cfg.JWT)
	authHandler := auth.NewHandler(authService)

	router := gin.New()
	router.Use(gin.Recovery())
	router.Use(gin.Logger())

	docs.SwaggerInfo.Host = "localhost:" + cfg.AppPort

	router.GET("/health", func(c *gin.Context) {
		c.JSON(http.StatusOK, response.Success(gin.H{"status": "ok"}, nil))
	})

	api := router.Group("/api/v1")
	{
		authGroup := api.Group("/auth")
		authGroup.POST("/register", authHandler.Register)
		authGroup.POST("/login", authHandler.Login)
		authGroup.POST("/refresh", authHandler.Refresh)
		authGroup.POST("/logout", authHandler.Logout)

		protected := api.Group("")
		protected.Use(middleware.Auth(cfg.JWT))
		protected.GET("/users", userHandler.List)
		protected.POST("/users", userHandler.Create)
		protected.GET("/users/:id", userHandler.Get)
		protected.PUT("/users/:id", userHandler.Update)
		protected.DELETE("/users/:id", userHandler.Delete)
		protected.GET("/me", userHandler.Me)
	}

	router.GET("/swagger/*any", ginSwagger.WrapHandler(swaggerFiles.Handler))

	server := &http.Server{
		Addr:              ":" + cfg.AppPort,
		Handler:           router,
		ReadHeaderTimeout: 5 * time.Second,
	}

	logger.Info("server running", zap.String("port", cfg.AppPort))
	if err := server.ListenAndServe(); err != nil && err != http.ErrServerClosed {
		logger.Fatal("server error", zap.Error(err))
	}
}
