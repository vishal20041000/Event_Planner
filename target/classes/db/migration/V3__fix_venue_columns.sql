-- Drop existing venues table if it exists
DROP TABLE IF EXISTS venue_amenities;
DROP TABLE IF EXISTS venues;

-- Recreate venues table with all required columns
CREATE TABLE venues (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL,
    capacity INTEGER NOT NULL,
    type VARCHAR(255),
    description TEXT,
    available BOOLEAN DEFAULT true,
    price_per_day FLOAT,
    price_per_hour FLOAT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create venue_amenities table
CREATE TABLE venue_amenities (
    venue_id BIGINT NOT NULL,
    amenity VARCHAR(255),
    FOREIGN KEY (venue_id) REFERENCES venues(id) ON DELETE CASCADE
); 