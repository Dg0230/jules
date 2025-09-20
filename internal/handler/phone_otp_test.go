package handler

import (
	"bytes"
	"encoding/json"
	"fmt"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
	"github.com/stretchr/testify/require"
)

func TestPhoneOTPFlow(t *testing.T) {
	router, jwtService, mockSmsService := setupFullTestServerWithMocks()
	phoneNumber := "13800138000"

	// 1. Request OTP
	var otpCode string
	t.Run("Request Phone OTP", func(t *testing.T) {
		// Set expectation on the mock
		mockSmsService.On("Send", mock.Anything, phoneNumber, mock.AnythingOfType("string")).Return(nil).Once()

		reqBody := fmt.Sprintf(`{"identifier": "%s"}`, phoneNumber)
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

		// Assert that the mock was called
		mockSmsService.AssertExpectations(t)
	})

	// 2. Verify with correct OTP
	t.Run("Verify Phone OTP", func(t *testing.T) {
		reqBody := fmt.Sprintf(`{"identifier": "%s", "code": "%s"}`, phoneNumber, otpCode)
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
