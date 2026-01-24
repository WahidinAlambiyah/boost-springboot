package role

import (
	"context"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

type Repository interface {
	Create(ctx context.Context, role *Role) error
	GetByID(ctx context.Context, id uuid.UUID) (Role, error)
	GetByName(ctx context.Context, name string) (Role, error)
	List(ctx context.Context) ([]Role, error)
	Update(ctx context.Context, role *Role) error
	Delete(ctx context.Context, id uuid.UUID) error
}

type GormRepository struct {
	db *gorm.DB
}

func NewGormRepository(db *gorm.DB) *GormRepository {
	return &GormRepository{db: db}
}

func (r *GormRepository) Create(ctx context.Context, role *Role) error {
	return r.db.WithContext(ctx).Create(role).Error
}

func (r *GormRepository) GetByID(ctx context.Context, id uuid.UUID) (Role, error) {
	var role Role
	result := r.db.WithContext(ctx).First(&role, "id = ?", id)
	return role, result.Error
}

func (r *GormRepository) GetByName(ctx context.Context, name string) (Role, error) {
	var role Role
	result := r.db.WithContext(ctx).First(&role, "name = ?", name)
	return role, result.Error
}

func (r *GormRepository) List(ctx context.Context) ([]Role, error) {
	var roles []Role
	result := r.db.WithContext(ctx).Order("created_at desc").Find(&roles)
	return roles, result.Error
}

func (r *GormRepository) Update(ctx context.Context, role *Role) error {
	return r.db.WithContext(ctx).Save(role).Error
}

func (r *GormRepository) Delete(ctx context.Context, id uuid.UUID) error {
	return r.db.WithContext(ctx).Delete(&Role{}, "id = ?", id).Error
}
