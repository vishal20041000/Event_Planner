-- Drop existing tables
DROP TABLE IF EXISTS venue_amenities CASCADE;
DROP TABLE IF EXISTS venues CASCADE;

-- Create venues table
CREATE TABLE venues (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL,
    capacity INTEGER NOT NULL,
    type VARCHAR(255) DEFAULT 'Indoor',
    description TEXT,
    available BOOLEAN DEFAULT true,
    price_per_day FLOAT,
    price_per_hour FLOAT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create venue_amenities table
CREATE TABLE venue_amenities (
    venue_id BIGINT NOT NULL,
    amenity VARCHAR(255),
    CONSTRAINT fk_venue FOREIGN KEY (venue_id) REFERENCES venues(id) ON DELETE CASCADE
); 