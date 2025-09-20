package storage

import (
	"auth-service/internal/domain"
	"context"

	"github.com/google/uuid"
)

// Storage defines the interface for database operations.
// It allows for multiple implementations (e.g., in-memory, PostgreSQL).
type Storage interface {
	// User methods
	CreateUser(ctx context.Context, user *domain.User) error
	FindUserByID(ctx context.Context, id uuid.UUID) (*domain.User, error)

	// Identity methods
	CreateIdentity(ctx context.Context, identity *domain.Identity) error
	FindIdentityByProvider(ctx context.Context, provider, providerID string) (*domain.Identity, error)

	// OTP methods
	StoreOTP(ctx context.Context, otp *domain.OTP) error
	FindOTP(ctx context.Context, email string) (*domain.OTP, error)
	DeleteOTP(ctx context.Context, email string) error

	// WeChat Session methods
	StoreWeChatSession(ctx context.Context, session *domain.WeChatLoginSession) error
	FindWeChatSession(ctx context.Context, sessionID string) (*domain.WeChatLoginSession, error)
	FindWeChatSessionByTicket(ctx context.Context, ticket string) (*domain.WeChatLoginSession, error)
	UpdateWeChatSession(ctx context.Context, session *domain.WeChatLoginSession) error
}

// Common errors
var (
	ErrUserNotFound     = NewStorageError("user not found")
	ErrIdentityNotFound = NewStorageError("identity not found")
	ErrDuplicateRecord  = NewStorageError("duplicate record")
)

// StorageError is a custom error type for storage operations.
type StorageError struct {
	message string
}

func NewStorageError(message string) *StorageError {
	return &StorageError{message: message}
}

func (e *StorageError) Error() string {
	return e.message
}
