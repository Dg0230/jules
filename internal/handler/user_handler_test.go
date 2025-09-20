package handler

import (
	"auth-service/internal/config"
	"auth-service/internal/service/auth"
	"auth-service/internal/service/sms"
	"auth-service/internal/service/user"
	"auth-service/internal/storage/memory"
	"bytes"
	"encoding/json"
	"fmt"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

// setupFullTestServer creates a test server with all services wired up, including a mock sms service.
func setupFullTestServerWithMocks() (*gin.Engine, *auth.JWTService, *sms.MockSMSService) {
	gin.SetMode(gin.TestMode)
	router := gin.New()

	// Dependencies
	cfg := config.Load()
	db := memory.New()
	jwtService := auth.NewJWTService(cfg)
	mockSmsService := new(sms.MockSMSService)
	userService := user.NewUserService(db, mockSmsService)
	userHandler := NewUserHandler(userService, jwtService)

	// Routes
	api := router.Group("/api")
	{
		api.POST("/register", userHandler.Register)
		api.POST("/login/password", userHandler.Login)
		api.POST("/login/otp/request", userHandler.RequestOTP)
		api.POST("/login/otp/verify", userHandler.VerifyOTP)
	}
	return router, jwtService, mockSmsService
}

func TestRegisterAndLoginFlow(t *testing.T) {
	router, jwtService, _ := setupFullTestServerWithMocks()

	// --- Test Registration ---
	t.Run("Successful Registration", func(t *testing.T) {
		creds := map[string]string{
			"email":    "test@example.com",
			"password": "password123",
		}
		body, _ := json.Marshal(creds)
		req, _ := http.NewRequest(http.MethodPost, "/api/register", bytes.NewBuffer(body))
		req.Header.Set("Content-Type", "application/json")
		w := httptest.NewRecorder()
		router.ServeHTTP(w, req)

		assert.Equal(t, http.StatusCreated, w.Code)
	})

	t.Run("Duplicate Registration", func(t *testing.T) {
		creds := map[string]string{
			"email":    "test@example.com",
			"password": "password123",
		}
		body, _ := json.Marshal(creds)
		req, _ := http.NewRequest(http.MethodPost, "/api/register", bytes.NewBuffer(body))
		req.Header.Set("Content-Type", "application/json")
		w := httptest.NewRecorder()
		router.ServeHTTP(w, req)

		assert.Equal(t, http.StatusConflict, w.Code)
	})

	// --- Test Login ---
	t.Run("Login with wrong password", func(t *testing.T) {
		creds := map[string]string{
			"email":    "test@example.com",
			"password": "wrongpassword",
		}
		body, _ := json.Marshal(creds)
		req, _ := http.NewRequest(http.MethodPost, "/api/login/password", bytes.NewBuffer(body))
		req.Header.Set("Content-Type", "application/json")
		w := httptest.NewRecorder()
		router.ServeHTTP(w, req)

		assert.Equal(t, http.StatusUnauthorized, w.Code)
	})

	t.Run("Successful Login", func(t *testing.T) {
		creds := map[string]string{
			"email":    "test@example.com",
			"password": "password123",
		}
		body, _ := json.Marshal(creds)
		req, _ := http.NewRequest(http.MethodPost, "/api/login/password", bytes.NewBuffer(body))
		req.Header.Set("Content-Type", "application/json")
		w := httptest.NewRecorder()
		router.ServeHTTP(w, req)

		assert.Equal(t, http.StatusOK, w.Code)

		// Check the token
		var respBody map[string]string
		err := json.Unmarshal(w.Body.Bytes(), &respBody)
		require.NoError(t, err)
		tokenString, ok := respBody["token"]
		require.True(t, ok, "Response should contain a token")
		require.NotEmpty(t, tokenString, "Token should not be empty")

		// Validate the token
		claims, err := jwtService.ValidateToken(tokenString)
		require.NoError(t, err)
		assert.NotEmpty(t, claims.UserID)
	})
}

func TestOTPFlow(t *testing.T) {
	router, jwtService, _ := setupFullTestServerWithMocks()
	email := "otp-user@example.com"

	// 1. Request OTP
	var otpCode string
	t.Run("Request OTP", func(t *testing.T) {
		reqBody := fmt.Sprintf(`{"email": "%s"}`, email)
		req, _ := http.NewRequest(http.MethodPost, "/api/login/otp/request", bytes.NewBufferString(reqBody))
		req.Header.Set("Content-Type", "application/json")
		w := httptest.NewRecorder()
		router.ServeHTTP(w, req)

		assert.Equal(t, http.StatusOK, w.Code)
		var respBody map[string]string
		err := json.Unmarshal(w.Body.Bytes(), &respBody)
		require.NoError(t, err)
		otpCode = respBody["otp_for_testing"]
		require.NotEmpty(t, otpCode, "OTP code should be returned for testing")
	})

	// 2. Verify with wrong OTP
	t.Run("Verify with wrong OTP", func(t *testing.T) {
		reqBody := fmt.Sprintf(`{"email": "%s", "code": "000000"}`, email)
		req, _ := http.NewRequest(http.MethodPost, "/api/login/otp/verify", bytes.NewBufferString(reqBody))
		req.Header.Set("Content-Type", "application/json")
		w := httptest.NewRecorder()
		router.ServeHTTP(w, req)

		assert.Equal(t, http.StatusUnauthorized, w.Code)
	})

	// 3. Verify with correct OTP
	t.Run("Verify with correct OTP", func(t *testing.T) {
		reqBody := fmt.Sprintf(`{"email": "%s", "code": "%s"}`, email, otpCode)
		req, _ := http.NewRequest(http.MethodPost, "/api/login/otp/verify", bytes.NewBufferString(reqBody))
		req.Header.Set("Content-Type", "application/json")
		w := httptest.NewRecorder()
		router.ServeHTTP(w, req)

		assert.Equal(t, http.StatusOK, w.Code)
		var respBody map[string]string
		err := json.Unmarshal(w.Body.Bytes(), &respBody)
		require.NoError(t, err)
		tokenString, ok := respBody["token"]
		require.True(t, ok)

		// Validate token
		claims, err := jwtService.ValidateToken(tokenString)
		require.NoError(t, err)
		assert.NotEmpty(t, claims.UserID)
	})
}
