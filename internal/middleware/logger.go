package middleware

import (
	"log/slog"
	"time"

	"github.com/gin-gonic/gin"
	"github.com/google/uuid"
)

const CtxRequestID = "requestID"

// StructuredLogger returns a Gin middleware that logs requests using slog.
func StructuredLogger() gin.HandlerFunc {
	return func(c *gin.Context) {
		start := time.Now()

		// Create a request ID
		requestID := uuid.New().String()
		c.Set(CtxRequestID, requestID)

		// Create a logger with context
		logger := slog.With(
			slog.String("request_id", requestID),
		)

		// Process request
		c.Next()

		// Log request details
		logger.Info("incoming request",
			slog.String("method", c.Request.Method),
			slog.String("path", c.Request.URL.Path),
			slog.Int("status", c.Writer.Status()),
			slog.Duration("latency", time.Since(start)),
			slog.String("ip", c.ClientIP()),
			slog.String("user_agent", c.Request.UserAgent()),
		)
	}
}
