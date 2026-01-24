package product

import (
	"context"
	"strings"

	"github.com/google/uuid"
	"gorm.io/gorm"
)

type ListFilter struct {
	CategoryID *uuid.UUID
	Search     string
	Sort       string
	Page       int
	Size       int
}

type Repository interface {
	Create(ctx context.Context, product *Product) error
	GetByID(ctx context.Context, id uuid.UUID) (Product, error)
	List(ctx context.Context, filter ListFilter) ([]Product, int64, error)
	Update(ctx context.Context, product *Product) error
	Delete(ctx context.Context, id uuid.UUID) error
}

type GormRepository struct {
	db *gorm.DB
}

func NewGormRepository(db *gorm.DB) *GormRepository {
	return &GormRepository{db: db}
}

func (r *GormRepository) Create(ctx context.Context, product *Product) error {
	return r.db.WithContext(ctx).Create(product).Error
}

func (r *GormRepository) GetByID(ctx context.Context, id uuid.UUID) (Product, error) {
	var product Product
	result := r.db.WithContext(ctx).
		Preload("Category").
		Preload("CreatedByUser").
		First(&product, "id = ?", id)
	return product, result.Error
}

func (r *GormRepository) List(ctx context.Context, filter ListFilter) ([]Product, int64, error) {
	query := r.db.WithContext(ctx).Model(&Product{}).
		Preload("Category").
		Preload("CreatedByUser")

	if filter.CategoryID != nil {
		query = query.Where("category_id = ?", *filter.CategoryID)
	}

	if filter.Search != "" {
		like := "%" + strings.ToLower(filter.Search) + "%"
		query = query.Where("LOWER(name) LIKE ? OR LOWER(sku) LIKE ?", like, like)
	}

	var total int64
	if err := query.Count(&total).Error; err != nil {
		return nil, 0, err
	}

	order := buildOrder(filter.Sort)
	query = query.Order(order)

	if filter.Size > 0 {
		offset := (filter.Page - 1) * filter.Size
		query = query.Limit(filter.Size).Offset(offset)
	}

	var products []Product
	if err := query.Find(&products).Error; err != nil {
		return nil, 0, err
	}

	return products, total, nil
}

func (r *GormRepository) Update(ctx context.Context, product *Product) error {
	return r.db.WithContext(ctx).Save(product).Error
}

func (r *GormRepository) Delete(ctx context.Context, id uuid.UUID) error {
	return r.db.WithContext(ctx).Delete(&Product{}, "id = ?", id).Error
}

func buildOrder(sort string) string {
	allowed := map[string]string{
		"name":       "name",
		"price":      "price",
		"created_at": "created_at",
		"updated_at": "updated_at",
		"stock":      "stock",
	}

	if sort == "" {
		return "created_at desc"
	}

	direction := "asc"
	key := sort
	if strings.HasPrefix(sort, "-") {
		direction = "desc"
		key = strings.TrimPrefix(sort, "-")
	}

	column, ok := allowed[key]
	if !ok {
		return "created_at desc"
	}

	return column + " " + direction
}
