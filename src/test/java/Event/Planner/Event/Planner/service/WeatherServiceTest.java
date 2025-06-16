package Event.Planner.Event.Planner.service;

import Event.Planner.Event.Planner.client.OpenWeatherMapClient;
import Event.Planner.Event.Planner.model.WeatherData;
import Event.Planner.Event.Planner.repository.WeatherDataRepository;
import Event.Planner.Event.Planner.service.impl.WeatherServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock
    private OpenWeatherMapClient weatherClient;

    @Mock
    private WeatherDataRepository weatherDataRepository;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private WeatherServiceImpl weatherService;

    private WeatherData testWeatherData;

    @BeforeEach
    void setUp() {
        testWeatherData = new WeatherData();
        testWeatherData.setId(1L);
        testWeatherData.setLocation("Test Location");
        testWeatherData.setTemperature(25.0);
        testWeatherData.setPrecipitation(0.0);
        testWeatherData.setWindSpeed(10.0);
        testWeatherData.setWeatherCondition("Clear");
        testWeatherData.setForecastDate(LocalDateTime.now());
    }

    @Test
    void getWeatherForLocationAndDate_Success() {
        when(weatherClient.getCurrentWeather(any())).thenReturn("{}");
        when(weatherDataRepository.save(any(WeatherData.class))).thenReturn(testWeatherData);

        WeatherData result = weatherService.getWeatherForLocationAndDate("Test Location", LocalDateTime.now());

        assertNotNull(result);
        assertEquals(testWeatherData.getLocation(), result.getLocation());
        verify(weatherClient, times(1)).getCurrentWeather(any());
        verify(weatherDataRepository, times(1)).save(any(WeatherData.class));
    }

    @Test
    void calculateWeatherSuitability_Sports_Good() {
        WeatherData weather = new WeatherData();
        weather.setTemperature(25.0);
        weather.setPrecipitation(0.1);
        weather.setWindSpeed(5.0);
        weather.setWeatherCondition("Clear");

        String result = weatherService.calculateWeatherSuitability(weather, "Sports");

        assertEquals("Good", result);
    }

    @Test
    void calculateWeatherSuitability_Wedding_Okay() {
        WeatherData weather = new WeatherData();
        weather.setTemperature(20.0);
        weather.setPrecipitation(0.2);
        weather.setWindSpeed(7.0);
        weather.setWeatherCondition("Clouds");

        String result = weatherService.calculateWeatherSuitability(weather, "Wedding");

        assertEquals("Okay", result);
    }

    @Test
    void calculateWeatherSuitability_Hiking_Poor() {
        WeatherData weather = new WeatherData();
        weather.setTemperature(35.0);
        weather.setPrecipitation(0.8);
        weather.setWindSpeed(20.0);
        weather.setWeatherCondition("Rain");

        String result = weatherService.calculateWeatherSuitability(weather, "Hiking");

        assertEquals("Poor", result);
    }

    @Test
    void getWeatherForecast_Success() {
        when(weatherClient.getWeatherForecast(any())).thenReturn("{}");

        List<WeatherData> result = weatherService.getWeatherForecast(
            "Test Location",
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(5)
        );

        assertNotNull(result);
        verify(weatherClient, times(1)).getWeatherForecast(any());
    }
} 