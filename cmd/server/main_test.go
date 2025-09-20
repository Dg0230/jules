package main

import (
	"auth-service/internal/middleware"
	"bytes"
	"encoding/json"
	"log/slog"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"

	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func setupTestServer(logBuffer *bytes.Buffer) *gin.Engine {
	// 1. Init logger to write to a buffer
	logger := slog.New(slog.NewJSONHandler(logBuffer, nil))
	slog.SetDefault(logger)

	// 2. Init Gin router
	gin.SetMode(gin.TestMode)
	router := gin.New()
	router.Use(middleware.StructuredLogger())
	router.Use(gin.Recovery())

	// 3. Add a simple health check endpoint
	router.GET("/ping", func(c *gin.Context) {
		requestLogger := slog.With("request_id", c.GetString(middleware.CtxRequestID))
		requestLogger.Info("ping request received")
		c.JSON(http.StatusOK, gin.H{
			"message": "pong",
		})
	})
	return router
}

func TestPingEndpoint(t *testing.T) {
	var logBuffer bytes.Buffer
	router := setupTestServer(&logBuffer)

	req, _ := http.NewRequest(http.MethodGet, "/ping", nil)
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	// Test HTTP Response
	assert.Equal(t, http.StatusOK, w.Code)
	expectedBody := `{"message":"pong"}`
	assert.JSONEq(t, expectedBody, w.Body.String())

	// Test Log Output
	logOutput := logBuffer.String()
	require.True(t, strings.Contains(logOutput, "ping request received"), "Log should contain the handler message")
	require.True(t, strings.Contains(logOutput, "incoming request"), "Log should contain the middleware message")

	// Unmarshal to check structured fields
	var log map[string]interface{}
	// We check the second log line, which is from the middleware
	logLines := strings.Split(strings.TrimSpace(logOutput), "\n")
	require.GreaterOrEqual(t, len(logLines), 2, "Should have at least two log lines")
	err := json.Unmarshal([]byte(logLines[1]), &log)
	require.NoError(t, err, "Log output should be valid JSON")

	assert.Equal(t, "INFO", log["level"]) // slog's default level is uppercase
	assert.Equal(t, "/ping", log["path"])
	assert.Equal(t, http.MethodGet, log["method"])
	assert.EqualValues(t, http.StatusOK, log["status"])
	assert.NotEmpty(t, log["request_id"])
}
