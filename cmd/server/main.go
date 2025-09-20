package main

import (
	"log/slog"
	"net/http"
	"os"

	"auth-service/internal/config"
	"auth-service/internal/handler"
	"auth-service/internal/middleware"
	"auth-service/internal/service/auth"
	"auth-service/internal/service/oauth"
	"auth-service/internal/service/sms"
	"auth-service/internal/service/user"
	"auth-service/internal/service/wechat"
	"auth-service/internal/storage/memory"
	"github.com/gin-gonic/gin"
)

func main() {
	// 1. Init structured logger and config
	logger := slog.New(slog.NewJSONHandler(os.Stdout, nil))
	slog.SetDefault(logger)
	cfg := config.Load()

	// 2. Init storage
	db := memory.New()

	// 3. Init services
	jwtService := auth.NewJWTService(cfg)
	smsService, err := sms.NewAliyunSMSService(cfg)
	if err != nil {
		slog.Warn("failed to create Aliyun SMS service, phone login will be unavailable", "error", err)
	}
	userService := user.NewUserService(db, smsService)
	oauthService := oauth.NewOAuthService(db)
	wechatService := wechat.NewWeChatService(cfg)

	// 4. Init handlers
	userHandler := handler.NewUserHandler(userService, jwtService)
	oauthHandler := handler.NewOAuthHandler(cfg, oauthService, jwtService)
	wechatHandler := handler.NewWeChatHandler(wechatService, oauthService, jwtService, db)

	// 4. Init Gin router
	router := gin.New()
	router.Use(middleware.StructuredLogger())
	router.Use(gin.Recovery())

	// 5. Setup routes
	api := router.Group("/api")
	{
		// User/Pass & OTP
		api.POST("/register", userHandler.Register)
		api.POST("/login/password", userHandler.Login)
		api.POST("/login/otp/request", userHandler.RequestOTP)
		api.POST("/login/otp/verify", userHandler.VerifyOTP)

		// OAuth
		api.GET("/login/google", oauthHandler.GoogleLogin)
		api.GET("/oauth/google/callback", oauthHandler.GoogleCallback)
		api.GET("/login/discord", oauthHandler.DiscordLogin)
		api.GET("/oauth/discord/callback", oauthHandler.DiscordCallback)

		// WeChat QR Code
		api.GET("/login/wechat/qrcode", wechatHandler.GetQRCode)
		api.GET("/login/wechat/status", wechatHandler.GetStatus)
		api.POST("/oauth/wechat/callback", wechatHandler.Callback)
	}

	// Health check endpoint
	router.GET("/ping", func(c *gin.Context) {
		requestLogger := slog.With("request_id", c.GetString(middleware.CtxRequestID))
		requestLogger.Info("ping request received")
		c.JSON(http.StatusOK, gin.H{
			"message": "pong",
		})
	})

	slog.Info("Starting server", "port", cfg.ServerPort)
	// 6. Start the server
	if err := router.Run(":" + cfg.ServerPort); err != nil {
		slog.Error("failed to start server", "error", err)
		os.Exit(1)
	}
}
