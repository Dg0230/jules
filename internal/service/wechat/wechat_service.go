package wechat

import (
	"auth-service/internal/config"
	"context"
	"fmt"
)

// WeChatService handles communication with WeChat's APIs.
type WeChatService struct {
	cfg *config.Config
}

// NewWeChatService creates a new WeChatService.
func NewWeChatService(cfg *config.Config) *WeChatService {
	return &WeChatService{cfg: cfg}
}

// WeChatTicket represents the response from WeChat when requesting a QR code ticket.
type WeChatTicket struct {
	Ticket        string `json:"ticket"`
	ExpireSeconds int    `json:"expire_seconds"`
	URL           string `json:"url"`
}

// GetQRCodeTicket simulates fetching a new QR code ticket from WeChat.
// In a real application, this would make live API calls.
func (s *WeChatService) GetQRCodeTicket(ctx context.Context, sessionID string) (*WeChatTicket, error) {
	// Since we can't make live calls without credentials, we return a mock ticket.
	// This allows us to test the handler and storage logic.
	mockTicket := fmt.Sprintf("mock_ticket_for_session_%s", sessionID)
	mockURL := "https://weixin.qq.com/q/MOCK_URL_FOR_TESTING"

	return &WeChatTicket{
		Ticket:        mockTicket,
		ExpireSeconds: 300,
		URL:           mockURL,
	}, nil
}
