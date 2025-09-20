package oauth

import (
	"auth-service/internal/domain"
	"auth-service/internal/storage"
	"context"
	"log/slog"
	"time"

	"github.com/google/uuid"
)

// OAuthService handles the business logic for OAuth user processing.
type OAuthService struct {
	db storage.Storage
}

// NewOAuthService creates a new OAuthService.
func NewOAuthService(db storage.Storage) *OAuthService {
	return &OAuthService{db: db}
}

// FindOrCreateUserFromProvider finds an existing user by their provider details,
// or creates a new user and identity if one does not exist.
func (s *OAuthService) FindOrCreateUserFromProvider(ctx context.Context, provider, providerID, email string) (*domain.User, error) {
	// Check if an identity with this provider ID already exists
	identity, err := s.db.FindIdentityByProvider(ctx, provider, providerID)
	if err == nil {
		// Identity found, so the user already exists. Return the user.
		user, userErr := s.db.FindUserByID(ctx, identity.UserID)
		if userErr != nil {
			slog.Error("oauth login failed: could not find user for valid identity", "user_id", identity.UserID, "provider", provider, "error", userErr)
			return nil, userErr
		}
		slog.Info("user login successful", "user_id", user.ID, "provider", provider)
		return user, nil
	}

	if err != storage.ErrIdentityNotFound {
		// A different storage error occurred
		slog.Error("oauth login failed: storage error", "provider", provider, "provider_id", providerID, "error", err)
		return nil, err
	}

	// No identity found for this provider. This is a new login.
	// We need to create a new user and a new identity.
	// In a more complex system, you might first check if a user with the same email
	// exists from a different provider and link the accounts. For now, we'll keep it simple.
	slog.Info("new oauth identity, creating user", "provider", provider, "provider_id", providerID, "email", email)

	newUser := &domain.User{
		ID:        uuid.New(),
		CreatedAt: time.Now(),
		UpdatedAt: time.Now(),
	}
	if err := s.db.CreateUser(ctx, newUser); err != nil {
		return nil, err
	}

	newIdentity := &domain.Identity{
		ID:         uuid.New(),
		UserID:     newUser.ID,
		Provider:   provider,
		ProviderID: providerID,
		// Note: We could store the email in the identity as well if needed,
		// or in a separate user profile table.
		CreatedAt: time.Now(),
		UpdatedAt: time.Now(),
	}
	if err := s.db.CreateIdentity(ctx, newIdentity); err != nil {
		// Attempt to roll back user creation
		return nil, err
	}

	slog.Info("user registration successful", "user_id", newUser.ID, "provider", provider, "email", email)
	return newUser, nil
}
