package user

import (
	"context"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

type Repository interface {
	Create(ctx context.Context, user *User) error
	GetByID(ctx context.Context, id uuid.UUID) (User, error)
	GetByEmailOrUsername(ctx context.Context, identifier string) (User, error)
	List(ctx context.Context) ([]User, error)
	Update(ctx context.Context, user *User) error
	Delete(ctx context.Context, id uuid.UUID) error
}

type GormRepository struct {
	db *gorm.DB
}

func NewGormRepository(db *gorm.DB) *GormRepository {
	return &GormRepository{db: db}
}

func (r *GormRepository) Create(ctx context.Context, user *User) error {
	return r.db.WithContext(ctx).Create(user).Error
}

func (r *GormRepository) GetByID(ctx context.Context, id uuid.UUID) (User, error) {
	var user User
	result := r.db.WithContext(ctx).Preload("Role").First(&user, "id = ?", id)
	return user, result.Error
}

func (r *GormRepository) GetByEmailOrUsername(ctx context.Context, identifier string) (User, error) {
	var user User
	result := r.db.WithContext(ctx).
		Preload("Role").
		Where("email = ? OR username = ?", identifier, identifier).
		First(&user)
	return user, result.Error
}

func (r *GormRepository) List(ctx context.Context) ([]User, error) {
	var users []User
	result := r.db.WithContext(ctx).Preload("Role").Order("created_at desc").Find(&users)
	return users, result.Error
}

func (r *GormRepository) Update(ctx context.Context, user *User) error {
	return r.db.WithContext(ctx).Save(user).Error
}

func (r *GormRepository) Delete(ctx context.Context, id uuid.UUID) error {
	return r.db.WithContext(ctx).Delete(&User{}, "id = ?", id).Error
}
