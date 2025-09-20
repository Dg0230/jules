package memory

import (
	"auth-service/internal/domain"
	"auth-service/internal/storage"
	"context"
	"sync"

	"github.com/google/uuid"
)

// MemStorage is an in-memory implementation of the Storage interface.
type MemStorage struct {
	mu             sync.RWMutex
	users          map[uuid.UUID]*domain.User
	identities     map[uuid.UUID]*domain.Identity
	otps           map[string]*domain.OTP // Keyed by email
	wechatSessions map[string]*domain.WeChatLoginSession // Keyed by SessionID
}

// New creates and returns a new MemStorage instance.
func New() *MemStorage {
	return &MemStorage{
		users:          make(map[uuid.UUID]*domain.User),
		identities:     make(map[uuid.UUID]*domain.Identity),
		otps:           make(map[string]*domain.OTP),
		wechatSessions: make(map[string]*domain.WeChatLoginSession),
	}
}

// StoreOTP saves an OTP for a given email.
func (s *MemStorage) StoreOTP(ctx context.Context, otp *domain.OTP) error {
	s.mu.Lock()
	defer s.mu.Unlock()
	s.otps[otp.Email] = otp
	return nil
}

// FindOTP retrieves an OTP for a given email.
func (s *MemStorage) FindOTP(ctx context.Context, email string) (*domain.OTP, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()
	otp, exists := s.otps[email]
	if !exists {
		return nil, storage.ErrIdentityNotFound // Re-using error for simplicity
	}
	return otp, nil
}

// DeleteOTP removes an OTP for a given email.
func (s *MemStorage) DeleteOTP(ctx context.Context, email string) error {
	s.mu.Lock()
	defer s.mu.Unlock()
	delete(s.otps, email)
	return nil
}

// StoreWeChatSession saves a new WeChat login session.
func (s *MemStorage) StoreWeChatSession(ctx context.Context, session *domain.WeChatLoginSession) error {
	s.mu.Lock()
	defer s.mu.Unlock()
	if _, exists := s.wechatSessions[session.SessionID]; exists {
		return storage.ErrDuplicateRecord
	}
	s.wechatSessions[session.SessionID] = session
	return nil
}

// FindWeChatSessionByTicket retrieves a WeChat login session by its ticket.
func (s *MemStorage) FindWeChatSessionByTicket(ctx context.Context, ticket string) (*domain.WeChatLoginSession, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()
	for _, session := range s.wechatSessions {
		if session.Ticket == ticket {
			return session, nil
		}
	}
	return nil, storage.ErrIdentityNotFound
}

// FindWeChatSession retrieves a WeChat login session by its ID.
func (s *MemStorage) FindWeChatSession(ctx context.Context, sessionID string) (*domain.WeChatLoginSession, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()
	session, exists := s.wechatSessions[sessionID]
	if !exists {
		return nil, storage.ErrIdentityNotFound // Re-using error for simplicity
	}
	return session, nil
}

// UpdateWeChatSession updates an existing WeChat login session.
func (s *MemStorage) UpdateWeChatSession(ctx context.Context, session *domain.WeChatLoginSession) error {
	s.mu.Lock()
	defer s.mu.Unlock()
	if _, exists := s.wechatSessions[session.SessionID]; !exists {
		return storage.ErrIdentityNotFound
	}
	s.wechatSessions[session.SessionID] = session
	return nil
}

// CreateUser adds a new user to the in-memory store.
func (s *MemStorage) CreateUser(ctx context.Context, user *domain.User) error {
	s.mu.Lock()
	defer s.mu.Unlock()

	if _, exists := s.users[user.ID]; exists {
		return storage.ErrDuplicateRecord
	}
	s.users[user.ID] = user
	return nil
}

// FindUserByID retrieves a user by their ID.
func (s *MemStorage) FindUserByID(ctx context.Context, id uuid.UUID) (*domain.User, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	user, exists := s.users[id]
	if !exists {
		return nil, storage.ErrUserNotFound
	}
	return user, nil
}

// CreateIdentity adds a new identity to the in-memory store.
func (s *MemStorage) CreateIdentity(ctx context.Context, identity *domain.Identity) error {
	s.mu.Lock()
	defer s.mu.Unlock()

	// Check for duplicates based on provider and providerID
	for _, i := range s.identities {
		if i.Provider == identity.Provider && i.ProviderID == identity.ProviderID {
			return storage.ErrDuplicateRecord
		}
	}

	if _, exists := s.identities[identity.ID]; exists {
		return storage.ErrDuplicateRecord
	}
	s.identities[identity.ID] = identity
	return nil
}

// FindIdentityByProvider retrieves an identity by its provider and provider-specific ID.
func (s *MemStorage) FindIdentityByProvider(ctx context.Context, provider, providerID string) (*domain.Identity, error) {
	s.mu.RLock()
	defer s.mu.RUnlock()

	for _, identity := range s.identities {
		if identity.Provider == provider && identity.ProviderID == providerID {
			return identity, nil
		}
	}

	return nil, storage.ErrIdentityNotFound
}
