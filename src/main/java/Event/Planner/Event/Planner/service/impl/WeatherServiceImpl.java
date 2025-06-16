package Event.Planner.Event.Planner.service.impl;

import Event.Planner.Event.Planner.client.OpenWeatherMapClient;
import Event.Planner.Event.Planner.model.WeatherData;
import Event.Planner.Event.Planner.repository.WeatherDataRepository;
import Event.Planner.Event.Planner.service.WeatherService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class WeatherServiceImpl implements WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherServiceImpl.class);

    private final OpenWeatherMapClient weatherClient;
    private final WeatherDataRepository weatherDataRepository;
    private final ObjectMapper objectMapper;

    @Value("${openweathermap.api.key}")
    private String apiKey;

    @Value("${openweathermap.api.units}")
    private String units;

    public WeatherServiceImpl(OpenWeatherMapClient weatherClient, 
                            WeatherDataRepository weatherDataRepository,
                            ObjectMapper objectMapper) {
        this.weatherClient = weatherClient;
        this.weatherDataRepository = weatherDataRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Cacheable(value = "weather", key = "#location + '-' + #date")
    public WeatherData getWeatherForLocationAndDate(String location, LocalDateTime date) {
        // Check cache first
        WeatherData cachedData = weatherDataRepository
            .findFirstByLocationAndForecastDateOrderByCreatedAtDesc(location, date);
        
        if (cachedData != null && 
            ChronoUnit.HOURS.between(cachedData.getCreatedAt(), LocalDateTime.now()) < 3) {
            return cachedData;
        }

        // Fetch from API
        String response = weatherClient.getCurrentWeather(location, apiKey, units);
        WeatherData weatherData = parseWeatherResponse(response, location, date);
        
        // Save to database
        return weatherDataRepository.save(weatherData);
    }

    @Override
    public List<WeatherData> getWeatherForecast(String location, LocalDateTime startDate, LocalDateTime endDate) {
        String response = weatherClient.getWeatherForecast(location, apiKey, units, 40);
        return parseForecastResponse(response, location, startDate, endDate);
    }

    @Override
    public String calculateWeatherSuitability(WeatherData weatherData, String eventType) {
        double score = 0;
        
        switch (eventType.toLowerCase()) {
            case "cricket":
            case "sports":
                score = calculateSportsScore(weatherData);
                break;
            case "wedding":
            case "formal":
                score = calculateWeddingScore(weatherData);
                break;
            case "hiking":
                score = calculateHikingScore(weatherData);
                break;
            default:
                score = calculateGenericScore(weatherData);
        }

        if (score >= 80) return "Good";
        if (score >= 50) return "Okay";
        return "Poor";
    }

    @Override
    public List<LocalDateTime> findAlternativeDates(String location, LocalDateTime originalDate, String eventType) {
        List<LocalDateTime> alternatives = new ArrayList<>();
        LocalDateTime startDate = originalDate.minusDays(3);
        LocalDateTime endDate = originalDate.plusDays(3);
        
        List<WeatherData> forecast = getWeatherForecast(location, startDate, endDate);
        
        for (WeatherData weather : forecast) {
            String suitability = calculateWeatherSuitability(weather, eventType);
            if (suitability.equals("Good") && !weather.getForecastDate().equals(originalDate)) {
                alternatives.add(weather.getForecastDate());
            }
        }
        
        return alternatives;
    }

    private WeatherData parseWeatherResponse(String response, String location, LocalDateTime date) {
        try {
            JsonNode root = objectMapper.readTree(response);
            WeatherData weatherData = new WeatherData();
            weatherData.setLocation(location);
            weatherData.setForecastDate(date);
            weatherData.setTemperature(root.path("main").path("temp").asDouble());
            weatherData.setPrecipitation(root.path("rain").path("1h").asDouble(0.0));
            weatherData.setWindSpeed(root.path("wind").path("speed").asDouble());
            weatherData.setWeatherCondition(root.path("weather").get(0).path("main").asText());
            weatherData.setCreatedAt(LocalDateTime.now());
            return weatherData;
        } catch (Exception e) {
            throw new RuntimeException("Error parsing weather response: " + e.getMessage(), e);
        }
    }

    private List<WeatherData> parseForecastResponse(String response, String location, 
                                                  LocalDateTime startDate, LocalDateTime endDate) {
        List<WeatherData> forecast = new ArrayList<>();
        try {
            if (response == null || response.trim().isEmpty()) {
                log.warn("Empty response received from weather API for location: {}", location);
                return forecast;
            }

            JsonNode root = objectMapper.readTree(response);
            JsonNode list = root.path("list");
            
            if (!list.isArray() || list.size() == 0) {
                log.warn("No forecast data available for location: {}", location);
                return forecast;
            }
            
            for (JsonNode item : list) {
                LocalDateTime forecastDate = LocalDateTime.ofEpochSecond(
                    item.path("dt").asLong(), 0, java.time.ZoneOffset.UTC);
                
                if (forecastDate.isAfter(startDate) && forecastDate.isBefore(endDate)) {
                    WeatherData weatherData = new WeatherData();
                    weatherData.setLocation(location);
                    weatherData.setForecastDate(forecastDate);
                    weatherData.setTemperature(item.path("main").path("temp").asDouble());
                    weatherData.setPrecipitation(item.path("rain").path("3h").asDouble(0.0));
                    weatherData.setWindSpeed(item.path("wind").path("speed").asDouble());
                    weatherData.setWeatherCondition(item.path("weather").get(0).path("main").asText());
                    forecast.add(weatherData);
                }
            }
        } catch (Exception e) {
            log.error("Error parsing forecast response for location {}: {}", location, e.getMessage());
            throw new RuntimeException("Error parsing forecast response", e);
        }
        return forecast;
    }

    private double calculateSportsScore(WeatherData weather) {
        double score = 0;
        // Temperature (15-30°C is ideal)
        if (weather.getTemperature() >= 15 && weather.getTemperature() <= 30) {
            score += 30;
        } else if (weather.getTemperature() >= 10 && weather.getTemperature() <= 35) {
            score += 15;
        }
        
        // Precipitation (less than 20% is good)
        if (weather.getPrecipitation() < 0.2) {
            score += 25;
        } else if (weather.getPrecipitation() < 0.5) {
            score += 10;
        }
        
        // Wind (less than 20 km/h is good)
        if (weather.getWindSpeed() < 5.5) {
            score += 20;
        } else if (weather.getWindSpeed() < 11.1) {
            score += 10;
        }
        
        // Weather condition
        if (weather.getWeatherCondition().equals("Clear") || 
            weather.getWeatherCondition().equals("Clouds")) {
            score += 25;
        }
        
        return score;
    }

    private double calculateWeddingScore(WeatherData weather) {
        double score = 0;
        // Temperature (18-28°C is ideal)
        if (weather.getTemperature() >= 18 && weather.getTemperature() <= 28) {
            score += 30;
        } else if (weather.getTemperature() >= 15 && weather.getTemperature() <= 32) {
            score += 15;
        }
        
        // Precipitation (less than 10% is good)
        if (weather.getPrecipitation() < 0.1) {
            score += 30;
        } else if (weather.getPrecipitation() < 0.3) {
            score += 10;
        }
        
        // Wind (less than 15 km/h is good)
        if (weather.getWindSpeed() < 4.2) {
            score += 25;
        } else if (weather.getWindSpeed() < 8.3) {
            score += 10;
        }
        
        // Weather condition
        if (weather.getWeatherCondition().equals("Clear")) {
            score += 15;
        }
        
        return score;
    }

    private double calculateHikingScore(WeatherData weather) {
        double score = 0;
        // Temperature (10-25°C is ideal)
        if (weather.getTemperature() >= 10 && weather.getTemperature() <= 25) {
            score += 30;
        } else if (weather.getTemperature() >= 5 && weather.getTemperature() <= 30) {
            score += 15;
        }
        
        // Precipitation (less than 10% is good)
        if (weather.getPrecipitation() < 0.1) {
            score += 30;
        } else if (weather.getPrecipitation() < 0.3) {
            score += 10;
        }
        
        // Wind (less than 15 km/h is good)
        if (weather.getWindSpeed() < 4.2) {
            score += 20;
        } else if (weather.getWindSpeed() < 8.3) {
            score += 10;
        }
        
        // Weather condition
        if (weather.getWeatherCondition().equals("Clear")) {
            score += 20;
        }
        
        return score;
    }

    private double calculateGenericScore(WeatherData weather) {
        double score = 0;
        // Temperature (15-28°C is ideal)
        if (weather.getTemperature() >= 15 && weather.getTemperature() <= 28) {
            score += 30;
        } else if (weather.getTemperature() >= 10 && weather.getTemperature() <= 32) {
            score += 15;
        }
        
        // Precipitation (less than 20% is good)
        if (weather.getPrecipitation() < 0.2) {
            score += 30;
        } else if (weather.getPrecipitation() < 0.4) {
            score += 15;
        }
        
        // Wind (less than 20 km/h is good)
        if (weather.getWindSpeed() < 5.5) {
            score += 20;
        } else if (weather.getWindSpeed() < 11.1) {
            score += 10;
        }
        
        // Weather condition
        if (weather.getWeatherCondition().equals("Clear") || 
            weather.getWeatherCondition().equals("Clouds")) {
            score += 20;
        }
        
        return score;
    }
} 