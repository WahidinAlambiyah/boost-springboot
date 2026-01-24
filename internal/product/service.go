package product

import (
	"context"
	"errors"

	"github.com/google/uuid"
)

var (
	ErrProductNotFound = errors.New("product not found")
)

type Service struct {
	repo Repository
}

func NewService(repo Repository) *Service {
	return &Service{repo: repo}
}

func (s *Service) Create(ctx context.Context, req CreateRequest, createdBy *uuid.UUID) (Product, error) {
	categoryID, err := uuid.Parse(req.CategoryID)
	if err != nil {
		return Product{}, err
	}

	isActive := true
	if req.IsActive != nil {
		isActive = *req.IsActive
	}

	entity := Product{
		ID:          uuid.New(),
		CategoryID:  categoryID,
		Name:        req.Name,
		SKU:         req.SKU,
		Description: req.Description,
		Price:       req.Price,
		Stock:       req.Stock,
		IsActive:    isActive,
		CreatedBy:   createdBy,
	}

	if err := s.repo.Create(ctx, &entity); err != nil {
		return Product{}, err
	}
	return entity, nil
}

func (s *Service) List(ctx context.Context, filter ListFilter) ([]Product, int64, error) {
	return s.repo.List(ctx, filter)
}

func (s *Service) GetByID(ctx context.Context, id uuid.UUID) (Product, error) {
	entity, err := s.repo.GetByID(ctx, id)
	if err != nil {
		return Product{}, ErrProductNotFound
	}
	return entity, nil
}

func (s *Service) Update(ctx context.Context, target Product, req UpdateRequest) (Product, error) {
	if req.CategoryID != nil {
		categoryID, err := uuid.Parse(*req.CategoryID)
		if err != nil {
			return Product{}, err
		}
		target.CategoryID = categoryID
	}
	if req.Name != "" {
		target.Name = req.Name
	}
	if req.SKU != "" {
		target.SKU = req.SKU
	}
	if req.Description != nil {
		target.Description = req.Description
	}
	if req.Price != nil {
		target.Price = *req.Price
	}
	if req.Stock != nil {
		target.Stock = *req.Stock
	}
	if req.IsActive != nil {
		target.IsActive = *req.IsActive
	}

	if err := s.repo.Update(ctx, &target); err != nil {
		return Product{}, err
	}
	return target, nil
}

func (s *Service) Delete(ctx context.Context, id uuid.UUID) error {
	return s.repo.Delete(ctx, id)
}
