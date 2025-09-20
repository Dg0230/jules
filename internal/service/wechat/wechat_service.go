package wechat

import (
	"auth-service/internal/config"
	"context"
	"encoding/json"
	"fmt"
	"net/http"
	"sync"
	"time"
)

// WeChatService handles communication with WeChat's APIs.
type WeChatService struct {
	cfg              *config.Config
	accessToken      string
	accessTokenLock  sync.RWMutex
	accessTokenExpAt time.Time
}

// NewWeChatService creates a new WeChatService.
func NewWeChatService(cfg *config.Config) *WeChatService {
	return &WeChatService{cfg: cfg}
}

// WeChatTicket represents the response from WeChat when requesting a QR code ticket.
type WeChatTicket struct {
	Ticket        string `json:"ticket"`
	ExpireSeconds int    `json:"expire_seconds"`
	URL           string `json:"url"` // The URL the user needs to visit (embedded in QR)
}

type accessTokenResponse struct {
	AccessToken string `json:"access_token"`
	ExpiresIn   int    `json:"expires_in"`
	ErrCode     int    `json:"errcode"`
	ErrMsg      string `json:"errmsg"`
}

func (s *WeChatService) getAccessToken(ctx context.Context) (string, error) {
	s.accessTokenLock.RLock()
	// Check if token is still valid (with a small buffer)
	if s.accessToken != "" && time.Now().Before(s.accessTokenExpAt.Add(-1*time.Minute)) {
		s.accessTokenLock.RUnlock()
		return s.accessToken, nil
	}
	s.accessTokenLock.RUnlock()

	// If not valid, get a write lock to fetch a new one
	s.accessTokenLock.Lock()
	defer s.accessTokenLock.Unlock()

	// Double-check in case another goroutine just refreshed it
	if s.accessToken != "" && time.Now().Before(s.accessTokenExpAt.Add(-1*time.Minute)) {
		return s.accessToken, nil
	}

	url := fmt.Sprintf("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s", s.cfg.WeChatAppID, s.cfg.WeChatAppSecret)
	resp, err := http.Get(url)
	if err != nil {
		return "", fmt.Errorf("failed to get wechat access token: %w", err)
	}
	defer resp.Body.Close()

	var tokenResp accessTokenResponse
	if err := json.NewDecoder(resp.Body).Decode(&tokenResp); err != nil {
		return "", fmt.Errorf("failed to decode wechat access token response: %w", err)
	}

	if tokenResp.ErrCode != 0 {
		return "", fmt.Errorf("wechat api error getting access token: code=%d, msg=%s", tokenResp.ErrCode, tokenResp.ErrMsg)
	}

	s.accessToken = tokenResp.AccessToken
	s.accessTokenExpAt = time.Now().Add(time.Duration(tokenResp.ExpiresIn) * time.Second)

	return s.accessToken, nil
}

	"bytes"
)

// GetQRCodeTicket fetches a new QR code ticket from WeChat.
func (s *WeChatService) GetQRCodeTicket(ctx context.Context, sessionID string) (*WeChatTicket, error) {
	accessToken, err := s.getAccessToken(ctx)
	if err != nil {
		return nil, fmt.Errorf("failed to get access token for qr code: %w", err)
	}

	url := fmt.Sprintf("https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=%s", accessToken)

	// Create a temporary QR code that expires in 5 minutes, containing the session ID.
	reqBodyMap := map[string]interface{}{
		"expire_seconds": 300,
		"action_name":    "QR_SCENE",
		"action_info": map[string]interface{}{
			"scene": map[string]string{
				"scene_str": sessionID,
			},
		},
	}
	reqBodyBytes, err := json.Marshal(reqBodyMap)
	if err != nil {
		return nil, fmt.Errorf("failed to marshal qr code request body: %w", err)
	}

	resp, err := http.Post(url, "application/json", bytes.NewBuffer(reqBodyBytes))
	if err != nil {
		return nil, fmt.Errorf("failed to post request for qr code ticket: %w", err)
	}
	defer resp.Body.Close()

	var ticketResp WeChatTicket
	if err := json.NewDecoder(resp.Body).Decode(&ticketResp); err != nil {
		return nil, fmt.Errorf("failed to decode qr code ticket response: %w", err)
	}

	// The ticket response can also contain error codes
	// We'd need to define a shared error struct to check for that, but we'll omit for now.

	return &ticketResp, nil
}
