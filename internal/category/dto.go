package category

type CreateRequest struct {
	Name        string  `json:"name" binding:"required,min=3"`
	Slug        string  `json:"slug" binding:"omitempty"`
	Description *string `json:"description"`
	IsActive    *bool   `json:"is_active"`
}

type UpdateRequest struct {
	Name        string  `json:"name" binding:"omitempty,min=3"`
	Slug        string  `json:"slug" binding:"omitempty"`
	Description *string `json:"description"`
	IsActive    *bool   `json:"is_active"`
}

type Response struct {
	ID          string  `json:"id"`
	Name        string  `json:"name"`
	Slug        string  `json:"slug"`
	Description *string `json:"description"`
	IsActive    bool    `json:"is_active"`
	CreatedAt   string  `json:"created_at"`
	UpdatedAt   string  `json:"updated_at"`
}

func ToResponse(entity Category) Response {
	return Response{
		ID:          entity.ID.String(),
		Name:        entity.Name,
		Slug:        entity.Slug,
		Description: entity.Description,
		IsActive:    entity.IsActive,
		CreatedAt:   entity.CreatedAt.Format("2006-01-02T15:04:05Z07:00"),
		UpdatedAt:   entity.UpdatedAt.Format("2006-01-02T15:04:05Z07:00"),
	}
}
