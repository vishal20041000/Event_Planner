-- Add missing columns to venues table
ALTER TABLE venues
ADD COLUMN IF NOT EXISTS available BOOLEAN DEFAULT true,
ADD COLUMN IF NOT EXISTS type VARCHAR(255),
ADD COLUMN IF NOT EXISTS description TEXT,
ADD COLUMN IF NOT EXISTS price_per_hour FLOAT;

-- Create venue_amenities table if it doesn't exist
CREATE TABLE IF NOT EXISTS venue_amenities (
    venue_id BIGINT NOT NULL,
    amenity VARCHAR(255),
    FOREIGN KEY (venue_id) REFERENCES venues(id)
); 