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
	"github.com/yourusername/go-users-api/internal/category"
	"github.com/yourusername/go-users-api/internal/config"
	"github.com/yourusername/go-users-api/internal/db"
	"github.com/yourusername/go-users-api/internal/middleware"
	"github.com/yourusername/go-users-api/internal/product"
	"github.com/yourusername/go-users-api/internal/response"
	"github.com/yourusername/go-users-api/internal/role"
	"github.com/yourusername/go-users-api/internal/user"
	"go.uber.org/zap"
)

// @title Go Users API
// @version 1.0
// @description User management API built with Gin and GORM.
// @tag.name Roles
// @tag.description Roles CRUD endpoints.
// @tag.name Categories
// @tag.description Category management endpoints.
// @tag.name Products
// @tag.description Product management endpoints.
// @host localhost:8080
// @BasePath /
// @schemes http
func main() {
	cfg, err := config.Load()
	if err != nil {
		log.Fatalf("failed to load config: %v", err)
	}

	logger, _ := zap.NewProduction(zap.AddCaller())
	zap.ReplaceGlobals(logger)
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

	userRepo := user.NewGormRepository(postgres)
	roleRepo := role.NewGormRepository(postgres)
	userService := user.NewService(userRepo, roleRepo)
	userHandler := user.NewHandler(userService)

	roleService := role.NewService(roleRepo)
	roleHandler := role.NewHandler(roleService)

	categoryRepo := category.NewGormRepository(postgres)
	categoryService := category.NewService(categoryRepo)
	categoryHandler := category.NewHandler(categoryService)

	productRepo := product.NewGormRepository(postgres)
	productService := product.NewService(productRepo)
	productHandler := product.NewHandler(productService)

	var tokenStore auth.TokenStore
	if cfg.Redis.Enabled {
		redisClient := cache.NewRedis(cfg.Redis)
		if err := cache.Ping(ctx, redisClient); err != nil {
			logger.Fatal("failed to ping redis", zap.Error(err))
		}
		tokenStore = auth.NewRedisTokenStore(redisClient)
	} else {
		logger.Warn("redis disabled, using in-memory token store")
		tokenStore = auth.NewMemoryTokenStore()
	}
	authService := auth.NewService(userRepo, roleRepo, tokenStore, cfg.JWT)
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

		api.GET("/users", userHandler.List)
		api.POST("/users", userHandler.Create)
		api.GET("/users/:id", userHandler.Get)
		api.PUT("/users/:id", userHandler.Update)
		api.DELETE("/users/:id", userHandler.Delete)

		api.GET("/roles", roleHandler.List)
		api.POST("/roles", roleHandler.Create)
		api.GET("/roles/:id", roleHandler.Get)
		api.PUT("/roles/:id", roleHandler.Update)
		api.DELETE("/roles/:id", roleHandler.Delete)

		api.GET("/categories", categoryHandler.List)
		api.GET("/categories/:id", categoryHandler.Get)

		api.GET("/products", productHandler.List)
		api.GET("/products/:id", productHandler.Get)

		adminGroup := api.Group("", middleware.Auth(cfg.JWT), middleware.RequireRole(string(user.RoleAdmin)))
		adminGroup.POST("/categories", categoryHandler.Create)
		adminGroup.PUT("/categories/:id", categoryHandler.Update)
		adminGroup.DELETE("/categories/:id", categoryHandler.Delete)

		adminGroup.POST("/products", productHandler.Create)
		adminGroup.PUT("/products/:id", productHandler.Update)
		adminGroup.DELETE("/products/:id", productHandler.Delete)
	}

	router.GET("/swagger/*any", ginSwagger.WrapHandler(swaggerFiles.Handler, ginSwagger.DefaultModelsExpandDepth(-1)))

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
