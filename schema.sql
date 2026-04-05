-- Enhanced Database schema with Authentication, Documents, and Analytics

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Users and Authentication
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role VARCHAR(50) NOT NULL CHECK (role IN ('ADMIN', 'COUNTRY_ADMIN', 'SUPPLIER', 'VIEWER')),
    country_id BIGINT,
    is_active BOOLEAN DEFAULT true,
    email_verified BOOLEAN DEFAULT false,
    last_login TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(500) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Countries
CREATE TABLE countries (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    country_code VARCHAR(3) NOT NULL UNIQUE,
    contact_email VARCHAR(255) NOT NULL,
    contact_phone VARCHAR(50),
    timezone VARCHAR(50) DEFAULT 'UTC',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Medications
CREATE TABLE medications (
    id BIGSERIAL PRIMARY KEY,
    generic_name VARCHAR(255) NOT NULL,
    brand_name VARCHAR(255),
    dosage_form VARCHAR(100),
    strength VARCHAR(100),
    description TEXT,
    atc_code VARCHAR(20),
    therapeutic_category VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Shortages
CREATE TABLE shortages (
    id BIGSERIAL PRIMARY KEY,
    country_id BIGINT REFERENCES countries(id) ON DELETE CASCADE,
    medication_id BIGINT REFERENCES medications(id) ON DELETE CASCADE,
    created_by BIGINT REFERENCES users(id),
    quantity_needed INTEGER NOT NULL,
    unit VARCHAR(50) NOT NULL,
    urgency_level VARCHAR(20) NOT NULL CHECK (urgency_level IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    reason TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'FULFILLED', 'CANCELLED', 'EXPIRED')),
    deadline TIMESTAMP,
    estimated_value DECIMAL(15, 2),
    currency VARCHAR(3) DEFAULT 'USD',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tenders
CREATE TABLE tenders (
    id BIGSERIAL PRIMARY KEY,
    shortage_id BIGINT REFERENCES shortages(id) ON DELETE CASCADE,
    supplier_country_id BIGINT REFERENCES countries(id) ON DELETE CASCADE,
    submitted_by BIGINT REFERENCES users(id),
    quantity_offered INTEGER NOT NULL,
    unit VARCHAR(50) NOT NULL,
    price_per_unit DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    delivery_time_days INTEGER NOT NULL,
    manufacturer_name VARCHAR(255),
    batch_number VARCHAR(100),
    expiry_date DATE,
    regulatory_approval_info TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'WITHDRAWN')),
    notes TEXT,
    reviewed_by BIGINT REFERENCES users(id),
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Documents (for regulatory certificates, etc.)
CREATE TABLE documents (
    id BIGSERIAL PRIMARY KEY,
    uuid UUID DEFAULT uuid_generate_v4() UNIQUE,
    tender_id BIGINT REFERENCES tenders(id) ON DELETE CASCADE,
    uploaded_by BIGINT REFERENCES users(id),
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    mime_type VARCHAR(100) NOT NULL,
    document_type VARCHAR(50) NOT NULL CHECK (document_type IN ('CERTIFICATE', 'LICENSE', 'APPROVAL', 'QUALITY_REPORT', 'OTHER')),
    description TEXT,
    verified BOOLEAN DEFAULT false,
    verified_by BIGINT REFERENCES users(id),
    verified_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Notifications
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    related_entity_type VARCHAR(50),
    related_entity_id BIGINT,
    is_read BOOLEAN DEFAULT false,
    read_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Email Queue
CREATE TABLE email_queue (
    id BIGSERIAL PRIMARY KEY,
    recipient_email VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    template VARCHAR(100),
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SENT', 'FAILED')),
    error_message TEXT,
    attempts INTEGER DEFAULT 0,
    max_attempts INTEGER DEFAULT 3,
    scheduled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Analytics Tables
CREATE TABLE shortage_analytics (
    id BIGSERIAL PRIMARY KEY,
    shortage_id BIGINT REFERENCES shortages(id) ON DELETE CASCADE,
    views_count INTEGER DEFAULT 0,
    tenders_count INTEGER DEFAULT 0,
    avg_tender_price DECIMAL(10, 2),
    time_to_fulfill_hours INTEGER,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tender_analytics (
    id BIGSERIAL PRIMARY KEY,
    tender_id BIGINT REFERENCES tenders(id) ON DELETE CASCADE,
    response_time_hours INTEGER,
    is_competitive BOOLEAN,
    price_difference_percentage DECIMAL(5, 2),
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE country_analytics (
    id BIGSERIAL PRIMARY KEY,
    country_id BIGINT REFERENCES countries(id) ON DELETE CASCADE,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    total_shortages INTEGER DEFAULT 0,
    total_tenders_submitted INTEGER DEFAULT 0,
    total_tenders_received INTEGER DEFAULT 0,
    avg_response_time_hours DECIMAL(10, 2),
    fulfillment_rate DECIMAL(5, 2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Audit Log
CREATE TABLE audit_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    old_value JSONB,
    new_value JSONB,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_country ON users(country_id);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_expires ON refresh_tokens(expires_at);

CREATE INDEX idx_shortages_country ON shortages(country_id);
CREATE INDEX idx_shortages_medication ON shortages(medication_id);
CREATE INDEX idx_shortages_status ON shortages(status);
CREATE INDEX idx_shortages_urgency ON shortages(urgency_level);
CREATE INDEX idx_shortages_created_by ON shortages(created_by);
CREATE INDEX idx_shortages_deadline ON shortages(deadline);

CREATE INDEX idx_tenders_shortage ON tenders(shortage_id);
CREATE INDEX idx_tenders_supplier ON tenders(supplier_country_id);
CREATE INDEX idx_tenders_status ON tenders(status);
CREATE INDEX idx_tenders_submitted_by ON tenders(submitted_by);

CREATE INDEX idx_documents_tender ON documents(tender_id);
CREATE INDEX idx_documents_uuid ON documents(uuid);
CREATE INDEX idx_documents_uploaded_by ON documents(uploaded_by);

CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_read ON notifications(is_read);
CREATE INDEX idx_notifications_created ON notifications(created_at);

CREATE INDEX idx_email_queue_status ON email_queue(status);
CREATE INDEX idx_email_queue_scheduled ON email_queue(scheduled_at);

CREATE INDEX idx_audit_log_user ON audit_log(user_id);
CREATE INDEX idx_audit_log_entity ON audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_log_created ON audit_log(created_at);

-- Sample Data
INSERT INTO countries (name, country_code, contact_email, contact_phone, timezone) VALUES
('United States', 'USA', 'health@usa.gov', '+1-202-555-0100', 'America/New_York'),
('Germany', 'DEU', 'health@germany.gov', '+49-30-555-0100', 'Europe/Berlin'),
('India', 'IND', 'health@india.gov', '+91-11-555-0100', 'Asia/Kolkata'),
('Brazil', 'BRA', 'health@brazil.gov', '+55-61-555-0100', 'America/Sao_Paulo'),
('South Africa', 'ZAF', 'health@southafrica.gov', '+27-12-555-0100', 'Africa/Johannesburg'),
('Japan', 'JPN', 'health@japan.gov', '+81-3-555-0100', 'Asia/Tokyo'),
('United Kingdom', 'GBR', 'health@uk.gov', '+44-20-555-0100', 'Europe/London');

INSERT INTO medications (generic_name, brand_name, dosage_form, strength, description, atc_code, therapeutic_category) VALUES
('Insulin', 'Humalog', 'Injectable Solution', '100 units/mL', 'Fast-acting insulin for diabetes management', 'A10AB04', 'Antidiabetic'),
('Amoxicillin', 'Amoxil', 'Capsule', '500mg', 'Antibiotic for bacterial infections', 'J01CA04', 'Antibacterial'),
('Metformin', 'Glucophage', 'Tablet', '500mg', 'Oral diabetes medication', 'A10BA02', 'Antidiabetic'),
('Albuterol', 'Ventolin', 'Inhaler', '90mcg', 'Bronchodilator for asthma', 'R03AC02', 'Respiratory'),
('Epinephrine', 'EpiPen', 'Auto-injector', '0.3mg', 'Emergency treatment for severe allergic reactions', 'C01CA24', 'Emergency'),
('Acetaminophen', 'Tylenol', 'Tablet', '500mg', 'Pain reliever and fever reducer', 'N02BE01', 'Analgesic'),
('Lisinopril', 'Prinivil', 'Tablet', '10mg', 'ACE inhibitor for hypertension', 'C09AA03', 'Cardiovascular');

-- Demo user (password: Admin123!)
-- Password hash for 'Admin123!' using BCrypt
INSERT INTO users (email, password_hash, first_name, last_name, role, country_id, is_active, email_verified) VALUES
('admin@mediconnect.global', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IERZmJvfYzEhiKvuK7E2E.1hJ4L8Wi', 'Admin', 'User', 'ADMIN', 1, true, true),
('usa.admin@gov.us', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IERZmJvfYzEhiKvuK7E2E.1hJ4L8Wi', 'John', 'Smith', 'COUNTRY_ADMIN', 1, true, true),
('germany.supplier@pharma.de', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IERZmJvfYzEhiKvuK7E2E.1hJ4L8Wi', 'Hans', 'Mueller', 'SUPPLIER', 2, true, true);

INSERT INTO shortages (country_id, medication_id, created_by, quantity_needed, unit, urgency_level, reason, deadline, estimated_value, currency) VALUES
(1, 1, 2, 50000, 'vials', 'HIGH', 'Manufacturing plant shutdown due to equipment failure', '2026-06-30 23:59:59', 1250000.00, 'USD'),
(3, 2, 1, 100000, 'bottles', 'MEDIUM', 'Increased demand due to seasonal outbreak', '2026-07-15 23:59:59', 500000.00, 'USD'),
(5, 5, 1, 10000, 'units', 'CRITICAL', 'Supply chain disruption affecting emergency services', '2026-05-01 23:59:59', 450000.00, 'USD'),
(7, 3, 1, 75000, 'tablets', 'HIGH', 'National stockpile depletion', '2026-06-15 23:59:59', 375000.00, 'GBP');

INSERT INTO tenders (shortage_id, supplier_country_id, submitted_by, quantity_offered, unit, price_per_unit, currency, delivery_time_days, manufacturer_name, batch_number, status) VALUES
(1, 2, 3, 30000, 'vials', 25.50, 'USD', 14, 'Pharma Corp GmbH', 'BATCH-2026-001', 'PENDING'),
(1, 3, 1, 50000, 'vials', 18.75, 'USD', 21, 'BioMed India Ltd', 'BATCH-2026-002', 'PENDING'),
(3, 4, 1, 8000, 'units', 45.00, 'USD', 7, 'MedSupply Brazil', 'BATCH-2026-003', 'ACCEPTED'),
(2, 6, 1, 100000, 'bottles', 5.25, 'USD', 10, 'Tokyo Pharma', 'BATCH-2026-004', 'PENDING');

-- Initialize analytics
INSERT INTO shortage_analytics (shortage_id, views_count, tenders_count, avg_tender_price) VALUES
(1, 45, 2, 22.13),
(2, 32, 1, 5.25),
(3, 78, 1, 45.00),
(4, 12, 0, NULL);
