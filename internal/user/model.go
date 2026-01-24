package user

import (
	"time"

	"github.com/google/uuid"
)

type Role string

const (
	RoleAdmin Role = "ADMIN"
	RoleUser  Role = "USER"
)

type User struct {
	ID           uuid.UUID `gorm:"type:uuid;primaryKey"`
	FullName     string    `gorm:"column:full_name"`
	Email        string    `gorm:"uniqueIndex"`
	Username     string    `gorm:"uniqueIndex"`
	PasswordHash string    `gorm:"column:password_hash"`
	Role         Role      `gorm:"type:varchar(16)"`
	IsActive     bool      `gorm:"column:is_active"`
	CreatedAt    time.Time `gorm:"column:created_at"`
	UpdatedAt    time.Time `gorm:"column:updated_at"`
}

func (User) TableName() string {
	return "users"
}
