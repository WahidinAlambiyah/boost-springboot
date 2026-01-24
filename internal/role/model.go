package role

import (
	"time"

	"github.com/google/uuid"
)

type Role struct {
	ID          uuid.UUID `gorm:"type:uuid;primaryKey"`
	Name        string    `gorm:"uniqueIndex"`
	Description string    `gorm:"column:description"`
	CreatedAt   time.Time `gorm:"column:created_at"`
	UpdatedAt   time.Time `gorm:"column:updated_at"`
}

func (Role) TableName() string {
	return "roles"
}
