package role

import (
	"context"
	"errors"

	"github.com/google/uuid"
)

var (
	ErrRoleNotFound = errors.New("role not found")
)

type Service struct {
	repo Repository
}

func NewService(repo Repository) *Service {
	return &Service{repo: repo}
}

func (s *Service) Create(ctx context.Context, req CreateRequest) (Role, error) {
	role := Role{
		ID:          uuid.New(),
		Name:        req.Name,
		Description: req.Description,
	}
	if err := s.repo.Create(ctx, &role); err != nil {
		return Role{}, err
	}
	return role, nil
}

func (s *Service) List(ctx context.Context) ([]Role, error) {
	return s.repo.List(ctx)
}

func (s *Service) GetByID(ctx context.Context, id uuid.UUID) (Role, error) {
	role, err := s.repo.GetByID(ctx, id)
	if err != nil {
		return Role{}, ErrRoleNotFound
	}
	return role, nil
}

func (s *Service) Update(ctx context.Context, target Role, req UpdateRequest) (Role, error) {
	if req.Name != "" {
		target.Name = req.Name
	}
	if req.Description != "" {
		target.Description = req.Description
	}
	if err := s.repo.Update(ctx, &target); err != nil {
		return Role{}, err
	}
	return target, nil
}

func (s *Service) Delete(ctx context.Context, id uuid.UUID) error {
	return s.repo.Delete(ctx, id)
}
