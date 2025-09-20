package oauth

import (
	"auth-service/internal/storage/memory"
	"context"
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestFindOrCreateUserFromProvider(t *testing.T) {
	db := memory.New()
	service := NewOAuthService(db)
	ctx := context.Background()

	provider := "google"
	providerID := "12345"
	email := "google-user@example.com"

	// 1. First time login for this user
	t.Run("Create new user", func(t *testing.T) {
		user, err := service.FindOrCreateUserFromProvider(ctx, provider, providerID, email)
		require.NoError(t, err)
		assert.NotNil(t, user)

		// Verify identity was created
		identity, err := db.FindIdentityByProvider(ctx, provider, providerID)
		require.NoError(t, err)
		assert.Equal(t, user.ID, identity.UserID)
	})

	// 2. Second time login for the same user
	t.Run("Find existing user", func(t *testing.T) {
		user, err := service.FindOrCreateUserFromProvider(ctx, provider, providerID, email)
		require.NoError(t, err)
		assert.NotNil(t, user)

		// Verify no new user was created by checking the user ID is the same
		identity, _ := db.FindIdentityByProvider(ctx, provider, providerID)
		assert.Equal(t, identity.UserID, user.ID)
	})
}
