package Event.Planner.Event.Planner.controller;

import Event.Planner.Event.Planner.model.EventType;
import Event.Planner.Event.Planner.model.WeatherData;
import Event.Planner.Event.Planner.model.WeatherPreferences;
import Event.Planner.Event.Planner.service.ForecastService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/weather")
@Tag(name = "Weather Forecast", description = "APIs for weather forecasting and analysis")
public class ForecastController {

    private final ForecastService forecastService;
    private static final Logger log = LoggerFactory.getLogger(ForecastController.class);

    public ForecastController(ForecastService forecastService) {
        this.forecastService = forecastService;
    }

    @GetMapping("/current/{location}")
    @Operation(summary = "Get current weather for a location")
    public ResponseEntity<WeatherData> getCurrentWeather(
            @Parameter(description = "Location name (e.g., Mumbai, Delhi)") 
            @PathVariable String location) {
        log.info("Fetching current weather for location: {}", location);
        return ResponseEntity.ok(forecastService.getCurrentWeather(location));
    }

    @GetMapping("/historical/{location}")
    @Operation(summary = "Get historical weather for a location and date")
    public ResponseEntity<WeatherData> getHistoricalWeather(
            @Parameter(description = "Location name (e.g., Mumbai, Delhi)") 
            @PathVariable String location,
            @Parameter(description = "Date and time in ISO format (e.g., 2024-03-16T09:00:00)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        log.info("Fetching historical weather for location: {} and date: {}", location, date);
        return ResponseEntity.ok(forecastService.getHistoricalWeather(location, date));
    }

    @GetMapping("/forecast/{location}")
    @Operation(summary = "Get weather forecast for a location within a date range")
    public ResponseEntity<List<WeatherData>> getForecast(
            @Parameter(description = "Location name (e.g., Mumbai, Delhi)") 
            @PathVariable String location,
            @Parameter(description = "Start date and time in ISO format (e.g., 2024-03-16T09:00:00)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date and time in ISO format (e.g., 2024-03-20T09:00:00)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Fetching forecast for location: {} between {} and {}", location, startDate, endDate);
        try {
            List<WeatherData> forecast = forecastService.getFiveDayForecast(location);
            
            // Filter forecast by date range
            List<WeatherData> filteredForecast = forecast.stream()
                .filter(w -> !w.getForecastDate().isBefore(startDate) && !w.getForecastDate().isAfter(endDate))
                .collect(Collectors.toList());
                
            log.info("Returning {} forecasts for location: {}", filteredForecast.size(), location);
            return ResponseEntity.ok(filteredForecast);
        } catch (Exception e) {
            log.error("Error fetching forecast for location {}: {}", location, e.getMessage());
            throw new RuntimeException("Failed to fetch forecast: " + e.getMessage());
        }
    }

    @GetMapping("/hourly/{location}")
    @Operation(summary = "Get hourly forecast for a location")
    public ResponseEntity<List<WeatherData>> getHourlyForecast(
            @Parameter(description = "Location name (e.g., Mumbai, Delhi)") 
            @PathVariable String location,
            @Parameter(description = "Date and time in ISO format (e.g., 2024-03-16T09:00:00)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        log.info("Fetching hourly forecast for location: {} and date: {}", location, date);
        return ResponseEntity.ok(forecastService.getHourlyForecast(location, date != null ? date : LocalDateTime.now()));
    }

    @GetMapping("/daily/{location}")
    @Operation(summary = "Get daily forecast for a location")
    public ResponseEntity<List<WeatherData>> getDailyForecast(
            @Parameter(description = "Location name (e.g., Mumbai, Delhi)") 
            @PathVariable String location,
            @Parameter(description = "Date and time in ISO format (e.g., 2024-03-16T09:00:00)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        log.info("Fetching daily forecast for location: {} and date: {}", location, date);
        return ResponseEntity.ok(forecastService.getDailyForecast(location, date != null ? date : LocalDateTime.now()));
    }

    @GetMapping("/trend/{location}")
    @Operation(summary = "Check if weather is improving for a specific event type")
    public ResponseEntity<Map<String, Object>> isWeatherImproving(
            @Parameter(description = "Location name (e.g., Mumbai, Delhi)") 
            @PathVariable String location,
            @Parameter(description = "Date and time in ISO format (e.g., 2024-03-16T09:00:00)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date,
            @Parameter(description = "Type of event (e.g., WEDDING, CORPORATE, OUTDOOR_SPORTS)") 
            @RequestParam(required = false) EventType eventType) {
        log.info("Checking weather trend for location: {}, date: {}, eventType: {}", location, date, eventType);
        return ResponseEntity.ok(forecastService.isWeatherImproving(location, date != null ? date : LocalDateTime.now(), eventType));
    }
} 