package Event.Planner.Event.Planner.service;

import Event.Planner.Event.Planner.model.WeatherData;
import java.time.LocalDateTime;
import java.util.List;

public interface WeatherService {
    WeatherData getWeatherForLocationAndDate(String location, LocalDateTime date);
    List<WeatherData> getWeatherForecast(String location, LocalDateTime startDate, LocalDateTime endDate);
    String calculateWeatherSuitability(WeatherData weatherData, String eventType);
    List<LocalDateTime> findAlternativeDates(String location, LocalDateTime originalDate, String eventType);
} 