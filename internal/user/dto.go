package user

type CreateRequest struct {
	FullName string `json:"full_name" binding:"required,min=3"`
	Email    string `json:"email" binding:"required,email"`
	Username string `json:"username" binding:"required,min=3"`
	Password string `json:"password" binding:"required,min=6"`
	Role     string `json:"role" binding:"omitempty,oneof=ADMIN USER"`
}

type UpdateRequest struct {
	FullName string `json:"full_name" binding:"omitempty,min=3"`
	Email    string `json:"email" binding:"omitempty,email"`
	Username string `json:"username" binding:"omitempty,min=3"`
	Password string `json:"password" binding:"omitempty,min=6"`
	Role     string `json:"role" binding:"omitempty,oneof=ADMIN USER"`
	IsActive *bool  `json:"is_active"`
}

type Response struct {
	ID       string `json:"id"`
	FullName string `json:"full_name"`
	Email    string `json:"email"`
	Username string `json:"username"`
	Role     string `json:"role"`
	IsActive bool   `json:"is_active"`
	CreatedAt string `json:"created_at"`
	UpdatedAt string `json:"updated_at"`
}

func ToResponse(u User) Response {
	return Response{
		ID:        u.ID.String(),
		FullName:  u.FullName,
		Email:     u.Email,
		Username:  u.Username,
		Role:      string(u.Role),
		IsActive:  u.IsActive,
		CreatedAt: u.CreatedAt.Format("2006-01-02T15:04:05Z07:00"),
		UpdatedAt: u.UpdatedAt.Format("2006-01-02T15:04:05Z07:00"),
	}
}
