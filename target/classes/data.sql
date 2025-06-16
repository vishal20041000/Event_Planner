-- Insert sample venues
INSERT INTO venues (name, location, capacity, price_per_day, created_at, updated_at)
VALUES 
('Grand Ballroom', 'New York', 500, 5000.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Sports Complex', 'Mumbai', 1000, 3000.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Beach Resort', 'Goa', 300, 8000.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Mountain Lodge', 'Lonavala', 200, 4000.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- Insert sample events
INSERT INTO events (name, location, event_date, event_type, description, weather_suitability, created_at, updated_at)
VALUES 
('Cricket Tournament', 'Mumbai', '2024-03-16 09:00:00', 'Sports', 'Annual cricket tournament', 'Pending', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Beach Wedding', 'Goa', '2024-12-10 16:00:00', 'Wedding', 'Beachside wedding ceremony', 'Pending', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Mountain Hiking', 'Lonavala', '2024-10-20 08:00:00', 'Hiking', 'Guided mountain hiking trip', 'Pending', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT DO NOTHING;

-- Insert a test venue in Mumbai
INSERT INTO venues (name, location, capacity, price_per_day, created_at, updated_at)
VALUES ('Grand Resort', 'Mumbai', 500, 50000.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert amenities for the venue
INSERT INTO venue_amenities (venue_id, amenity)
VALUES 
    (1, 'Conference Room'),
    (1, 'Catering Service'),
    (1, 'Parking'),
    (1, 'WiFi'),
    (1, 'Projector'),
    (1, 'Air Conditioning'),
    (1, 'Outdoor Space'),
    (1, 'Indoor Space'); 