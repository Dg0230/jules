package domain

import (
	"time"

	"github.com/google/uuid"
)

// User represents the central user account in the system.
type User struct {
	ID        uuid.UUID `json:"id"`
	CreatedAt time.Time `json:"created_at"`
	UpdatedAt time.Time `json:"updated_at"`
}

// Identity represents a method by which a user can authenticate.
// A single user can have multiple identities (e.g., email/pass, Google, etc.).
type Identity struct {
	ID           uuid.UUID `json:"id"`
	UserID       uuid.UUID `json:"user_id"`
	Provider     string    `json:"provider"`      // e.g., "email", "google", "discord"
	ProviderID   string    `json:"provider_id"`   // The user's ID on the provider's system
	PasswordHash []byte    `json:"-"`             // Only for "email" or "phone" provider
	CreatedAt    time.Time `json:"created_at"`
	UpdatedAt    time.Time `json:"updated_at"`
}

// OTP holds one-time password information.
type OTP struct {
	Code      string
	Email     string
	ExpiresAt time.Time
}

// WeChatLoginSession holds the state for a WeChat QR code login flow.
type WeChatLoginSession struct {
	SessionID string    `json:"session_id"`
	Ticket    string    `json:"ticket"`
	Status    string    `json:"status"` // PENDING, SCANNED, AUTHORIZED, EXPIRED
	UserID    uuid.UUID `json:"user_id,omitempty"`
	Token     string    `json:"token,omitempty"`
	CreatedAt time.Time `json:"created_at"`
}
