package handler

import (
	"auth-service/internal/domain"
	"auth-service/internal/service/auth"
	"auth-service/internal/service/oauth"
	"auth-service/internal/service/wechat"
	"auth-service/internal/storage"
	"log/slog"
	"net/http"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
)

type WeChatHandler struct {
	wechatService *wechat.WeChatService
	oauthService  *oauth.OAuthService
	jwtService    *auth.JWTService
	db            storage.Storage
}

func NewWeChatHandler(
	wechatService *wechat.WeChatService,
	oauthService *oauth.OAuthService,
	jwtService *auth.JWTService,
	db storage.Storage,
) *WeChatHandler {
	return &WeChatHandler{
		wechatService: wechatService,
		oauthService:  oauthService,
		jwtService:    jwtService,
		db:            db,
	}
}

// GetQRCode handles the request to start a WeChat QR code login.
func (h *WeChatHandler) GetQRCode(c *gin.Context) {
	sessionID := uuid.New().String()

	ticket, err := h.wechatService.GetQRCodeTicket(c.Request.Context(), sessionID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to get QR code ticket from WeChat"})
		return
	}

	session := &domain.WeChatLoginSession{
		SessionID: sessionID,
		Ticket:    ticket.Ticket,
		Status:    "PENDING",
		CreatedAt: time.Now(),
	}

	if err := h.db.StoreWeChatSession(c.Request.Context(), session); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to create login session"})
		return
	}

	c.JSON(http.StatusOK, gin.H{
		"session_id": session.SessionID,
		"qr_url":     ticket.URL, // In a real app, frontend might construct this from the ticket
	})
}

// GetStatus handles the frontend polling to check the login status.
func (h *WeChatHandler) GetStatus(c *gin.Context) {
	sessionID := c.Query("session_id")
	if sessionID == "" {
		c.JSON(http.StatusBadRequest, gin.H{"error": "session_id is required"})
		return
	}

	session, err := h.db.FindWeChatSession(c.Request.Context(), sessionID)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "Session not found or expired"})
		return
	}

	response := gin.H{"status": session.Status}
	if session.Status == "AUTHORIZED" {
		response["token"] = session.Token
	}

	c.JSON(http.StatusOK, response)
}

type WeChatCallbackRequest struct {
	Ticket string `json:"ticket"`
	Code   string `json:"code"`
	// In a real scenario, WeChat sends more, but we'll simulate with these.
}

// Callback handles the callback from WeChat's servers.
func (h *WeChatHandler) Callback(c *gin.Context) {
	var req WeChatCallbackRequest
	// In reality, this would be an XML payload from WeChat, not JSON.
	// We use JSON for a simplified, testable simulation.
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid callback body"})
		return
	}

	// 1. Find the session by the ticket from the callback
	session, err := h.db.FindWeChatSessionByTicket(c.Request.Context(), req.Ticket)
	if err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "Session for ticket not found"})
		return
	}

	// 2. Mock fetching user info from WeChat using the `code`
	// In a real app, you'd exchange the code for an access token, then get user info.
	wechatUserID := "mock_wechat_user_" + req.Code
	wechatEmail := wechatUserID + "@example.com"

	// 3. Find or create the user in our system
	user, err := h.oauthService.FindOrCreateUserFromProvider(c.Request.Context(), "wechat", wechatUserID, wechatEmail)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to process user"})
		return
	}

	// 4. Generate our app's JWT
	token, err := h.jwtService.GenerateToken(user.ID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to generate token"})
		return
	}

	// 5. Update the session state
	session.Status = "AUTHORIZED"
	session.UserID = user.ID
	session.Token = token
	if err := h.db.UpdateWeChatSession(c.Request.Context(), session); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to update session"})
		return
	}

	slog.Info("wechat callback processed successfully", "session_id", session.SessionID, "user_id", user.ID)
	c.JSON(http.StatusOK, gin.H{"message": "Callback received and processed"})
}
