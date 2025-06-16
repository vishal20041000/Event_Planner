# Smart Event Planner

A Spring Boot application that helps users plan outdoor events by integrating weather data and providing intelligent recommendations.

## Features

- Event Management (CRUD operations)
- Weather Integration with OpenWeatherMap API
- Weather Suitability Analysis
- Alternative Date Suggestions
- Caching for Weather Data
- API Documentation with Swagger

## Prerequisites

- Java 21
- PostgreSQL
- Maven
- OpenWeatherMap API Key

## Setup

1. **Database Setup**
   ```sql
   CREATE DATABASE event_planner;
   ```

2. **Configuration**
   Update `src/main/resources/application.properties`:
   - Set your PostgreSQL password
   - Add your OpenWeatherMap API key

3. **Build and Run**
   ```bash
   ./mvnw clean install
   ./mvnw spring-boot:run
   ```

## API Endpoints

### Event Management

1. Create Event
   ```http
   POST /events
   Content-Type: application/json

   {
     "name": "Cricket Tournament",
     "location": "Mumbai",
     "eventDate": "2024-03-16T10:00:00",
     "eventType": "cricket",
     "description": "Annual cricket tournament"
   }
   ```

2. List All Events
   ```http
   GET /events
   ```

3. Get Event Details
   ```http
   GET /events/{id}
   ```

4. Update Event
   ```http
   PUT /events/{id}
   Content-Type: application/json

   {
     "name": "Updated Event Name",
     "location": "New Location",
     "eventDate": "2024-03-17T10:00:00",
     "eventType": "cricket",
     "description": "Updated description"
   }
   ```

5. Delete Event
   ```http
   DELETE /events/{id}
   ```

### Weather Integration

1. Get Weather for Location and Date
   ```http
   GET /weather/{location}/{date}
   ```

2. Get Weather Forecast
   ```http
   GET /weather/forecast/{location}?startDate=2024-03-16T00:00:00&endDate=2024-03-20T00:00:00
   ```

3. Check Event Weather Suitability
   ```http
   GET /events/{id}/weather-check
   ```

4. Get Alternative Dates
   ```http
   GET /events/{id}/alternatives
   ```

## Weather Scoring

The application uses different scoring criteria for different event types:

### Sports Events (Cricket, etc.)
- Temperature: 15-30°C (ideal)
- Precipitation: < 20%
- Wind Speed: < 20 km/h
- Weather Condition: Clear/Partly Cloudy

### Wedding/Formal Events
- Temperature: 18-28°C (ideal)
- Precipitation: < 10%
- Wind Speed: < 15 km/h
- Weather Condition: Clear

### Hiking
- Temperature: 10-25°C (ideal)
- Precipitation: < 10%
- Wind Speed: < 15 km/h
- Weather Condition: Clear

## API Documentation

Access the Swagger UI at: http://localhost:8080/swagger-ui.html

## Testing

You can use the following Postman collection to test the APIs:

1. Create Test Events:
   - Cricket Tournament in Mumbai
   - Wedding in Goa
   - Hiking Trip in Lonavala

2. Test Weather Integration:
   - Get weather for each event location
   - Check weather suitability
   - Get alternative dates if needed

## Error Handling

The application handles various error scenarios:
- Invalid event data
- Weather API failures
- Database errors
- Invalid dates or locations

## Caching

Weather data is cached for 3 hours to:
- Reduce API calls
- Improve response time
- Handle API rate limits 