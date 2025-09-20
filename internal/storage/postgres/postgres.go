package postgres

import (
	"auth-service/internal/config"
	"auth-service/internal/domain"
	"auth-service/internal/storage"
	"context"
	"fmt"

	"github.com/google/uuid"
	"github.com/jackc/pgx/v5"
	"github.com/jackc/pgx/v5/pgxpool"
)

// PostgresStorage is a PostgreSQL implementation of the Storage interface.
type PostgresStorage struct {
	pool *pgxpool.Pool
}

// New creates a new PostgresStorage instance and connects to the database.
func New(ctx context.Context, cfg *config.Config) (*PostgresStorage, error) {
	pool, err := pgxpool.New(ctx, cfg.PostgresDSN)
	if err != nil {
		return nil, fmt.Errorf("unable to create connection pool: %w", err)
	}
	if err := pool.Ping(ctx); err != nil {
		pool.Close()
		return nil, fmt.Errorf("unable to ping database: %w", err)
	}
	return &PostgresStorage{pool: pool}, nil
}

// Close closes the database connection pool.
func (p *PostgresStorage) Close() {
	p.pool.Close()
}

// Ensure PostgresStorage implements the Storage interface.
var _ storage.Storage = (*PostgresStorage)(nil)

func (p *PostgresStorage) CreateUser(ctx context.Context, user *domain.User) error {
	query := `INSERT INTO users (id, created_at, updated_at) VALUES ($1, $2, $3)`
	_, err := p.pool.Exec(ctx, query, user.ID, user.CreatedAt, user.UpdatedAt)
	if err != nil {
		// In a real app, you would check for a unique violation error code from postgres
		// and return storage.ErrDuplicateRecord
		return fmt.Errorf("failed to create user: %w", err)
	}
	return nil
}

func (p *PostgresStorage) FindUserByID(ctx context.Context, id uuid.UUID) (*domain.User, error) {
	query := `SELECT id, created_at, updated_at FROM users WHERE id = $1`
	user := &domain.User{}
	err := p.pool.QueryRow(ctx, query, id).Scan(&user.ID, &user.CreatedAt, &user.UpdatedAt)
	if err != nil {
		if err == pgx.ErrNoRows {
			return nil, storage.ErrUserNotFound
		}
		return nil, fmt.Errorf("failed to find user by id: %w", err)
	}
	return user, nil
}

func (p *PostgresStorage) CreateIdentity(ctx context.Context, identity *domain.Identity) error {
	query := `INSERT INTO identities (id, user_id, provider, provider_id, password_hash, created_at, updated_at)
              VALUES ($1, $2, $3, $4, $5, $6, $7)`
	_, err := p.pool.Exec(ctx, query, identity.ID, identity.UserID, identity.Provider, identity.ProviderID, identity.PasswordHash, identity.CreatedAt, identity.UpdatedAt)
	if err != nil {
		return fmt.Errorf("failed to create identity: %w", err)
	}
	return nil
}

func (p *PostgresStorage) FindIdentityByProvider(ctx context.Context, provider, providerID string) (*domain.Identity, error) {
	query := `SELECT id, user_id, provider, provider_id, password_hash, created_at, updated_at
              FROM identities WHERE provider = $1 AND provider_id = $2`
	identity := &domain.Identity{}
	err := p.pool.QueryRow(ctx, query, provider, providerID).Scan(
		&identity.ID, &identity.UserID, &identity.Provider, &identity.ProviderID, &identity.PasswordHash, &identity.CreatedAt, &identity.UpdatedAt,
	)
	if err != nil {
		if err == pgx.ErrNoRows {
			return nil, storage.ErrIdentityNotFound
		}
		return nil, fmt.Errorf("failed to find identity by provider: %w", err)
	}
	return identity, nil
}

func (p *PostgresStorage) StoreOTP(ctx context.Context, otp *domain.OTP) error {
	query := `INSERT INTO otps (identifier, code, expires_at) VALUES ($1, $2, $3)
              ON CONFLICT (identifier) DO UPDATE SET code = $2, expires_at = $3`
	_, err := p.pool.Exec(ctx, query, otp.Email, otp.Code, otp.ExpiresAt)
	if err != nil {
		return fmt.Errorf("failed to store otp: %w", err)
	}
	return nil
}

func (p *PostgresStorage) FindOTP(ctx context.Context, email string) (*domain.OTP, error) {
	query := `SELECT identifier, code, expires_at FROM otps WHERE identifier = $1`
	otp := &domain.OTP{}
	err := p.pool.QueryRow(ctx, query, email).Scan(&otp.Email, &otp.Code, &otp.ExpiresAt)
	if err != nil {
		if err == pgx.ErrNoRows {
			return nil, storage.ErrIdentityNotFound
		}
		return nil, fmt.Errorf("failed to find otp: %w", err)
	}
	return otp, nil
}

func (p *PostgresStorage) DeleteOTP(ctx context.Context, email string) error {
	query := `DELETE FROM otps WHERE identifier = $1`
	_, err := p.pool.Exec(ctx, query, email)
	if err != nil {
		return fmt.Errorf("failed to delete otp: %w", err)
	}
	return nil
}

func (p *PostgresStorage) StoreWeChatSession(ctx context.Context, session *domain.WeChatLoginSession) error {
	query := `INSERT INTO wechat_sessions (session_id, ticket, status, created_at) VALUES ($1, $2, $3, $4)`
	_, err := p.pool.Exec(ctx, query, session.SessionID, session.Ticket, session.Status, session.CreatedAt)
	if err != nil {
		return fmt.Errorf("failed to store wechat session: %w", err)
	}
	return nil
}

func (p *PostgresStorage) FindWeChatSession(ctx context.Context, sessionID string) (*domain.WeChatLoginSession, error) {
	query := `SELECT session_id, ticket, status, user_id, token, created_at FROM wechat_sessions WHERE session_id = $1`
	session := &domain.WeChatLoginSession{}
	// Need to handle NULLable fields user_id and token
	err := p.pool.QueryRow(ctx, query, sessionID).Scan(
		&session.SessionID, &session.Ticket, &session.Status, &session.UserID, &session.Token, &session.CreatedAt,
	)
	if err != nil {
		if err == pgx.ErrNoRows {
			return nil, storage.ErrIdentityNotFound
		}
		return nil, fmt.Errorf("failed to find wechat session: %w", err)
	}
	return session, nil
}

func (p *PostgresStorage) FindWeChatSessionByTicket(ctx context.Context, ticket string) (*domain.WeChatLoginSession, error) {
	query := `SELECT session_id, ticket, status, user_id, token, created_at FROM wechat_sessions WHERE ticket = $1`
	session := &domain.WeChatLoginSession{}
	err := p.pool.QueryRow(ctx, query, ticket).Scan(
		&session.SessionID, &session.Ticket, &session.Status, &session.UserID, &session.Token, &session.CreatedAt,
	)
	if err != nil {
		if err == pgx.ErrNoRows {
			return nil, storage.ErrIdentityNotFound
		}
		return nil, fmt.Errorf("failed to find wechat session by ticket: %w", err)
	}
	return session, nil
}

func (p *PostgresStorage) UpdateWeChatSession(ctx context.Context, session *domain.WeChatLoginSession) error {
	query := `UPDATE wechat_sessions SET status = $2, user_id = $3, token = $4 WHERE session_id = $1`
	_, err := p.pool.Exec(ctx, query, session.SessionID, session.Status, session.UserID, session.Token)
	if err != nil {
		return fmt.Errorf("failed to update wechat session: %w", err)
	}
	return nil
}
