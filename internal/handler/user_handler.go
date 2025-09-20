package handler

import (
	"auth-service/internal/service/auth"
	"auth-service/internal/service/user"
	"auth-service/internal/storage"
	"errors"
	"net/http"

	"github.com/gin-gonic/gin"
)

type UserHandler struct {
	userService *user.UserService
	jwtService  *auth.JWTService
}

func NewUserHandler(userService *user.UserService, jwtService *auth.JWTService) *UserHandler {
	return &UserHandler{
		userService: userService,
		jwtService:  jwtService,
	}
}

type RegisterRequest struct {
	Email    string `json:"email" binding:"required,email"`
	Password string `json:"password" binding:"required,min=8"`
}

type LoginRequest struct {
	Email    string `json:"email" binding:"required,email"`
	Password string `json:"password" binding:"required"`
}

type OTPRequest struct {
	Identifier string `json:"identifier" binding:"required"`
}

type OTPVerifyRequest struct {
	Identifier string `json:"identifier" binding:"required"`
	Code       string `json:"code" binding:"required,len=6"`
}

// RequestOTP godoc
// @Summary Request an OTP
// @Description Request a one-time password for email or phone number.
// @Tags auth
// @Accept  json
// @Produce  json
// @Param   request body OTPRequest true "Identifier (Email or Phone)"
// @Success 200 {object} map[string]string
// @Failure 400 {object} map[string]string
// @Failure 500 {object} map[string]string
// @Router /login/otp/request [post]
func (h *UserHandler) RequestOTP(c *gin.Context) {
	var req OTPRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid request body"})
		return
	}
	// In a real app, we wouldn't return the code in the response.
	// This is for demonstration/testing purposes.
	code, err := h.userService.RequestOTP(c.Request.Context(), req.Identifier)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to request OTP"})
		return
	}
	c.JSON(http.StatusOK, gin.H{"message": "OTP sent", "otp_for_testing": code})
}

// VerifyOTP godoc
// @Summary Verify an OTP
// @Description Verify a one-time password and receive a JWT upon success.
// @Tags auth
// @Accept  json
// @Produce  json
// @Param   request body OTPVerifyRequest true "Identifier and OTP Code"
// @Success 200 {object} map[string]string
// @Failure 400 {object} map[string]string
// @Failure 401 {object} map[string]string
// @Failure 500 {object} map[string]string
// @Router /login/otp/verify [post]
func (h *UserHandler) VerifyOTP(c *gin.Context) {
	var req OTPVerifyRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid request body"})
		return
	}

	user, err := h.userService.VerifyOTP(c.Request.Context(), req.Identifier, req.Code)
	if err != nil {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "Invalid or expired OTP"})
		return
	}

	token, err := h.jwtService.GenerateToken(user.ID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to generate token"})
		return
	}

	c.JSON(http.StatusOK, gin.H{"token": token})
}

// Login godoc
// @Summary Login with email and password
// @Description Authenticate a user with their email and password.
// @Tags auth
// @Accept  json
// @Produce  json
// @Param   request body LoginRequest true "User Credentials"
// @Success 200 {object} map[string]string
// @Failure 400 {object} map[string]string
// @Failure 401 {object} map[string]string
// @Failure 500 {object} map[string]string
// @Router /login/password [post]
func (h *UserHandler) Login(c *gin.Context) {
	var req LoginRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid request body"})
		return
	}

	user, err := h.userService.LoginWithPassword(c.Request.Context(), req.Email, req.Password)
	if err != nil {
		c.JSON(http.StatusUnauthorized, gin.H{"error": "Invalid credentials"})
		return
	}

	token, err := h.jwtService.GenerateToken(user.ID)
	if err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to generate token"})
		return
	}

	c.JSON(http.StatusOK, gin.H{"token": token})
}

// Register godoc
// @Summary Register a new user
// @Description Create a new user with an email and password.
// @Tags auth
// @Accept  json
// @Produce  json
// @Param   request body RegisterRequest true "New User Details"
// @Success 201 {object} map[string]string
// @Failure 400 {object} map[string]string
// @Failure 409 {object} map[string]string
// @Failure 500 {object} map[string]string
// @Router /register [post]
func (h *UserHandler) Register(c *gin.Context) {
	var req RegisterRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		// The validation is now handled automatically by the binding tags.
		c.JSON(http.StatusBadRequest, gin.H{"error": "Invalid request: " + err.Error()})
		return
	}

	newUser, err := h.userService.RegisterWithPassword(c.Request.Context(), req.Email, req.Password)
	if err != nil {
		if errors.Is(err, storage.ErrDuplicateRecord) {
			c.JSON(http.StatusConflict, gin.H{"error": "User with this email already exists"})
			return
		}
		c.JSON(http.StatusInternalServerError, gin.H{"error": "Failed to register user"})
		return
	}

	c.JSON(http.StatusCreated, gin.H{"id": newUser.ID, "created_at": newUser.CreatedAt})
}
