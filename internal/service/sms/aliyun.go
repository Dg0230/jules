package sms

import (
	"auth-service/internal/config"
	"context"
	"fmt"

	openapi "github.com/alibabacloud-go/darabonba-openapi/v2/client"
	dysmsapi "github.com/alibabacloud-go/dysmsapi-20180501"
	"github.com/alibabacloud-go/tea"
)

// AliyunSMSService is an implementation of SMSService for Alibaba Cloud.
type AliyunSMSService struct {
	client *dysmsapi.Client
	cfg    *config.Config
}

// NewAliyunSMSService creates a new AliyunSMSService.
func NewAliyunSMSService(cfg *config.Config) (*AliyunSMSService, error) {
	if cfg.AliyunAccessKeyId == "" || cfg.AliyunAccessKeySecret == "" {
		// Return a nil service if not configured, allowing graceful degradation.
		// The calling code should handle this.
		return nil, fmt.Errorf("aliyun SMS service is not configured")
	}

	clientCfg := &openapi.Config{
		AccessKeyId:     tea.String(cfg.AliyunAccessKeyId),
		AccessKeySecret: tea.String(cfg.AliyunAccessKeySecret),
		Endpoint:        tea.String("dysmsapi.aliyuncs.com"),
	}

	client, err := dysmsapi.NewClient(clientCfg)
	if err != nil {
		return nil, err
	}

	return &AliyunSMSService{
		client: client,
		cfg:    cfg,
	}, nil
}

// Send sends an SMS using the Alibaba Cloud Dysmsapi.
func (s *AliyunSMSService) Send(ctx context.Context, phoneNumber string, otpCode string) error {
	req := &dysmsapi.SendSmsRequest{
		PhoneNumbers:  tea.String(phoneNumber),
		SignName:      tea.String(s.cfg.SmsSignName),
		TemplateCode:  tea.String(s.cfg.SmsTemplateCode),
		TemplateParam: tea.String(fmt.Sprintf(`{"code":"%s"}`, otpCode)),
	}

	resp, err := s.client.SendSms(req)
	if err != nil {
		// This handles network-level errors.
		return fmt.Errorf("failed to send sms request to aliyun: %w", err)
	}

	// This handles API-level errors (e.g., invalid signature, bad parameters).
	if resp.Body == nil || resp.Body.Code == nil || *resp.Body.Code != "OK" {
		var code, msg string
		if resp.Body != nil && resp.Body.Code != nil {
			code = *resp.Body.Code
		}
		if resp.Body != nil && resp.Body.Message != nil {
			msg = *resp.Body.Message
		}
		return fmt.Errorf("aliyun sms api returned an error: code=%s, message=%s", code, msg)
	}

	return nil
}
