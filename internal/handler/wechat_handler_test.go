package handler

import (
	"auth-service/internal/config"
	"auth-service/internal/service/auth"
	"auth-service/internal/service/oauth"
	"auth-service/internal/service/wechat"
	"auth-service/internal/storage/memory"
	"encoding/json"
	"fmt"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

// setupWeChatTestServer creates a test server with all services needed for WeChat tests.
func setupWeChatTestServer() *gin.Engine {
	gin.SetMode(gin.TestMode)
	router := gin.New()

	// Dependencies
	cfg := config.Load()
	db := memory.New()
	jwtService := auth.NewJWTService(cfg)
	oauthService := oauth.NewOAuthService(db, cfg)
	wechatService := wechat.NewWeChatService(cfg)
	wechatHandler := NewWeChatHandler(wechatService, oauthService, jwtService, db)

	// Routes
	api := router.Group("/api")
	{
		api.GET("/login/wechat/qrcode", wechatHandler.GetQRCode)
		api.GET("/login/wechat/status", wechatHandler.GetStatus)
		api.POST("/oauth/wechat/callback", wechatHandler.Callback)
	}
	return router
}

func TestWeChatQRFlow_GetQRCodeAndInitialStatus(t *testing.T) {
	router := setupWeChatTestServer()

	// 1. Get QR Code and Session ID
	var sessionID string
	var qrURL string

	req, _ := http.NewRequest(http.MethodGet, "/api/login/wechat/qrcode", nil)
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	assert.Equal(t, http.StatusOK, w.Code)
	var respBody map[string]string
	err := json.Unmarshal(w.Body.Bytes(), &respBody)
	require.NoError(t, err)
	sessionID = respBody["session_id"]
	qrURL = respBody["qr_url"]
	require.NotEmpty(t, sessionID)
	require.NotEmpty(t, qrURL)

	// 2. Poll status immediately, should be PENDING
	req, _ = http.NewRequest(http.MethodGet, fmt.Sprintf("/api/login/wechat/status?session_id=%s", sessionID), nil)
	w = httptest.NewRecorder()
	router.ServeHTTP(w, req)

	assert.Equal(t, http.StatusOK, w.Code)
	var statusRespBody map[string]string
	err = json.Unmarshal(w.Body.Bytes(), &statusRespBody)
	require.NoError(t, err)
	assert.Equal(t, "PENDING", statusRespBody["status"])
}
