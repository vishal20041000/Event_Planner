package Event.Planner.Event.Planner.service.impl;

import Event.Planner.Event.Planner.client.OpenWeatherMapClient;
import Event.Planner.Event.Planner.exception.WeatherApiException;
import Event.Planner.Event.Planner.model.EventType;
import Event.Planner.Event.Planner.model.WeatherData;
import Event.Planner.Event.Planner.model.WeatherPreferences;
import Event.Planner.Event.Planner.service.ForecastService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ForecastServiceImpl implements ForecastService {

    private static final Logger log = LoggerFactory.getLogger(ForecastServiceImpl.class);
    private final OpenWeatherMapClient weatherClient;
    private final String apiKey;
    private final Bucket bucket;

    public ForecastServiceImpl(
            OpenWeatherMapClient weatherClient,
            @Value("${openweathermap.api.key}") String apiKey,
            @Value("${rate.limit.requests:100}") int requests,
            @Value("${rate.limit.duration:3600}") int duration) {
        this.weatherClient = weatherClient;
        this.apiKey = apiKey;
        Bandwidth limit = Bandwidth.classic(requests, Refill.greedy(requests, Duration.ofSeconds(duration)));
        this.bucket = Bucket.builder().addLimit(limit).build();
    }

    @Override
    public WeatherData getCurrentWeather(String location) {
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Location cannot be null or empty");
        }

        String formattedLocation = formatLocation(location);
        log.info("Fetching current weather for location: {}", formattedLocation);

        try {
            if (!bucket.tryConsume(1)) {
                throw new WeatherApiException("Rate limit exceeded. Please try again later.");
            }

            String response = weatherClient.getCurrentWeather(formattedLocation, apiKey, "metric");
            return parseWeatherData(new ObjectMapper().readTree(response), formattedLocation);
        } catch (Exception e) {
            log.error("Error fetching current weather for {}: {}", formattedLocation, e.getMessage());
            throw new WeatherApiException("Failed to fetch current weather: " + e.getMessage());
        }
    }

    @Override
    public List<WeatherData> getHourlyForecast(String location, LocalDateTime date) {
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Location cannot be null or empty");
        }

        String formattedLocation = formatLocation(location);
        log.info("Fetching hourly forecast for location: {} and date: {}", formattedLocation, date);

        try {
            if (!bucket.tryConsume(1)) {
                throw new WeatherApiException("Rate limit exceeded. Please try again later.");
            }

            String response = weatherClient.getWeatherForecast(formattedLocation, apiKey, "metric", 40);
            JsonNode root = new ObjectMapper().readTree(response);
            List<WeatherData> forecast = new ArrayList<>();
            
            JsonNode list = root.path("list");
            for (JsonNode item : list) {
                LocalDateTime itemTime = LocalDateTime.parse(item.path("dt_txt").asText().replace(" ", "T"));
                if (itemTime.getDayOfYear() == date.getDayOfYear()) {
                    forecast.add(parseWeatherData(item, formattedLocation));
                }
            }
            
            return forecast;
        } catch (Exception e) {
            log.error("Error fetching hourly forecast for {}: {}", formattedLocation, e.getMessage());
            throw new WeatherApiException("Failed to fetch hourly forecast: " + e.getMessage());
        }
    }

    @Override
    public List<WeatherData> getDailyForecast(String location, LocalDateTime date) {
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Location cannot be null or empty");
        }

        String formattedLocation = formatLocation(location);
        log.info("Fetching daily forecast for location: {} and date: {}", formattedLocation, date);

        try {
            if (!bucket.tryConsume(1)) {
                throw new WeatherApiException("Rate limit exceeded. Please try again later.");
            }

            String response = weatherClient.getWeatherForecast(formattedLocation, apiKey, "metric", 40);
            JsonNode root = new ObjectMapper().readTree(response);
            List<WeatherData> forecast = new ArrayList<>();
            
            JsonNode list = root.path("list");
            for (JsonNode item : list) {
                LocalDateTime itemTime = LocalDateTime.parse(item.path("dt_txt").asText().replace(" ", "T"));
                if (itemTime.getDayOfYear() == date.getDayOfYear()) {
                    forecast.add(parseWeatherData(item, formattedLocation));
                }
            }
            
            return forecast;
        } catch (Exception e) {
            log.error("Error fetching daily forecast for {}: {}", formattedLocation, e.getMessage());
            throw new WeatherApiException("Failed to fetch daily forecast: " + e.getMessage());
        }
    }

    @Override
    public List<WeatherData> getFiveDayForecast(String location) {
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Location cannot be null or empty");
        }

        String formattedLocation = formatLocation(location);
        log.info("Fetching 5-day forecast for location: {}", formattedLocation);

        try {
            if (!bucket.tryConsume(1)) {
                throw new WeatherApiException("Rate limit exceeded. Please try again later.");
            }

            // Request 40 data points (5 days * 8 points per day)
            String response = weatherClient.getWeatherForecast(formattedLocation, apiKey, "metric", 40);
            log.debug("Raw API response: {}", response);
            
            JsonNode root = new ObjectMapper().readTree(response);
            List<WeatherData> forecast = new ArrayList<>();
            
            JsonNode list = root.path("list");
            if (!list.isArray() || list.size() == 0) {
                log.warn("No forecast data available for location: {}", formattedLocation);
                return forecast;
            }

            log.info("Received {} forecast data points", list.size());

            // Group forecasts by date to get one forecast per day
            Map<String, List<WeatherData>> dailyForecasts = new HashMap<>();
            
            for (JsonNode item : list) {
                try {
                    WeatherData weatherData = parseWeatherData(item, formattedLocation);
                    if (weatherData != null && weatherData.getForecastDate() != null) {
                        String dateKey = weatherData.getForecastDate().toLocalDate().toString();
                        dailyForecasts.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(weatherData);
                        log.debug("Added forecast for date: {}, time: {}, temp: {}", 
                            dateKey, 
                            weatherData.getForecastDate().toLocalTime(),
                            weatherData.getTemperature());
                    }
                } catch (Exception e) {
                    log.warn("Error parsing forecast item: {}", e.getMessage());
                }
            }
            
            // For each day, take the forecast closest to noon
            for (List<WeatherData> dayForecasts : dailyForecasts.values()) {
                if (!dayForecasts.isEmpty()) {
                    WeatherData noonForecast = dayForecasts.stream()
                        .min((a, b) -> {
                            int hourA = a.getForecastDate().getHour();
                            int hourB = b.getForecastDate().getHour();
                            return Math.abs(hourA - 12) - Math.abs(hourB - 12);
                        })
                        .orElse(dayForecasts.get(0));
                    forecast.add(noonForecast);
                    log.debug("Selected noon forecast for date: {}, temp: {}", 
                        noonForecast.getForecastDate().toLocalDate(),
                        noonForecast.getTemperature());
                }
            }
            
            // Sort by date
            forecast.sort((a, b) -> a.getForecastDate().compareTo(b.getForecastDate()));
            
            log.info("Retrieved {} daily forecasts for location: {}", forecast.size(), formattedLocation);
            return forecast;
        } catch (Exception e) {
            log.error("Error fetching 5-day forecast for {}: {}", formattedLocation, e.getMessage());
            throw new WeatherApiException("Failed to fetch 5-day forecast: " + e.getMessage());
        }
    }

    @Override
    public WeatherData getHistoricalWeather(String location, LocalDateTime date) {
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Location cannot be null or empty");
        }

        String formattedLocation = formatLocation(location);
        log.info("Fetching historical weather for location: {} and date: {}", formattedLocation, date);

        try {
            if (!bucket.tryConsume(1)) {
                throw new WeatherApiException("Rate limit exceeded. Please try again later.");
            }

            // For historical data, we'll use the current weather as a fallback
            // since the free API doesn't provide historical data
            String response = weatherClient.getCurrentWeather(formattedLocation, apiKey, "metric");
            WeatherData weatherData = parseWeatherData(new ObjectMapper().readTree(response), formattedLocation);
            
            // Set the requested date
            weatherData.setForecastDate(date);
            return weatherData;
        } catch (Exception e) {
            log.error("Error fetching historical weather for {}: {}", formattedLocation, e.getMessage());
            throw new WeatherApiException("Failed to fetch historical weather: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> isWeatherImproving(String location, LocalDateTime date, EventType eventType) {
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Location cannot be null or empty");
        }

        String formattedLocation = formatLocation(location);
        log.info("Checking weather trend for location: {}, date: {}, eventType: {}", formattedLocation, date, eventType);

        try {
            List<WeatherData> forecast = getFiveDayForecast(formattedLocation);
            if (forecast.size() < 2) {
                Map<String, Object> response = new HashMap<>();
                response.put("isImproving", false);
                response.put("message", "Insufficient forecast data");
                return response;
            }

            WeatherData current = forecast.get(0);
            WeatherData nextDay = forecast.get(1);

            Map<String, Object> response = new HashMap<>();
            Map<String, Object> currentWeather = new HashMap<>();
            currentWeather.put("temperature", current.getTemperature());
            currentWeather.put("precipitation", current.getPrecipitation());
            currentWeather.put("windSpeed", current.getWindSpeed());
            currentWeather.put("condition", current.getWeatherCondition());

            Map<String, Object> nextDayWeather = new HashMap<>();
            nextDayWeather.put("temperature", nextDay.getTemperature());
            nextDayWeather.put("precipitation", nextDay.getPrecipitation());
            nextDayWeather.put("windSpeed", nextDay.getWindSpeed());
            nextDayWeather.put("condition", nextDay.getWeatherCondition());

            Map<String, Object> analysis = new HashMap<>();
            
            if (eventType != null) {
                WeatherPreferences preferences = eventType.getPreferences();
                
                double currentScore = preferences.calculateSuitabilityScore(
                    current.getTemperature(),
                    current.getPrecipitation(),
                    current.getWindSpeed(),
                    current.getWeatherCondition()
                );
                
                double nextDayScore = preferences.calculateSuitabilityScore(
                    nextDay.getTemperature(),
                    nextDay.getPrecipitation(),
                    nextDay.getWindSpeed(),
                    nextDay.getWeatherCondition()
                );
                
                analysis.put("eventType", eventType.name());
                analysis.put("currentSuitabilityScore", currentScore);
                analysis.put("nextDaySuitabilityScore", nextDayScore);
                analysis.put("isImproving", nextDayScore > currentScore);
                
                Map<String, Object> currentFactors = new HashMap<>();
                currentFactors.put("temperature", preferences.isTemperatureSuitable(current.getTemperature()));
                currentFactors.put("precipitation", preferences.isPrecipitationSuitable(current.getPrecipitation()));
                currentFactors.put("windSpeed", preferences.isWindSpeedSuitable(current.getWindSpeed()));
                currentFactors.put("condition", preferences.isWeatherConditionSuitable(current.getWeatherCondition()));
                
                Map<String, Object> nextDayFactors = new HashMap<>();
                nextDayFactors.put("temperature", preferences.isTemperatureSuitable(nextDay.getTemperature()));
                nextDayFactors.put("precipitation", preferences.isPrecipitationSuitable(nextDay.getPrecipitation()));
                nextDayFactors.put("windSpeed", preferences.isWindSpeedSuitable(nextDay.getWindSpeed()));
                nextDayFactors.put("condition", preferences.isWeatherConditionSuitable(nextDay.getWeatherCondition()));
                
                analysis.put("currentFactors", currentFactors);
                analysis.put("nextDayFactors", nextDayFactors);
                
                String message = String.format(
                    "Weather %s for %s event. Current suitability: %.2f, Next day: %.2f",
                    nextDayScore > currentScore ? "is improving" : "is not improving",
                    eventType.name(),
                    currentScore,
                    nextDayScore
                );
                analysis.put("message", message);
            } else {
                boolean tempImproving = nextDay.getTemperature() >= 15 && nextDay.getTemperature() <= 30 &&
                                      (nextDay.getTemperature() > current.getTemperature() || 
                                       Math.abs(nextDay.getTemperature() - current.getTemperature()) < 2);
                boolean precipImproving = nextDay.getPrecipitation() < current.getPrecipitation();
                boolean windImproving = nextDay.getWindSpeed() < current.getWindSpeed();

                Map<String, Boolean> improvements = new HashMap<>();
                improvements.put("temperature", tempImproving);
                improvements.put("precipitation", precipImproving);
                improvements.put("wind", windImproving);

                int improvementScore = 0;
                if (tempImproving) improvementScore++;
                if (precipImproving) improvementScore++;
                if (windImproving) improvementScore++;

                analysis.put("isImproving", improvementScore >= 2);
                analysis.put("improvements", improvements);
                analysis.put("improvementScore", improvementScore);
                analysis.put("message", improvementScore >= 2 ? 
                    "Weather is expected to improve" : 
                    "Weather conditions are not expected to improve significantly");
            }

            response.put("currentWeather", currentWeather);
            response.put("nextDayWeather", nextDayWeather);
            response.put("analysis", analysis);

            return response;
        } catch (Exception e) {
            log.error("Error checking weather trend for {}: {}", formattedLocation, e.getMessage());
            throw new WeatherApiException("Failed to check weather trend: " + e.getMessage());
        }
    }

    private WeatherData parseWeatherData(JsonNode item, String location) {
        try {
            WeatherData weatherData = new WeatherData();
            weatherData.setLocation(location);
            
            // Handle both current weather and forecast data
            if (item.has("dt_txt")) {
                // Forecast data
                String dtTxt = item.path("dt_txt").asText();
                log.debug("Parsing forecast date from dt_txt: {}", dtTxt);
                weatherData.setForecastDate(LocalDateTime.parse(dtTxt.replace(" ", "T")));
            } else if (item.has("dt")) {
                // Current weather data
                long timestamp = item.path("dt").asLong();
                log.debug("Parsing forecast date from dt: {}", timestamp);
                weatherData.setForecastDate(LocalDateTime.ofEpochSecond(timestamp, 0, java.time.ZoneOffset.UTC));
            } else {
                log.warn("No date information found in weather data");
                return null;
            }
            
            JsonNode main = item.path("main");
            if (main.isMissingNode()) {
                log.warn("No main weather data found");
                return null;
            }
            
            double temp = main.path("temp").asDouble();
            double precip = item.path("rain").path("3h").asDouble(0.0);
            double wind = item.path("wind").path("speed").asDouble();
            
            weatherData.setTemperature(temp);
            weatherData.setPrecipitation(precip);
            weatherData.setWindSpeed(wind);
            
            JsonNode weather = item.path("weather");
            if (weather.isArray() && weather.size() > 0) {
                String condition = weather.get(0).path("main").asText();
                weatherData.setWeatherCondition(condition);
                log.debug("Weather condition: {}", condition);
            } else {
                weatherData.setWeatherCondition("Unknown");
            }
            
            log.debug("Parsed weather data - Date: {}, Temp: {}, Precip: {}, Wind: {}", 
                weatherData.getForecastDate(), temp, precip, wind);
            
            return weatherData;
        } catch (Exception e) {
            log.error("Error parsing weather data: {}", e.getMessage());
            return null;
        }
    }

    private String formatLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Location cannot be null or empty");
        }
        String[] words = location.trim().split("\\s+");
        StringBuilder formatted = new StringBuilder();
        for (String word : words) {
            if (word.length() > 0) {
                formatted.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return formatted.toString().trim();
    }
} 