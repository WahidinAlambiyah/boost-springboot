package role

type CreateRequest struct {
	Name        string `json:"name" binding:"required,min=2"`
	Description string `json:"description"`
}

type UpdateRequest struct {
	Name        string `json:"name" binding:"omitempty,min=2"`
	Description string `json:"description"`
}

type Response struct {
	ID          string `json:"id"`
	Name        string `json:"name"`
	Description string `json:"description"`
	CreatedAt   string `json:"created_at"`
	UpdatedAt   string `json:"updated_at"`
}

func ToResponse(r Role) Response {
	return Response{
		ID:          r.ID.String(),
		Name:        r.Name,
		Description: r.Description,
		CreatedAt:   r.CreatedAt.Format("2006-01-02T15:04:05Z07:00"),
		UpdatedAt:   r.UpdatedAt.Format("2006-01-02T15:04:05Z07:00"),
	}
}
