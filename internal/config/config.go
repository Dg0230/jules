package config

import (
	"os"
	"time"
)

// Config holds all configuration for the application.
type Config struct {
	ServerPort string
	// Auth
	JWTSecret     string
	TokenLifetime time.Duration
	// OAuth
	GoogleClientID     string
	GoogleClientSecret string
	GoogleRedirectURL  string
	DiscordClientID    string
	DiscordClientSecret string
	DiscordRedirectURL string
	// OAuth - WeChat
	WeChatAppID        string
	WeChatAppSecret    string
	// Aliyun SMS
	AliyunAccessKeyId     string
	AliyunAccessKeySecret string
	SmsSignName           string
	SmsTemplateCode       string
}

// Load loads configuration from environment variables.
func Load() *Config {
	return &Config{
		ServerPort: getEnv("SERVER_PORT", "8080"),
		// Auth
		JWTSecret:     getEnv("JWT_SECRET", "a-very-secret-key"),
		TokenLifetime: 24 * time.Hour, // Default to 24 hours
		// OAuth - Google
		GoogleClientID:     getEnv("GOOGLE_CLIENT_ID", "your-google-client-id"),
		GoogleClientSecret: getEnv("GOOGLE_CLIENT_SECRET", "your-google-client-secret"),
		GoogleRedirectURL:  getEnv("GOOGLE_REDIRECT_URL", "http://localhost:8080/api/oauth/google/callback"),
		// OAuth - Discord
		DiscordClientID:     getEnv("DISCORD_CLIENT_ID", "your-discord-client-id"),
		DiscordClientSecret: getEnv("DISCORD_CLIENT_SECRET", "your-discord-client-secret"),
		DiscordRedirectURL:  getEnv("DISCORD_REDIRECT_URL", "http://localhost:8080/api/oauth/discord/callback"),
		// OAuth - WeChat
		WeChatAppID:        getEnv("WECHAT_APP_ID", "your-wechat-app-id"),
		WeChatAppSecret:    getEnv("WECHAT_APP_SECRET", "your-wechat-app-secret"),
		// Aliyun SMS
		AliyunAccessKeyId:     getEnv("ALIYUN_ACCESS_KEY_ID", ""),
		AliyunAccessKeySecret: getEnv("ALIYUN_ACCESS_KEY_SECRET", ""),
		SmsSignName:           getEnv("ALIYUN_SMS_SIGN_NAME", "AuthService"),
		SmsTemplateCode:       getEnv("ALIYUN_SMS_TEMPLATE_CODE", "SMS_12345678"),
	}
}

// Helper function to get an environment variable or return a default value.
func getEnv(key, defaultValue string) string {
	if value, exists := os.LookupEnv(key); exists {
		return value
	}
	return defaultValue
}
