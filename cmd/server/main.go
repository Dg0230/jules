package main

import (
	"auth-service/docs"
	"context"
	"errors"
	"log/slog"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"auth-service/internal/config"
	"auth-service/internal/handler"
	"auth-service/internal/middleware"
	"auth-service/internal/service/auth"
	"auth-service/internal/service/oauth"
	"auth-service/internal/service/user"
	"auth-service/internal/service/wechat"
	"auth-service/internal/storage"
	"auth-service/internal/storage/memory"
	"auth-service/internal/storage/postgres"

	"github.com/getsentry/sentry-go"
	sentrygin "github.com/getsentry/sentry-go/gin"
	"github.com/gin-gonic/gin"
	swaggerFiles "github.com/swaggo/files"
	ginSwagger "github.com/swaggo/gin-swagger"
)

// @title Unified Login Service API
// @version 1.0
// @description This is a unified login service with support for multiple authentication methods.
// @termsOfService http://swagger.io/terms/

// @contact.name API Support
// @contact.url http://www.swagger.io/support
// @contact.email support@swagger.io

// @license.name Apache 2.0
// @license.url http://www.apache.org/licenses/LICENSE-2.0.html

// @host localhost:8080
// @BasePath /api
func main() {
	// 1. Init structured logger and config
	logger := slog.New(slog.NewJSONHandler(os.Stdout, nil))
	slog.SetDefault(logger)
	cfg := config.Load()

	// 2. Init Sentry for error tracking
	if cfg.SentryDSN != "" {
		if err := sentry.Init(sentry.ClientOptions{
			Dsn: cfg.SentryDSN,
			// Set TracesSampleRate to 1.0 to capture 100%
			// of transactions for performance monitoring.
			// We recommend adjusting this value in production.
			TracesSampleRate: 1.0,
		}); err != nil {
			slog.Error("sentry initialization failed", "error", err)
		}
		// Flush buffered events before the program terminates.
		defer sentry.Flush(2 * time.Second)
	}

	// 3. Init storage
	var db storage.Storage
	var pgStore *postgres.PostgresStorage
	var err error

	if cfg.PostgresDSN != "" {
		slog.Info("initializing postgresql storage")
		pgStore, err = postgres.New(context.Background(), cfg)
		if err != nil {
			slog.Error("failed to initialize postgresql storage, falling back to in-memory", "error", err)
			sentry.CaptureException(err)
			db = memory.New()
		} else {
			db = pgStore
		}
	} else {
		slog.Info("initializing in-memory storage")
		db = memory.New()
	}

	// 4. Init services
	jwtService := auth.NewJWTService(cfg)
	userService := user.NewUserService(db, nil) // Temporarily passing nil for smsService
	oauthService := oauth.NewOAuthService(db)
	wechatService := wechat.NewWeChatService(cfg)

	// 5. Init handlers
	userHandler := handler.NewUserHandler(userService, jwtService)
	oauthHandler := handler.NewOAuthHandler(cfg, oauthService, jwtService)
	wechatHandler := handler.NewWeChatHandler(wechatService, oauthService, jwtService, db)

	// 6. Init Gin router
	router := gin.New()
	// Add Sentry middleware
	if cfg.SentryDSN != "" {
		router.Use(sentrygin.New(sentrygin.Options{}))
	}
	router.Use(middleware.StructuredLogger())
	router.Use(gin.Recovery())

	// 7. Setup routes
	docs.SwaggerInfo.BasePath = "/api"
	router.GET("/swagger/*any", ginSwagger.WrapHandler(swaggerFiles.Handler))
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
	router.GET("/ping", func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{"message": "pong"})
	})

	// 8. Start server with graceful shutdown
	srv := &http.Server{
		Addr:    ":" + cfg.ServerPort,
		Handler: router,
	}

	go func() {
		slog.Info("starting server", "port", cfg.ServerPort)
		if err := srv.ListenAndServe(); err != nil && !errors.Is(err, http.ErrServerClosed) {
			slog.Error("listen and serve failed", "error", err)
			os.Exit(1)
		}
	}()

	// Wait for interrupt signal to gracefully shut down the server
	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit
	slog.Info("shutting down server...")

	// The context is used to inform the server it has 5 seconds to finish
	// the requests it is currently handling
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()

	if pgStore != nil {
		slog.Info("closing database connection pool")
		pgStore.Close()
	}

	if err := srv.Shutdown(ctx); err != nil {
		slog.Error("server forced to shutdown", "error", err)
		os.Exit(1)
	}

	slog.Info("server exiting")
}
