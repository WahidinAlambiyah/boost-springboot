package product

import "github.com/google/uuid"

type CreateRequest struct {
	CategoryID  string  `json:"category_id" binding:"required,uuid"`
	Name        string  `json:"name" binding:"required,min=3"`
	SKU         string  `json:"sku" binding:"required"`
	Description *string `json:"description"`
	Price       float64 `json:"price" binding:"required,gte=0"`
	Stock       int     `json:"stock" binding:"required,gte=0"`
	IsActive    *bool   `json:"is_active"`
}

type UpdateRequest struct {
	CategoryID  *string  `json:"category_id" binding:"omitempty,uuid"`
	Name        string   `json:"name" binding:"omitempty,min=3"`
	SKU         string   `json:"sku" binding:"omitempty"`
	Description *string  `json:"description"`
	Price       *float64 `json:"price" binding:"omitempty,gte=0"`
	Stock       *int     `json:"stock" binding:"omitempty,gte=0"`
	IsActive    *bool    `json:"is_active"`
}

type CategoryInfo struct {
	ID   string `json:"id"`
	Name string `json:"name"`
	Slug string `json:"slug"`
}

type Response struct {
	ID          string        `json:"id"`
	CategoryID  string        `json:"category_id"`
	Category    *CategoryInfo `json:"category"`
	Name        string        `json:"name"`
	SKU         string        `json:"sku"`
	Description *string       `json:"description"`
	Price       float64       `json:"price"`
	Stock       int           `json:"stock"`
	IsActive    bool          `json:"is_active"`
	CreatedBy   *string       `json:"created_by"`
	CreatedAt   string        `json:"created_at"`
	UpdatedAt   string        `json:"updated_at"`
}

func ToResponse(entity Product) Response {
	var categoryInfo *CategoryInfo
	if entity.Category.ID != uuid.Nil {
		categoryInfo = &CategoryInfo{
			ID:   entity.Category.ID.String(),
			Name: entity.Category.Name,
			Slug: entity.Category.Slug,
		}
	}

	var createdBy *string
	if entity.CreatedBy != nil {
		value := entity.CreatedBy.String()
		createdBy = &value
	}

	return Response{
		ID:          entity.ID.String(),
		CategoryID:  entity.CategoryID.String(),
		Category:    categoryInfo,
		Name:        entity.Name,
		SKU:         entity.SKU,
		Description: entity.Description,
		Price:       entity.Price,
		Stock:       entity.Stock,
		IsActive:    entity.IsActive,
		CreatedBy:   createdBy,
		CreatedAt:   entity.CreatedAt.Format("2006-01-02T15:04:05Z07:00"),
		UpdatedAt:   entity.UpdatedAt.Format("2006-01-02T15:04:05Z07:00"),
	}
}
