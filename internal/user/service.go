package user

import (
	"context"
	"errors"

	"github.com/google/uuid"
	rolepkg "github.com/yourusername/go-users-api/internal/role"
	"github.com/yourusername/go-users-api/internal/utils"
	"gorm.io/gorm"
)

var (
	ErrUserNotFound = errors.New("user not found")
	ErrRoleNotFound = errors.New("role not found")
)

type Service struct {
	repo     Repository
	roleRepo RoleRepository
}

type RoleRepository interface {
	GetByName(ctx context.Context, name string) (rolepkg.Role, error)
}

func NewService(repo Repository, roleRepo RoleRepository) *Service {
	return &Service{repo: repo, roleRepo: roleRepo}
}

func (s *Service) Create(ctx context.Context, req CreateRequest, role Role) (User, error) {
	roleEntity, err := s.roleRepo.GetByName(ctx, string(role))
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return User{}, ErrRoleNotFound
		}
		return User{}, err
	}

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
		RoleID:       roleEntity.ID,
		Role:         roleEntity,
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
		roleEntity, err := s.roleRepo.GetByName(ctx, req.Role)
		if err != nil {
			if errors.Is(err, gorm.ErrRecordNotFound) {
				return User{}, ErrRoleNotFound
			}
			return User{}, err
		}
		target.RoleID = roleEntity.ID
		target.Role = roleEntity
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
