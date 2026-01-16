package auth

import (
	"context"
	"errors"
	"fmt"

	"github.com/google/uuid"
	"github.com/yourusername/go-users-api/internal/config"
	"github.com/yourusername/go-users-api/internal/user"
	"github.com/yourusername/go-users-api/internal/utils"
	"gorm.io/gorm"
)

var (
	ErrInvalidCredentials = errors.New("invalid credentials")
	ErrInvalidToken       = errors.New("invalid token")
)

type Service struct {
	repo      user.Repository
	cache     TokenStore
	jwtConfig config.JWTConfig
}

type TokenResponse struct {
	AccessToken  string `json:"access_token"`
	RefreshToken string `json:"refresh_token"`
	ExpiresIn    int64  `json:"expires_in"`
}

type RegisterRequest struct {
	FullName string `json:"full_name" binding:"required,min=3"`
	Email    string `json:"email" binding:"required,email"`
	Username string `json:"username" binding:"required,min=3"`
	Password string `json:"password" binding:"required,min=6"`
}

type LoginRequest struct {
	Identifier string `json:"identifier" binding:"required"`
	Password   string `json:"password" binding:"required"`
}

type RefreshRequest struct {
	RefreshToken string `json:"refresh_token" binding:"required"`
}

type LogoutRequest struct {
	RefreshToken string `json:"refresh_token" binding:"required"`
}

func NewService(repo user.Repository, cache TokenStore, jwtCfg config.JWTConfig) *Service {
	return &Service{repo: repo, cache: cache, jwtConfig: jwtCfg}
}

func (s *Service) Register(ctx context.Context, req RegisterRequest) (user.User, error) {
	createReq := user.CreateRequest{
		FullName: req.FullName,
		Email:    req.Email,
		Username: req.Username,
		Password: req.Password,
	}
	userService := user.NewService(s.repo)
	return userService.Create(ctx, createReq, user.RoleUser)
}

func (s *Service) Login(ctx context.Context, req LoginRequest) (TokenResponse, error) {
	entity, err := s.repo.GetByEmailOrUsername(ctx, req.Identifier)
	if err != nil {
		if errors.Is(err, gorm.ErrRecordNotFound) {
			return TokenResponse{}, ErrInvalidCredentials
		}
		return TokenResponse{}, err
	}
	if err := utils.VerifyPassword(entity.PasswordHash, req.Password); err != nil {
		return TokenResponse{}, ErrInvalidCredentials
	}
	return s.issueTokenPair(ctx, entity.ID, string(entity.Role))
}

func (s *Service) Refresh(ctx context.Context, refreshToken string) (TokenResponse, error) {
	claims, err := utils.ParseToken(refreshToken, s.jwtConfig.Secret)
	if err != nil || claims.TokenType != s.jwtConfig.RefreshTokenLabel {
		return TokenResponse{}, ErrInvalidToken
	}
	key := refreshKey(claims.ID)
	exists, err := s.cache.Exists(ctx, key)
	if err != nil {
		return TokenResponse{}, err
	}
	if exists == 0 {
		return TokenResponse{}, ErrInvalidToken
	}

	userID, err := uuid.Parse(claims.Subject)
	if err != nil {
		return TokenResponse{}, ErrInvalidToken
	}

	role := claims.Role
	return s.issueTokenPair(ctx, userID, role)
}

func (s *Service) Logout(ctx context.Context, refreshToken string) error {
	claims, err := utils.ParseToken(refreshToken, s.jwtConfig.Secret)
	if err != nil {
		return ErrInvalidToken
	}
	return s.cache.Del(ctx, refreshKey(claims.ID))
}

func (s *Service) issueTokenPair(ctx context.Context, userID uuid.UUID, role string) (TokenResponse, error) {
	accessToken, _, err := utils.GenerateToken(userID, role, s.jwtConfig.Secret, s.jwtConfig.AccessTokenTTL, s.jwtConfig.AccessTokenLabel)
	if err != nil {
		return TokenResponse{}, err
	}
	refreshToken, refreshID, err := utils.GenerateToken(userID, role, s.jwtConfig.Secret, s.jwtConfig.RefreshTokenTTL, s.jwtConfig.RefreshTokenLabel)
	if err != nil {
		return TokenResponse{}, err
	}

	key := refreshKey(refreshID)
	if err := s.cache.Set(ctx, key, userID.String(), s.jwtConfig.RefreshTokenTTL); err != nil {
		return TokenResponse{}, err
	}

	expiresIn := int64(s.jwtConfig.AccessTokenTTL.Seconds())
	return TokenResponse{AccessToken: accessToken, RefreshToken: refreshToken, ExpiresIn: expiresIn}, nil
}

func refreshKey(id string) string {
	return fmt.Sprintf("refresh:%s", id)
}
