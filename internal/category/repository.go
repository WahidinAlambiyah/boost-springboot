package category

import (
	"context"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

type Repository interface {
	Create(ctx context.Context, category *Category) error
	GetByID(ctx context.Context, id uuid.UUID) (Category, error)
	List(ctx context.Context) ([]Category, error)
	Update(ctx context.Context, category *Category) error
	Delete(ctx context.Context, id uuid.UUID) error
}

type GormRepository struct {
	db *gorm.DB
}

func NewGormRepository(db *gorm.DB) *GormRepository {
	return &GormRepository{db: db}
}

func (r *GormRepository) Create(ctx context.Context, category *Category) error {
	return r.db.WithContext(ctx).Create(category).Error
}

func (r *GormRepository) GetByID(ctx context.Context, id uuid.UUID) (Category, error) {
	var category Category
	result := r.db.WithContext(ctx).First(&category, "id = ?", id)
	return category, result.Error
}

func (r *GormRepository) List(ctx context.Context) ([]Category, error) {
	var categories []Category
	result := r.db.WithContext(ctx).Order("created_at desc").Find(&categories)
	return categories, result.Error
}

func (r *GormRepository) Update(ctx context.Context, category *Category) error {
	return r.db.WithContext(ctx).Save(category).Error
}

func (r *GormRepository) Delete(ctx context.Context, id uuid.UUID) error {
	return r.db.WithContext(ctx).Delete(&Category{}, "id = ?", id).Error
}
