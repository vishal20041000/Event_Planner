package Event.Planner.Event.Planner.controller;

import Event.Planner.Event.Planner.model.EventType;
import Event.Planner.Event.Planner.model.Venue;
import Event.Planner.Event.Planner.model.WeatherData;
import Event.Planner.Event.Planner.service.RecommendationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/recommendations")
public class RecommendationController {

    private static final Logger log = LoggerFactory.getLogger(RecommendationController.class);
    private final RecommendationService recommendationService;

    @Autowired
    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/weather")
    public ResponseEntity<Map<String, Object>> getWeatherRecommendations(
            @RequestParam String location,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @RequestParam(required = false) EventType eventType) {
        log.info("Getting weather recommendations for location: {}, date: {}, eventType: {}", 
                location, date, eventType);
        return ResponseEntity.ok(recommendationService.getWeatherRecommendations(location, date, eventType));
    }

    @GetMapping("/venues")
    @Operation(summary = "Get venue recommendations based on location and requirements")
    public ResponseEntity<List<Venue>> getVenueRecommendations(
            @Parameter(description = "Location name (e.g., Mumbai, Delhi)") 
            @RequestParam String location,
            @Parameter(description = "Required capacity") 
            @RequestParam Integer capacity,
            @Parameter(description = "Type of event (e.g., WEDDING, CORPORATE, OUTDOOR_SPORTS)") 
            @RequestParam(required = false) EventType eventType) {
        log.info("Getting venue recommendations for location: {}, capacity: {}, eventType: {}", 
                location, capacity, eventType);
        return ResponseEntity.ok(recommendationService.getVenueRecommendations(location, capacity, eventType));
    }

    @GetMapping("/events")
    @Operation(summary = "Get event recommendations based on weather")
    public ResponseEntity<List<Map<String, Object>>> getEventRecommendations(
            @Parameter(description = "Location name (e.g., Mumbai, Delhi)") 
            @RequestParam String location,
            @Parameter(description = "Date and time in ISO format (e.g., 2024-03-16T09:00:00)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        log.info("Getting event recommendations for location: {} and date: {}", location, date);
        return ResponseEntity.ok(recommendationService.getEventRecommendations(location, date));
    }
} 