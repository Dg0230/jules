package sms

import (
	"context"
	"testing"

	"github.com/stretchr/testify/mock"
)

// MockSMSService is a mock implementation of the SMSService interface for testing.
type MockSMSService struct {
	mock.Mock
}

// Send is the mock implementation of the Send method.
func (m *MockSMSService) Send(ctx context.Context, phoneNumber string, otpCode string) error {
	args := m.Called(ctx, phoneNumber, otpCode)
	return args.Error(0)
}

// AssertSendCalledWith is a helper function for tests to assert that Send was called with specific arguments.
func (m *MockSMSService) AssertSendCalledWith(t *testing.T, phoneNumber string, otpCode string) {
	m.AssertCalled(t, "Send", mock.Anything, phoneNumber, otpCode)
}
