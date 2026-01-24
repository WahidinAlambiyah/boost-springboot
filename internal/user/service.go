package user

import (
	"context"
	"errors"

	"github.com/google/uuid"
	"github.com/yourusername/go-users-api/internal/utils"
)

var (
	ErrUserNotFound = errors.New("user not found")
)

type Service struct {
	repo Repository
}

func NewService(repo Repository) *Service {
	return &Service{repo: repo}
}

func (s *Service) Create(ctx context.Context, req CreateRequest, role Role) (User, error) {
	hash, err := utils.HashPassword(req.Password)
	if err != nil {
		return User{}, err
	}

	user := User{
		ID:           uuid.New(),
		FullName:     req.FullName,
		Email:        req.Email,
		Username:     req.Username,
		PasswordHash: hash,
		Role:         role,
		IsActive:     true,
	}

	if err := s.repo.Create(ctx, &user); err != nil {
		return User{}, err
	}
	return user, nil
}

func (s *Service) List(ctx context.Context) ([]User, error) {
	return s.repo.List(ctx)
}

func (s *Service) GetByID(ctx context.Context, id uuid.UUID) (User, error) {
	user, err := s.repo.GetByID(ctx, id)
	if err != nil {
		return User{}, ErrUserNotFound
	}
	return user, nil
}

func (s *Service) Update(ctx context.Context, target User, req UpdateRequest, canEditRole bool) (User, error) {
	if req.FullName != "" {
		target.FullName = req.FullName
	}
	if req.Email != "" {
		target.Email = req.Email
	}
	if req.Username != "" {
		target.Username = req.Username
	}
	if req.Password != "" {
		hash, err := utils.HashPassword(req.Password)
		if err != nil {
			return User{}, err
		}
		target.PasswordHash = hash
	}
	if req.Role != "" && canEditRole {
		target.Role = Role(req.Role)
	}
	if req.IsActive != nil {
		target.IsActive = *req.IsActive
	}

	if err := s.repo.Update(ctx, &target); err != nil {
		return User{}, err
	}
	return target, nil
}

func (s *Service) Delete(ctx context.Context, id uuid.UUID) error {
	return s.repo.Delete(ctx, id)
}
