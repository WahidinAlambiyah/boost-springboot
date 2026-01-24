package product

import (
	"time"

	"github.com/google/uuid"
	"github.com/yourusername/go-users-api/internal/category"
	"github.com/yourusername/go-users-api/internal/user"
)

type Product struct {
	ID            uuid.UUID         `gorm:"type:uuid;primaryKey"`
	CategoryID    uuid.UUID         `gorm:"type:uuid;column:category_id"`
	Category      category.Category `gorm:"foreignKey:CategoryID"`
	Name          string            `gorm:"not null"`
	SKU           string            `gorm:"uniqueIndex;not null"`
	Description   *string           `gorm:"type:text"`
	Price         float64           `gorm:"type:numeric(12,2);not null"`
	Stock         int               `gorm:"not null"`
	IsActive      bool              `gorm:"column:is_active;default:true"`
	CreatedBy     *uuid.UUID        `gorm:"type:uuid;column:created_by"`
	CreatedByUser *user.User        `gorm:"foreignKey:CreatedBy"`
	CreatedAt     time.Time         `gorm:"column:created_at"`
	UpdatedAt     time.Time         `gorm:"column:updated_at"`
}

func (Product) TableName() string {
	return "products"
}
