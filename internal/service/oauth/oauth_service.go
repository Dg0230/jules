package oauth

import (
	"auth-service/internal/config"
	"auth-service/internal/domain"
	"auth-service/internal/storage"
	"context"
	"crypto/rand"
	"encoding/base64"
	"log/slog"
	"time"

	"github.com/google/uuid"
	"github.com/ravener/discord-oauth2"
	"golang.org/x/oauth2"
	"golang.org/x/oauth2/google"
)

// OAuthService handles the business logic for OAuth user processing and config management.
type OAuthService struct {
	db                 storage.Storage
	GoogleOAuthConfig  *oauth2.Config
	DiscordOAuthConfig *oauth2.Config
}

// NewOAuthService creates a new OAuthService.
func NewOAuthService(db storage.Storage, cfg *config.Config) *OAuthService {
	return &OAuthService{
		db: db,
		GoogleOAuthConfig: &oauth2.Config{
			ClientID:     cfg.GoogleClientID,
			ClientSecret: cfg.GoogleClientSecret,
			RedirectURL:  cfg.GoogleRedirectURL,
			Scopes:       []string{"https://www.googleapis.com/auth/userinfo.email", "https://www.googleapis.com/auth/userinfo.profile"},
			Endpoint:     google.Endpoint,
		},
		DiscordOAuthConfig: &oauth2.Config{
			ClientID:     cfg.DiscordClientID,
			ClientSecret: cfg.DiscordClientSecret,
			RedirectURL:  cfg.DiscordRedirectURL,
			Scopes:       []string{discord.ScopeIdentify, discord.ScopeEmail},
			Endpoint:     discord.Endpoint,
		},
	}
}

// GenerateOauthState is used to prevent CSRF attacks.
func GenerateOauthState() (string, error) {
	b := make([]byte, 16)
	_, err := rand.Read(b)
	if err != nil {
		return "", err
	}
	return base64.URLEncoding.EncodeToString(b), nil
}

// FindOrCreateUserFromProvider finds an existing user by their provider details,
// or creates a new user and identity if one does not exist. It also handles account linking.
func (s *OAuthService) FindOrCreateUserFromProvider(ctx context.Context, provider, providerID, email string) (*domain.User, error) {
	// 1. Check if this specific provider identity already exists.
	identity, err := s.db.FindIdentityByProvider(ctx, provider, providerID)
	if err == nil {
		// Identity found, user has logged in with this provider before.
		user, userErr := s.db.FindUserByID(ctx, identity.UserID)
		if userErr != nil {
			slog.Error("oauth login failed: could not find user for valid identity", "user_id", identity.UserID, "provider", provider, "error", userErr)
			return nil, userErr
		}
		slog.Info("user login successful", "user_id", user.ID, "provider", provider)
		return user, nil
	}
	if err != storage.ErrIdentityNotFound {
		slog.Error("oauth login failed: storage error on find identity", "provider", provider, "provider_id", providerID, "error", err)
		return nil, err // A different storage error occurred
	}

	// 2. Identity not found. Check if a user with this email exists from another provider.
	existingUser, err := s.db.FindUserByEmail(ctx, email)
	if err == nil {
		// User with this email exists. Link the new provider to this existing user.
		slog.Info("linking new provider to existing user", "user_id", existingUser.ID, "provider", provider, "email", email)
		newIdentity := &domain.Identity{
			ID:         uuid.New(),
			UserID:     existingUser.ID,
			Provider:   provider,
			ProviderID: providerID,
			CreatedAt:  time.Now(),
			UpdatedAt:  time.Now(),
		}
		if err := s.db.CreateIdentity(ctx, newIdentity); err != nil {
			slog.Error("failed to link new provider to user", "user_id", existingUser.ID, "provider", provider, "error", err)
			return nil, err
		}
		slog.Info("user login successful", "user_id", existingUser.ID, "provider", provider)
		return existingUser, nil
	}
	if err != storage.ErrUserNotFound {
		slog.Error("oauth login failed: storage error on find user by email", "email", email, "error", err)
		return nil, err // A different storage error occurred
	}

	// 3. No user with this email exists. Create a new user and a new identity.
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
		CreatedAt:  time.Now(),
		UpdatedAt:  time.Now(),
	}
	if err := s.db.CreateIdentity(ctx, newIdentity); err != nil {
		return nil, err
	}
	slog.Info("user registration successful", "user_id", newUser.ID, "provider", provider, "email", email)
	return newUser, nil
}
