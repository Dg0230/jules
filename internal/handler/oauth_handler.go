package handler

import (
	"auth-service/internal/service/auth"
	"auth-service/internal/service/oauth"
	"context"
	"encoding/json"
	"io"
	"net/http"

	"github.com/gin-gonic/gin"
)

type OAuthHandler struct {
	oauthService *oauth.OAuthService
	jwtService   *auth.JWTService
}

type GoogleUserInfo struct {
	ID    string `json:"id"`
	Email string `json:"email"`
}

type DiscordUserInfo struct {
	ID    string `json:"id"`
	Email string `json:"email"`
}

func NewOAuthHandler(oauthService *oauth.OAuthService, jwtService *auth.JWTService) *OAuthHandler {
	return &OAuthHandler{
		oauthService: oauthService,
		jwtService:   jwtService,
	}
}

// GoogleLogin godoc
// @Summary      Login with Google
// @Description  Redirects the user to Google's consent page to initiate OAuth2 login.
// @Tags         oauth
// @Success      307
// @Router       /login/google [get]
func (h *OAuthHandler) GoogleLogin(c *gin.Context) {
	state, err := oauth.GenerateOauthState()
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to generate state"})
		return
	}
	c.SetCookie("oauthstate", state, 3600, "/", "", false, true)
	url := h.oauthService.GoogleOAuthConfig.AuthCodeURL(state)
	c.Redirect(http.StatusTemporaryRedirect, url)
}

// GoogleCallback godoc
// @Summary      Google OAuth2 Callback
// @Description  Handles the callback from Google after user authorization.
// @Tags         oauth
// @Produce      json
// @Success      200  {object}  map[string]string  "Returns a JWT token"
// @Failure      400  {object}  map[string]string
// @Failure      500  {object}  map[string]string
// @Router       /oauth/google/callback [get]
func (h *OAuthHandler) GoogleCallback(c *gin.Context) {
	oauthState, _ := c.Cookie("oauthstate")
	if c.Query("state") != oauthState {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid oauth state"})
		return
	}

	code := c.Query("code")
	token, err := h.oauthService.GoogleOAuthConfig.Exchange(context.Background(), code)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to exchange token"})
		return
	}

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

	user, err := h.oauthService.FindOrCreateUserFromProvider(c.Request.Context(), "google", userInfo.ID, userInfo.Email)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to process user from provider"})
		return
	}

	appToken, err := h.jwtService.GenerateToken(user.ID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to generate application token"})
		return
	}
	c.JSON(http.StatusOK, gin.H{"token": appToken})
}

// DiscordLogin godoc
// @Summary      Login with Discord
// @Description  Redirects the user to Discord's consent page to initiate OAuth2 login.
// @Tags         oauth
// @Success      307
// @Router       /login/discord [get]
func (h *OAuthHandler) DiscordLogin(c *gin.Context) {
	state, err := oauth.GenerateOauthState()
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to generate state"})
		return
	}
	c.SetCookie("oauthstate", state, 3600, "/", "", false, true)
	url := h.oauthService.DiscordOAuthConfig.AuthCodeURL(state)
	c.Redirect(http.StatusTemporaryRedirect, url)
}

// DiscordCallback godoc
// @Summary      Discord OAuth2 Callback
// @Description  Handles the callback from Discord after user authorization.
// @Tags         oauth
// @Produce      json
// @Success      200  {object}  map[string]string  "Returns a JWT token"
// @Failure      400  {object}  map[string]string
// @Failure      500  {object}  map[string]string
// @Router       /oauth/discord/callback [get]
func (h *OAuthHandler) DiscordCallback(c *gin.Context) {
	oauthState, _ := c.Cookie("oauthstate")
	if c.Query("state") != oauthState {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid oauth state"})
		return
	}

	code := c.Query("code")
	token, err := h.oauthService.DiscordOAuthConfig.Exchange(context.Background(), code)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to exchange token"})
		return
	}

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

	user, err := h.oauthService.FindOrCreateUserFromProvider(c.Request.Context(), "discord", userInfo.ID, userInfo.Email)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to process user from provider"})
		return
	}

	appToken, err := h.jwtService.GenerateToken(user.ID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to generate application token"})
		return
	}
	c.JSON(http.StatusOK, gin.H{"token": appToken})
}
