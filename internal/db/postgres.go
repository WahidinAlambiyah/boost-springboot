package db

import (
	"context"
	"fmt"
	"time"

	"github.com/yourusername/go-users-api/internal/config"
	"gorm.io/driver/postgres"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
)

func NewPostgres(cfg config.DBConfig) (*gorm.DB, error) {
	dsn := fmt.Sprintf("host=%s port=%s user=%s password=%s dbname=%s sslmode=%s search_path=%s", cfg.Host, cfg.Port, cfg.User, cfg.Password, cfg.Name, cfg.SSLMode, cfg.Schema)
	gormCfg := &gorm.Config{
		Logger: logger.Default.LogMode(logger.Warn),
	}
	driverCfg := postgres.Config{
		DSN:                  dsn,
		PreferSimpleProtocol: true,
	}
	return gorm.Open(postgres.New(driverCfg), gormCfg)
}

func Ping(ctx context.Context, database *gorm.DB) error {
	sqlDB, err := database.DB()
	if err != nil {
		return err
	}
	ctx, cancel := context.WithTimeout(ctx, 2*time.Second)
	defer cancel()
	return sqlDB.PingContext(ctx)
}
