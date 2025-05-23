-- Users table
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE,
    phone_number VARCHAR(50) UNIQUE,
    status VARCHAR(50) DEFAULT 'active', -- e.g., active, inactive, suspended
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_phone_number ON users(phone_number);

-- Roles table
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Permissions table
CREATE TABLE permissions (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE, -- e.g., create_user, delete_user, view_reports
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Role-Permissions link table
CREATE TABLE role_permissions (
    role_id INTEGER NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id INTEGER NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_role_permissions_role_id ON role_permissions(role_id);
CREATE INDEX idx_role_permissions_permission_id ON role_permissions(permission_id);

-- User-Roles link table
CREATE TABLE user_roles (
    user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id INTEGER NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role_id ON user_roles(role_id);

-- Channels table
CREATE TABLE channels (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    contact_person VARCHAR(255),
    contact_email VARCHAR(255),
    contact_phone VARCHAR(50),
    commission_rate DECIMAL(5, 2) DEFAULT 0.00, -- e.g., 0.10 for 10%
    status VARCHAR(50) DEFAULT 'active', -- e.g., active, inactive
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_channels_status ON channels(status);
CREATE INDEX idx_channels_name ON channels(name);

-- Merchants table
CREATE TABLE merchants (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    channel_id INTEGER REFERENCES channels(id) ON DELETE SET NULL, -- Can be null if not associated with a channel
    contact_person VARCHAR(255),
    contact_email VARCHAR(255),
    contact_phone VARCHAR(50),
    address TEXT,
    status VARCHAR(50) DEFAULT 'pending', -- e.g., pending, active, inactive, suspended
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_merchants_status ON merchants(status);
CREATE INDEX idx_merchants_channel_id ON merchants(channel_id);
CREATE INDEX idx_merchants_name ON merchants(name);

-- Review Cards table
CREATE TABLE review_cards (
    id SERIAL PRIMARY KEY,
    merchant_id INTEGER NOT NULL REFERENCES merchants(id) ON DELETE CASCADE,
    card_identifier VARCHAR(255) NOT NULL UNIQUE, -- Unique ID for the physical card or QR code
    status VARCHAR(50) DEFAULT 'active', -- e.g., active, inactive, assigned, unassigned
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_review_cards_merchant_id ON review_cards(merchant_id);
CREATE INDEX idx_review_cards_status ON review_cards(status);
CREATE INDEX idx_review_cards_card_identifier ON review_cards(card_identifier);

-- Materials table
CREATE TABLE materials (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50), -- e.g., image, video, text, template_id
    content TEXT, -- URL to content, or text content itself, or template identifier
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_materials_type ON materials(type);

-- Material Collections table
CREATE TABLE material_collections (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Collection-Materials link table
CREATE TABLE collection_materials (
    collection_id INTEGER NOT NULL REFERENCES material_collections(id) ON DELETE CASCADE,
    material_id INTEGER NOT NULL REFERENCES materials(id) ON DELETE CASCADE,
    display_order INTEGER DEFAULT 0,
    PRIMARY KEY (collection_id, material_id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_collection_materials_collection_id ON collection_materials(collection_id);
CREATE INDEX idx_collection_materials_material_id ON collection_materials(material_id);

-- Review Card Material Collections link table
CREATE TABLE review_card_material_collections (
    review_card_id INTEGER NOT NULL REFERENCES review_cards(id) ON DELETE CASCADE,
    material_collection_id INTEGER NOT NULL REFERENCES material_collections(id) ON DELETE CASCADE,
    PRIMARY KEY (review_card_id, material_collection_id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_rcmc_review_card_id ON review_card_material_collections(review_card_id);
CREATE INDEX idx_rcmc_material_collection_id ON review_card_material_collections(material_collection_id);

-- Reviews table
CREATE TABLE reviews (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id) ON DELETE SET NULL, -- User who left the review (can be anonymous)
    review_card_id INTEGER NOT NULL REFERENCES review_cards(id) ON DELETE CASCADE,
    rating INTEGER CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    status VARCHAR(50) DEFAULT 'pending', -- e.g., pending, approved, rejected
    reviewer_ip_address VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_reviews_user_id ON reviews(user_id);
CREATE INDEX idx_reviews_review_card_id ON reviews(review_card_id);
CREATE INDEX idx_reviews_status ON reviews(status);
CREATE INDEX idx_reviews_rating ON reviews(rating);

-- Orders table
CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    order_type VARCHAR(50) NOT NULL, -- e.g., 'review_card_purchase', 'subscription', 'service_fee'
    merchant_id INTEGER REFERENCES merchants(id) ON DELETE SET NULL,
    channel_id INTEGER REFERENCES channels(id) ON DELETE SET NULL,
    user_id INTEGER REFERENCES users(id) ON DELETE SET NULL, -- User who placed the order (if applicable)
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'CNY',
    status VARCHAR(50) DEFAULT 'pending', -- e.g., pending, paid, failed, refunded
    payment_gateway_transaction_id VARCHAR(255),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_order_entity CHECK (merchant_id IS NOT NULL OR channel_id IS NOT NULL OR user_id IS NOT NULL) -- At least one entity must be associated
);

CREATE INDEX idx_orders_merchant_id ON orders(merchant_id);
CREATE INDEX idx_orders_channel_id ON orders(channel_id);
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);
CREATE INDEX idx_orders_order_type ON orders(order_type);

-- Profit Sharing Records table
CREATE TABLE profit_sharing_records (
    id SERIAL PRIMARY KEY,
    order_id INTEGER NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    channel_id INTEGER REFERENCES channels(id) ON DELETE SET NULL, -- Channel receiving profit
    merchant_id INTEGER REFERENCES merchants(id) ON DELETE SET NULL, -- Merchant involved (e.g., if channel sells to merchant)
    shared_amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'CNY',
    commission_rate_snapshot DECIMAL(5,2), -- Store the rate at the time of transaction
    status VARCHAR(50) DEFAULT 'pending', -- e.g., pending, paid, voided
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_profit_sharing_records_order_id ON profit_sharing_records(order_id);
CREATE INDEX idx_profit_sharing_records_channel_id ON profit_sharing_records(channel_id);
CREATE INDEX idx_profit_sharing_records_merchant_id ON profit_sharing_records(merchant_id);
CREATE INDEX idx_profit_sharing_records_status ON profit_sharing_records(status);

-- Add comments to tables and columns for clarity in PostgreSQL
COMMENT ON TABLE users IS 'Storing basic information of all users';
COMMENT ON TABLE roles IS 'System role definitions';
COMMENT ON TABLE permissions IS 'System permission definitions';
COMMENT ON TABLE role_permissions IS 'Link table for many-to-many relationship between roles and permissions';
COMMENT ON TABLE user_roles IS 'Link table for many-to-many relationship between users and roles';
COMMENT ON TABLE channels IS '渠道商信息 - Channel merchant information';
COMMENT ON TABLE merchants IS '商家信息 - Merchant information, includes a reference to channels';
COMMENT ON COLUMN merchants.channel_id IS 'Reference to the channel that brought in this merchant, if any.';
COMMENT ON TABLE review_cards IS '好评牌信息 - Review card information, includes a reference to merchants';
COMMENT ON COLUMN review_cards.card_identifier IS 'Unique ID for the physical card or QR code, used for linking reviews.';
COMMENT ON TABLE materials IS '素材信息 - Material information, e.g., images, videos, text templates for reviews or promotions';
COMMENT ON COLUMN materials.type IS 'Type of material, e.g., image, video, text, template_id from a messaging service.';
COMMENT ON COLUMN materials.content IS 'URL to content, or text content itself, or a specific template identifier.';
COMMENT ON TABLE material_collections IS '素材集表 - Material collection information, groups of materials';
COMMENT ON TABLE collection_materials IS 'Link table for many-to-many relationship between material_collections and materials';
COMMENT ON TABLE review_card_material_collections IS 'Link table to associate review_cards with specific material_collections to be displayed';
COMMENT ON TABLE reviews IS '用户评价信息 - User review information, includes references to users and review_cards';
COMMENT ON TABLE orders IS '充值订单信息 - Top-up order information, includes references to merchants or channels or users depending on who is ordering';
COMMENT ON COLUMN orders.order_type IS 'Defines what the order is for, e.g., review_card_purchase, subscription, service_fee.';
COMMENT ON CONSTRAINT chk_order_entity ON orders IS 'Ensures that an order is associated with at least one entity: a merchant, a channel, or a user.';
COMMENT ON TABLE profit_sharing_records IS '分润记录表 - Profit sharing records, includes references to orders, channels, and potentially merchants';
COMMENT ON COLUMN profit_sharing_records.commission_rate_snapshot IS 'Stores the commission rate at the time of the transaction for historical accuracy.';

-- Example of how to handle updated_at automatically (PostgreSQL specific)
CREATE OR REPLACE FUNCTION trigger_set_timestamp()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = NOW();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply the trigger to all tables that have updated_at
DO $$
DECLARE
    t_name TEXT;
BEGIN
    FOR t_name IN 
        SELECT table_name 
        FROM information_schema.columns 
        WHERE column_name = 'updated_at' AND table_schema = 'public' -- Adjust schema if needed
    LOOP
        EXECUTE format('CREATE TRIGGER set_timestamp
                        BEFORE UPDATE ON %I
                        FOR EACH ROW
                        EXECUTE PROCEDURE trigger_set_timestamp();', t_name);
    END LOOP;
END $$;

-- Note: For other SQL databases like MySQL, the auto-update mechanism for `updated_at` would be different.
-- For MySQL, it's typically: `updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP`
-- This script is primarily for PostgreSQL due to SERIAL, TIMESTAMP WITH TIME ZONE, and the trigger function syntax.
-- If using another RDBMS, adjustments for auto-incrementing IDs, timestamp types, and triggers will be needed.
