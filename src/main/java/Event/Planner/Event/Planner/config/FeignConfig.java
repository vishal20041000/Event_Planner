package Event.Planner.Event.Planner.config;

import Event.Planner.Event.Planner.exception.WeatherApiException;
import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Configuration
public class FeignConfig {

    @Value("${openweathermap.api.key}")
    private String apiKey;

    @Value("${openweathermap.api.units}")
    private String units;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.query("appid", apiKey);
            requestTemplate.query("units", units);
            requestTemplate.header("Accept", "application/json");
        };
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            switch (response.status()) {
                case 401:
                    return new WeatherApiException("Invalid API key. Please check your OpenWeatherMap API key.");
                case 404:
                    return new WeatherApiException("Location not found. Please check the location name and try again.");
                case 429:
                    return new WeatherApiException("API rate limit exceeded. Please try again later.");
                default:
                    return new WeatherApiException("Error calling weather API: " + response.reason());
            }
        };
    }
} 