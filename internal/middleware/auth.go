package middleware

import (
	"auth-service/internal/service/auth"
	"log/slog"
	"net/http"
	"strings"

	"github.com/gin-gonic/gin"
)

const CtxUserID = "userID"

// AuthMiddleware creates a Gin middleware for JWT authentication.
func AuthMiddleware(jwtService *auth.JWTService) gin.HandlerFunc {
	return func(c *gin.Context) {
		authHeader := c.GetHeader("Authorization")
		if authHeader == "" {
			slog.Warn("Authorization header is missing")
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"error": "Authorization header is required"})
			return
		}

		headerParts := strings.Split(authHeader, " ")
		if len(headerParts) != 2 || strings.ToLower(headerParts[0]) != "bearer" {
			slog.Warn("Invalid Authorization header format")
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"error": "Invalid Authorization header format"})
			return
		}

		tokenString := headerParts[1]
		claims, err := jwtService.ValidateToken(tokenString)
		if err != nil {
			slog.Warn("Invalid token", "error", err)
			c.AbortWithStatusJSON(http.StatusUnauthorized, gin.H{"error": "Invalid or expired token"})
			return
		}

		// Set user ID in context for downstream handlers
		c.Set(CtxUserID, claims.UserID)
		c.Next()
	}
}
