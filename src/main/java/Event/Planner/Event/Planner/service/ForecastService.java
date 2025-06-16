package Event.Planner.Event.Planner.service;

import Event.Planner.Event.Planner.model.EventType;
import Event.Planner.Event.Planner.model.WeatherData;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ForecastService {
    WeatherData getCurrentWeather(String location);
    List<WeatherData> getHourlyForecast(String location, LocalDateTime date);
    List<WeatherData> getDailyForecast(String location, LocalDateTime date);
    List<WeatherData> getFiveDayForecast(String location);
    WeatherData getHistoricalWeather(String location, LocalDateTime date);
    Map<String, Object> isWeatherImproving(String location, LocalDateTime date, EventType eventType);
} 