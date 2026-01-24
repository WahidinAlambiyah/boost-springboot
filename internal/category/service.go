package category

import (
	"context"
	"errors"
	"regexp"
	"strings"

	"github.com/google/uuid"
)

var (
	ErrCategoryNotFound = errors.New("category not found")
)

type Service struct {
	repo Repository
}

func NewService(repo Repository) *Service {
	return &Service{repo: repo}
}

func (s *Service) Create(ctx context.Context, req CreateRequest) (Category, error) {
	slug := strings.TrimSpace(req.Slug)
	if slug == "" {
		slug = slugify(req.Name)
	} else {
		slug = slugify(slug)
	}

	isActive := true
	if req.IsActive != nil {
		isActive = *req.IsActive
	}

	entity := Category{
		ID:          uuid.New(),
		Name:        req.Name,
		Slug:        slug,
		Description: req.Description,
		IsActive:    isActive,
	}

	if err := s.repo.Create(ctx, &entity); err != nil {
		return Category{}, err
	}
	return entity, nil
}

func (s *Service) List(ctx context.Context) ([]Category, error) {
	return s.repo.List(ctx)
}

func (s *Service) GetByID(ctx context.Context, id uuid.UUID) (Category, error) {
	entity, err := s.repo.GetByID(ctx, id)
	if err != nil {
		return Category{}, ErrCategoryNotFound
	}
	return entity, nil
}

func (s *Service) Update(ctx context.Context, target Category, req UpdateRequest) (Category, error) {
	if req.Name != "" {
		target.Name = req.Name
	}
	if req.Slug != "" {
		target.Slug = slugify(req.Slug)
	}
	if req.Description != nil {
		target.Description = req.Description
	}
	if req.IsActive != nil {
		target.IsActive = *req.IsActive
	}

	if err := s.repo.Update(ctx, &target); err != nil {
		return Category{}, err
	}
	return target, nil
}

func (s *Service) Delete(ctx context.Context, id uuid.UUID) error {
	return s.repo.Delete(ctx, id)
}

func slugify(input string) string {
	normalized := strings.ToLower(strings.TrimSpace(input))
	re := regexp.MustCompile(`[^a-z0-9]+`)
	slug := re.ReplaceAllString(normalized, "-")
	return strings.Trim(slug, "-")
}
