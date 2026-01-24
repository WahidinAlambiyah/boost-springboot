package utils

import (
	"time"

	"github.com/golang-jwt/jwt/v5"
	"github.com/google/uuid"
)

type TokenPair struct {
	AccessToken  string
	RefreshToken string
}

type CustomClaims struct {
	TokenType string `json:"token_type"`
	Role      string `json:"role"`
	jwt.RegisteredClaims
}

func GenerateToken(userID uuid.UUID, role string, secret string, ttl time.Duration, tokenType string) (string, string, error) {
	jti := uuid.New().String()
	claims := CustomClaims{
		TokenType: tokenType,
		Role:      role,
		RegisteredClaims: jwt.RegisteredClaims{
			Subject:   userID.String(),
			ExpiresAt: jwt.NewNumericDate(time.Now().Add(ttl)),
			IssuedAt:  jwt.NewNumericDate(time.Now()),
			ID:        jti,
		},
	}

	token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
	signed, err := token.SignedString([]byte(secret))
	if err != nil {
		return "", "", err
	}
	return signed, jti, nil
}

func ParseToken(tokenString string, secret string) (*CustomClaims, error) {
	claims := &CustomClaims{}
	parsed, err := jwt.ParseWithClaims(tokenString, claims, func(token *jwt.Token) (interface{}, error) {
		return []byte(secret), nil
	})
	if err != nil {
		return nil, err
	}
	if !parsed.Valid {
		return nil, jwt.ErrTokenInvalidClaims
	}
	return claims, nil
}
