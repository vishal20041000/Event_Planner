package Event.Planner.Event.Planner.service;

import Event.Planner.Event.Planner.model.EventType;
import Event.Planner.Event.Planner.model.Venue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface RecommendationService {
    Map<String, Object> getWeatherRecommendations(String location, LocalDateTime date, EventType eventType);
    List<Venue> getVenueRecommendations(String location, Integer capacity, EventType eventType);
    List<Map<String, Object>> getEventRecommendations(String location, LocalDateTime date);
} 