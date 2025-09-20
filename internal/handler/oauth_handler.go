package handler

import (
	"auth-service/internal/config"
	"auth-service/internal/service/auth"
	"auth-service/internal/service/oauth"
	"context"
	"crypto/rand"
	"encoding/base64"
	"encoding/json"
	"io"
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/ravener/discord-oauth2"
	"golang.org/x/oauth2"
	"golang.org/x/oauth2/google"
)

type OAuthHandler struct {
	cfg           *config.Config
	oauthService  *oauth.OAuthService
	jwtService    *auth.JWTService
	googleOAuthConfig  *oauth2.Config
	discordOAuthConfig *oauth2.Config
}

func NewOAuthHandler(cfg *config.Config, oauthService *oauth.OAuthService, jwtService *auth.JWTService) *OAuthHandler {
	return &OAuthHandler{
		cfg:          cfg,
		oauthService: oauthService,
		jwtService:   jwtService,
		googleOAuthConfig: &oauth2.Config{
			ClientID:     cfg.GoogleClientID,
			ClientSecret: cfg.GoogleClientSecret,
			RedirectURL:  cfg.GoogleRedirectURL,
			Scopes:       []string{"https://www.googleapis.com/auth/userinfo.email", "https://www.googleapis.com/auth/userinfo.profile"},
			Endpoint:     google.Endpoint,
		},
		discordOAuthConfig: &oauth2.Config{
			ClientID:     cfg.DiscordClientID,
			ClientSecret: cfg.DiscordClientSecret,
			RedirectURL:  cfg.DiscordRedirectURL,
			Scopes:       []string{"identify", "email"},
			Endpoint:     discord.Endpoint,
		},
	}
}

// generateOauthState is used to prevent CSRF attacks.
func generateOauthState() (string, error) {
	b := make([]byte, 16)
	_, err := rand.Read(b)
	if err != nil {
		return "", err
	}
	return base64.URLEncoding.EncodeToString(b), nil
}

func (h *OAuthHandler) GoogleLogin(c *gin.Context) {
	state, err := generateOauthState()
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to generate state"})
		return
	}
	// Store the state in a short-lived cookie
	c.SetCookie("oauthstate", state, 3600, "/", "", false, true)

	url := h.googleOAuthConfig.AuthCodeURL(state)
	c.Redirect(http.StatusTemporaryRedirect, url)
}

type GoogleUserInfo struct {
	ID    string `json:"id"`
	Email string `json:"email"`
}

func (h *OAuthHandler) GoogleCallback(c *gin.Context) {
	// Check state cookie
	oauthState, _ := c.Cookie("oauthstate")
	if c.Query("state") != oauthState {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid oauth state"})
		return
	}

	code := c.Query("code")
	token, err := h.googleOAuthConfig.Exchange(context.Background(), code)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to exchange token"})
		return
	}

	// Get user info from Google
	response, err := http.Get("https://www.googleapis.com/oauth2/v2/userinfo?access_token=" + token.AccessToken)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to get user info"})
		return
	}
	defer response.Body.Close()

	contents, err := io.ReadAll(response.Body)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to read user info response"})
		return
	}

	var userInfo GoogleUserInfo
	if err := json.Unmarshal(contents, &userInfo); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to parse user info"})
		return
	}

	// Find or create the user in our system
	user, err := h.oauthService.FindOrCreateUserFromProvider(c.Request.Context(), "google", userInfo.ID, userInfo.Email)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to process user from provider"})
		return
	}

	// Issue our own JWT for the user
	appToken, err := h.jwtService.GenerateToken(user.ID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to generate application token"})
		return
	}

	c.JSON(http.StatusOK, gin.H{"token": appToken})
}

func (h *OAuthHandler) DiscordLogin(c *gin.Context) {
	state, err := generateOauthState()
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to generate state"})
		return
	}
	c.SetCookie("oauthstate", state, 3600, "/", "", false, true)
	url := h.discordOAuthConfig.AuthCodeURL(state)
	c.Redirect(http.StatusTemporaryRedirect, url)
}

type DiscordUserInfo struct {
	ID    string `json:"id"`
	Email string `json:"email"`
}

func (h *OAuthHandler) DiscordCallback(c *gin.Context) {
	oauthState, _ := c.Cookie("oauthstate")
	if c.Query("state") != oauthState {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid oauth state"})
		return
	}

	code := c.Query("code")
	token, err := h.discordOAuthConfig.Exchange(context.Background(), code)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to exchange token"})
		return
	}

	// Get user info from Discord
	req, err := http.NewRequest("GET", "https://discord.com/api/users/@me", nil)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to create request to Discord"})
		return
	}
	req.Header.Set("Authorization", "Bearer "+token.AccessToken)

	client := &http.Client{}
	response, err := client.Do(req)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to get user info"})
		return
	}
	defer response.Body.Close()

	contents, err := io.ReadAll(response.Body)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to read user info response"})
		return
	}

	var userInfo DiscordUserInfo
	if err := json.Unmarshal(contents, &userInfo); err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to parse user info"})
		return
	}

	// Find or create the user in our system
	user, err := h.oauthService.FindOrCreateUserFromProvider(c.Request.Context(), "discord", userInfo.ID, userInfo.Email)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to process user from provider"})
		return
	}

	// Issue our own JWT for the user
	appToken, err := h.jwtService.GenerateToken(user.ID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to generate application token"})
		return
	}

	c.JSON(http.StatusOK, gin.H{"token": appToken})
}
