package Event.Planner.Event.Planner.service;

import Event.Planner.Event.Planner.model.Venue;
import Event.Planner.Event.Planner.model.WeatherData;
import Event.Planner.Event.Planner.repository.VenueRepository;
import Event.Planner.Event.Planner.service.impl.RecommendationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private VenueRepository venueRepository;

    @Mock
    private WeatherService weatherService;

    @InjectMocks
    private RecommendationServiceImpl recommendationService;

    private Venue testVenue;
    private WeatherData testWeatherData;

    @BeforeEach
    void setUp() {
        testVenue = new Venue();
        testVenue.setId(1L);
        testVenue.setName("Test Venue");
        testVenue.setLocation("Test Location");
        testVenue.setCapacity(100);
        testVenue.setPricePerDay(1000.0f);
        testVenue.setAmenities(Arrays.asList("Indoor Space", "Outdoor Space"));

        testWeatherData = new WeatherData();
        testWeatherData.setLocation("Test Location");
        testWeatherData.setTemperature(25.0);
        testWeatherData.setPrecipitation(0.1);
        testWeatherData.setWindSpeed(5.0);
        testWeatherData.setWeatherCondition("Clear");
    }

    @Test
    void getWeatherBasedRecommendations_Success() {
        List<Venue> venues = Arrays.asList(testVenue);
        when(venueRepository.findAll()).thenReturn(venues);
        when(weatherService.getWeatherForLocationAndDate(any(), any())).thenReturn(testWeatherData);

        List<Venue> recommendations = recommendationService.getWeatherBasedRecommendations(
            "Test Location",
            LocalDateTime.now()
        );

        assertNotNull(recommendations);
        assertFalse(recommendations.isEmpty());
        assertEquals(1, recommendations.size());
        verify(venueRepository, times(1)).findAll();
        verify(weatherService, times(1)).getWeatherForLocationAndDate(any(), any());
    }

    @Test
    void getVenueBasedRecommendations_Success() {
        List<Venue> venues = Arrays.asList(testVenue);
        when(venueRepository.findAll()).thenReturn(venues);
        when(weatherService.getWeatherForLocationAndDate(any(), any())).thenReturn(testWeatherData);
        when(weatherService.calculateWeatherSuitability(any(), any())).thenReturn("Good");

        List<Venue> recommendations = recommendationService.getVenueBasedRecommendations(
            "Test Location",
            50,
            LocalDateTime.now()
        );

        assertNotNull(recommendations);
        assertFalse(recommendations.isEmpty());
        assertEquals(1, recommendations.size());
        verify(venueRepository, times(1)).findAll();
        verify(weatherService, times(1)).getWeatherForLocationAndDate(any(), any());
        verify(weatherService, times(1)).calculateWeatherSuitability(any(), any());
    }

    @Test
    void getWeatherBasedRecommendations_NoSuitableVenues() {
        testWeatherData.setWeatherCondition("Rain");
        testWeatherData.setPrecipitation(0.8);
        List<Venue> venues = Arrays.asList(testVenue);
        when(venueRepository.findAll()).thenReturn(venues);
        when(weatherService.getWeatherForLocationAndDate(any(), any())).thenReturn(testWeatherData);

        List<Venue> recommendations = recommendationService.getWeatherBasedRecommendations(
            "Test Location",
            LocalDateTime.now()
        );

        assertNotNull(recommendations);
        assertTrue(recommendations.isEmpty());
    }

    @Test
    void getVenueBasedRecommendations_InsufficientCapacity() {
        List<Venue> venues = Arrays.asList(testVenue);
        when(venueRepository.findAll()).thenReturn(venues);

        List<Venue> recommendations = recommendationService.getVenueBasedRecommendations(
            "Test Location",
            200,
            LocalDateTime.now()
        );

        assertNotNull(recommendations);
        assertTrue(recommendations.isEmpty());
    }
} 