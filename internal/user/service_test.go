package user

import (
	"context"
	"errors"
	"testing"

	"github.com/google/uuid"
	"github.com/stretchr/testify/require"
)

type mockUserRepo struct {
	created User
	list    []User
	get     User
	update  User
	err     error
}

func (m *mockUserRepo) Create(ctx context.Context, user *User) error {
	m.created = *user
	return m.err
}

func (m *mockUserRepo) GetByID(ctx context.Context, id uuid.UUID) (User, error) {
	if m.err != nil {
		return User{}, m.err
	}
	return m.get, nil
}

func (m *mockUserRepo) GetByEmailOrUsername(ctx context.Context, identifier string) (User, error) {
	return User{}, errors.New("not implemented")
}

func (m *mockUserRepo) List(ctx context.Context) ([]User, error) {
	if m.err != nil {
		return nil, m.err
	}
	return m.list, nil
}

func (m *mockUserRepo) Update(ctx context.Context, user *User) error {
	m.update = *user
	return m.err
}

func (m *mockUserRepo) Delete(ctx context.Context, id uuid.UUID) error {
	return m.err
}

func TestUserService_CreateUpdate(t *testing.T) {
	repo := &mockUserRepo{}
	svc := NewService(repo)

	created, err := svc.Create(context.Background(), CreateRequest{
		FullName: "Jane Doe",
		Email:    "jane@example.com",
		Username: "jane",
		Password: "secret123",
	}, RoleUser)
	require.NoError(t, err)
	require.Equal(t, "Jane Doe", created.FullName)
	require.Equal(t, RoleUser, created.Role)

	updateReq := UpdateRequest{FullName: "Jane Updated"}
	updated, err := svc.Update(context.Background(), created, updateReq, false)
	require.NoError(t, err)
	require.Equal(t, "Jane Updated", updated.FullName)
}

func TestUserService_GetNotFound(t *testing.T) {
	repo := &mockUserRepo{err: ErrUserNotFound}
	svc := NewService(repo)

	_, err := svc.GetByID(context.Background(), uuid.New())
	require.Error(t, err)
}
