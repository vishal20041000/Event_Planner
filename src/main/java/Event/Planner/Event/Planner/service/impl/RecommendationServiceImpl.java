package Event.Planner.Event.Planner.service.impl;

import Event.Planner.Event.Planner.model.EventType;
import Event.Planner.Event.Planner.model.Venue;
import Event.Planner.Event.Planner.model.WeatherData;
import Event.Planner.Event.Planner.model.WeatherPreferences;
import Event.Planner.Event.Planner.repository.VenueRepository;
import Event.Planner.Event.Planner.service.ForecastService;
import Event.Planner.Event.Planner.service.RecommendationService;
import Event.Planner.Event.Planner.service.VenueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    private static final Logger log = LoggerFactory.getLogger(RecommendationServiceImpl.class);
    private final ForecastService forecastService;
    private final VenueService venueService;
    private final VenueRepository venueRepository;

    @Autowired
    public RecommendationServiceImpl(ForecastService forecastService, VenueService venueService, VenueRepository venueRepository) {
        this.forecastService = forecastService;
        this.venueService = venueService;
        this.venueRepository = venueRepository;
    }

    @Override
    public Map<String, Object> getWeatherRecommendations(String location, LocalDateTime date, EventType eventType) {
        log.info("Generating weather recommendations for location: {}, date: {}, eventType: {}", 
                location, date, eventType);

        Map<String, Object> response = new HashMap<>();
        List<WeatherData> forecast = forecastService.getFiveDayForecast(location);
        
        if (forecast.isEmpty()) {
            response.put("message", "No weather data available for recommendations");
            return response;
        }

        // Get weather data for the specified date
        WeatherData targetDateWeather = forecast.stream()
                .filter(w -> w.getForecastDate() != null && 
                           w.getForecastDate().toLocalDate().equals(date.toLocalDate()))
                .findFirst()
                .orElseGet(() -> {
                    // If no matching date found, use the first forecast entry
                    WeatherData firstForecast = forecast.get(0);
                    if (firstForecast.getForecastDate() == null) {
                        firstForecast.setForecastDate(date);
                    }
                    return firstForecast;
                });

        // Analyze weather conditions
        Map<String, Object> weatherAnalysis = new HashMap<>();
        weatherAnalysis.put("temperature", targetDateWeather.getTemperature());
        weatherAnalysis.put("precipitation", targetDateWeather.getPrecipitation());
        weatherAnalysis.put("windSpeed", targetDateWeather.getWindSpeed());
        weatherAnalysis.put("condition", targetDateWeather.getWeatherCondition());

        // Get event-specific recommendations
        if (eventType != null) {
            WeatherPreferences preferences = eventType.getPreferences();
            double suitabilityScore = preferences.calculateSuitabilityScore(
                targetDateWeather.getTemperature(),
                targetDateWeather.getPrecipitation(),
                targetDateWeather.getWindSpeed(),
                targetDateWeather.getWeatherCondition()
            );

            Map<String, Object> eventRecommendations = new HashMap<>();
            eventRecommendations.put("suitabilityScore", suitabilityScore);
            eventRecommendations.put("isSuitable", suitabilityScore >= 0.7);
            eventRecommendations.put("recommendations", generateEventSpecificRecommendations(
                eventType, targetDateWeather, suitabilityScore));

            response.put("eventType", eventType.name());
            response.put("eventRecommendations", eventRecommendations);
        }

        // Get general recommendations
        response.put("weatherAnalysis", weatherAnalysis);
        response.put("generalRecommendations", generateGeneralRecommendations(targetDateWeather));
        
        return response;
    }

    @Override
    public List<Venue> getVenueRecommendations(String location, Integer capacity, EventType eventType) {
        log.info("Getting venue recommendations for location: {}, capacity: {}, eventType: {}", 
                location, capacity, eventType);

        List<Venue> venues = new ArrayList<>();
        try {
            // Get venues from repository
            List<Venue> allVenues = venueRepository.findAll();
            
            // Filter venues based on location and capacity
            venues = allVenues.stream()
                .filter(venue -> matchesLocation(venue, location))
                .filter(venue -> venue.getCapacity() >= capacity)
                .collect(Collectors.toList());

            // If no venues found, add some default recommendations
            if (venues.isEmpty()) {
                venues.add(createDefaultVenue(location, "Grand Ballroom", capacity, "Indoor", "Luxury venue with modern amenities"));
                venues.add(createDefaultVenue(location, "Garden Pavilion", capacity, "Outdoor", "Beautiful outdoor space with natural surroundings"));
                venues.add(createDefaultVenue(location, "Conference Center", capacity, "Indoor", "Professional setting for corporate events"));
            }

            // If event type is specified, prioritize venues based on event type
            if (eventType != null) {
                venues.sort((v1, v2) -> {
                    boolean v1Suitable = isVenueSuitableForEventType(v1, eventType);
                    boolean v2Suitable = isVenueSuitableForEventType(v2, eventType);
                    return Boolean.compare(v2Suitable, v1Suitable);
                });
            }

        } catch (Exception e) {
            log.error("Error getting venue recommendations: {}", e.getMessage());
            // Add default venues in case of error
            venues.add(createDefaultVenue(location, "Emergency Venue", capacity, "Indoor", "Backup venue option"));
        }

        return venues;
    }

    @Override
    public List<Map<String, Object>> getEventRecommendations(String location, LocalDateTime date) {
        log.info("Generating event recommendations for location: {} and date: {}", location, date);

        List<Map<String, Object>> recommendations = new ArrayList<>();
        try {
            WeatherData weather = forecastService.getHistoricalWeather(location, date);

            // Generate recommendations based on weather conditions
            if (weather.getTemperature() >= 20 && weather.getTemperature() <= 30 && 
                weather.getPrecipitation() < 0.1 && weather.getWindSpeed() < 15) {
                // Good weather for outdoor events
                addEventRecommendation(recommendations, "Outdoor Wedding", 
                    "Perfect weather for an outdoor ceremony", 0.9);
                addEventRecommendation(recommendations, "Corporate Team Building", 
                    "Great conditions for outdoor activities", 0.85);
                addEventRecommendation(recommendations, "Sports Tournament", 
                    "Ideal weather for outdoor sports", 0.8);
            } else if (weather.getPrecipitation() > 0.1 || weather.getWindSpeed() >= 15) {
                // Indoor event recommendations
                addEventRecommendation(recommendations, "Indoor Conference", 
                    "Weather suggests indoor venue", 0.9);
                addEventRecommendation(recommendations, "Exhibition", 
                    "Protected environment recommended", 0.85);
                addEventRecommendation(recommendations, "Indoor Sports Event", 
                    "Consider indoor facilities", 0.8);
            }

            // Add weather information to recommendations
            Map<String, Object> weatherInfo = new HashMap<>();
            weatherInfo.put("temperature", weather.getTemperature());
            weatherInfo.put("precipitation", weather.getPrecipitation());
            weatherInfo.put("windSpeed", weather.getWindSpeed());
            weatherInfo.put("condition", weather.getWeatherCondition());
            
            Map<String, Object> weatherRecommendation = new HashMap<>();
            weatherRecommendation.put("type", "weather_info");
            weatherRecommendation.put("data", weatherInfo);
            recommendations.add(weatherRecommendation);

        } catch (Exception e) {
            log.error("Error generating event recommendations: {}", e.getMessage());
            Map<String, Object> errorRecommendation = new HashMap<>();
            errorRecommendation.put("type", "error");
            errorRecommendation.put("message", "Unable to generate recommendations due to weather service error");
            recommendations.add(errorRecommendation);
        }

        return recommendations;
    }

    private boolean matchesLocation(Venue venue, String location) {
        if (venue.getLocation() == null || location == null) {
            return false;
        }
        return venue.getLocation().toLowerCase().contains(location.toLowerCase());
    }

    private boolean isVenueSuitableForEventType(Venue venue, EventType eventType) {
        switch (eventType) {
            case WEDDING:
                return venue.getType().equalsIgnoreCase("Indoor") || 
                       venue.getType().equalsIgnoreCase("Outdoor");
            case CORPORATE:
                return venue.getType().equalsIgnoreCase("Indoor") && 
                       venue.getCapacity() >= 100;
            case OUTDOOR_SPORTS:
                return venue.getType().equalsIgnoreCase("Outdoor") && 
                       venue.getCapacity() >= 200;
            default:
                return true;
        }
    }

    private List<String> generateEventSpecificRecommendations(EventType eventType, 
            WeatherData weather, double suitabilityScore) {
        List<String> recommendations = new ArrayList<>();
        
        switch (eventType) {
            case WEDDING:
                if (weather.getPrecipitation() > 0.1) {
                    recommendations.add("Consider indoor ceremony with outdoor photo opportunities");
                }
                if (weather.getWindSpeed() > 10) {
                    recommendations.add("Secure decorations and consider wind protection");
                }
                break;
            case CORPORATE:
                if (weather.getTemperature() > 25) {
                    recommendations.add("Ensure proper air conditioning");
                }
                if (weather.getPrecipitation() > 0.1) {
                    recommendations.add("Provide covered parking and entrance");
                }
                break;
            case OUTDOOR_SPORTS:
                if (weather.getPrecipitation() > 0.1) {
                    recommendations.add("Consider indoor alternative venue");
                }
                if (weather.getWindSpeed() > 15) {
                    recommendations.add("Wind may affect certain sports activities");
                }
                break;
            // Add more event types as needed
        }
        
        return recommendations;
    }

    private List<String> generateGeneralRecommendations(WeatherData weather) {
        List<String> recommendations = new ArrayList<>();
        
        if (weather.getTemperature() > 30) {
            recommendations.add("Ensure proper cooling and hydration");
        } else if (weather.getTemperature() < 15) {
            recommendations.add("Consider heating arrangements");
        }
        
        if (weather.getPrecipitation() > 0.1) {
            recommendations.add("Provide covered areas and rain protection");
        }
        
        if (weather.getWindSpeed() > 15) {
            recommendations.add("Secure outdoor decorations and structures");
        }
        
        return recommendations;
    }

    private void addEventRecommendation(List<Map<String, Object>> recommendations, 
            String eventType, String description, double suitabilityScore) {
        Map<String, Object> recommendation = new HashMap<>();
        recommendation.put("eventType", eventType);
        recommendation.put("description", description);
        recommendation.put("suitabilityScore", suitabilityScore);
        recommendations.add(recommendation);
    }

    private Venue createDefaultVenue(String location, String name, int capacity, String type, String description) {
        Venue venue = new Venue();
        venue.setName(name);
        venue.setLocation(location);
        venue.setCapacity(capacity);
        venue.setType(type);
        venue.setDescription(description);
        venue.setAvailable(true);
        return venue;
    }
} 