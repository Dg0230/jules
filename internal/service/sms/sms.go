package sms

import "context"

// SMSService defines the interface for sending SMS messages.
type SMSService interface {
	// Send sends an SMS with the given OTP code to the specified phone number.
	Send(ctx context.Context, phoneNumber string, otpCode string) error
}
