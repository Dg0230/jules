package middleware

import (
	"auth-service/internal/config"
	"auth-service/internal/service/auth"
	"fmt"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
	"github.com/stretchr/testify/assert"
)

func TestAuthMiddleware(t *testing.T) {
	// Setup
	cfg := config.Load()
	jwtService := auth.NewJWTService(cfg)
	testUserID := uuid.New()

	gin.SetMode(gin.TestMode)
	router := gin.New()
	router.Use(AuthMiddleware(jwtService))
	router.GET("/protected", func(c *gin.Context) {
		contextUserID, exists := c.Get(CtxUserID)
		if !exists {
			c.JSON(http.StatusInternalServerError, gin.H{"error": "User ID not found in context"})
			return
		}
		c.JSON(http.StatusOK, gin.H{"user_id": contextUserID})
	})

	t.Run("No Authorization Header", func(t *testing.T) {
		req, _ := http.NewRequest(http.MethodGet, "/protected", nil)
		w := httptest.NewRecorder()
		router.ServeHTTP(w, req)

		assert.Equal(t, http.StatusUnauthorized, w.Code)
	})

	t.Run("Invalid Header Format", func(t *testing.T) {
		req, _ := http.NewRequest(http.MethodGet, "/protected", nil)
		req.Header.Set("Authorization", "invalid-token")
		w := httptest.NewRecorder()
		router.ServeHTTP(w, req)

		assert.Equal(t, http.StatusUnauthorized, w.Code)
	})

	t.Run("Invalid Token", func(t *testing.T) {
		req, _ := http.NewRequest(http.MethodGet, "/protected", nil)
		req.Header.Set("Authorization", "Bearer faketoken123")
		w := httptest.NewRecorder()
		router.ServeHTTP(w, req)

		assert.Equal(t, http.StatusUnauthorized, w.Code)
	})

	t.Run("Valid Token", func(t *testing.T) {
		validToken, err := jwtService.GenerateToken(testUserID)
		assert.NoError(t, err)

		req, _ := http.NewRequest(http.MethodGet, "/protected", nil)
		req.Header.Set("Authorization", fmt.Sprintf("Bearer %s", validToken))
		w := httptest.NewRecorder()
		router.ServeHTTP(w, req)

		assert.Equal(t, http.StatusOK, w.Code)
		assert.JSONEq(t, fmt.Sprintf(`{"user_id": "%s"}`, testUserID.String()), w.Body.String())
	})
}
