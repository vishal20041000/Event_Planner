package Event.Planner.Event.Planner.service;

import Event.Planner.Event.Planner.model.Event;
import Event.Planner.Event.Planner.model.WeatherData;
import Event.Planner.Event.Planner.repository.EventRepository;
import Event.Planner.Event.Planner.service.impl.EventServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private WeatherService weatherService;

    @InjectMocks
    private EventServiceImpl eventService;

    private Event testEvent;
    private WeatherData testWeatherData;

    @BeforeEach
    void setUp() {
        testEvent = new Event();
        testEvent.setId(1L);
        testEvent.setName("Test Event");
        testEvent.setLocation("Test Location");
        testEvent.setEventDate(LocalDateTime.now());
        testEvent.setEventType("Sports");
        testEvent.setDescription("Test Description");

        testWeatherData = new WeatherData();
        testWeatherData.setLocation("Test Location");
        testWeatherData.setTemperature(25.0);
        testWeatherData.setPrecipitation(0.1);
        testWeatherData.setWindSpeed(5.0);
        testWeatherData.setWeatherCondition("Clear");
    }

    @Test
    void createEvent_Success() {
        when(weatherService.getWeatherForLocationAndDate(any(), any())).thenReturn(testWeatherData);
        when(weatherService.calculateWeatherSuitability(any(), any())).thenReturn("Good");
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        Event createdEvent = eventService.createEvent(testEvent);

        assertNotNull(createdEvent);
        assertEquals(testEvent.getName(), createdEvent.getName());
        assertEquals("Good", createdEvent.getWeatherSuitability());
        verify(eventRepository, times(1)).save(any(Event.class));
        verify(weatherService, times(1)).getWeatherForLocationAndDate(any(), any());
        verify(weatherService, times(1)).calculateWeatherSuitability(any(), any());
    }

    @Test
    void getAllEvents_Success() {
        List<Event> events = Arrays.asList(testEvent);
        when(eventRepository.findAll()).thenReturn(events);

        List<Event> result = eventService.getAllEvents();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(eventRepository, times(1)).findAll();
    }

    @Test
    void getEventById_Success() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        Event result = eventService.getEventById(1L);

        assertNotNull(result);
        assertEquals(testEvent.getId(), result.getId());
        verify(eventRepository, times(1)).findById(1L);
    }

    @Test
    void getEventById_NotFound() {
        when(eventRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> eventService.getEventById(1L));
        verify(eventRepository, times(1)).findById(1L);
    }

    @Test
    void updateEvent_Success() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(weatherService.getWeatherForLocationAndDate(any(), any())).thenReturn(testWeatherData);
        when(weatherService.calculateWeatherSuitability(any(), any())).thenReturn("Good");
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        Event updatedEvent = new Event();
        updatedEvent.setName("Updated Event");
        updatedEvent.setLocation("Updated Location");
        updatedEvent.setEventDate(LocalDateTime.now());
        updatedEvent.setEventType("Wedding");

        Event result = eventService.updateEvent(1L, updatedEvent);

        assertNotNull(result);
        assertEquals(updatedEvent.getName(), result.getName());
        assertEquals(updatedEvent.getLocation(), result.getLocation());
        assertEquals(updatedEvent.getEventType(), result.getEventType());
        assertEquals("Good", result.getWeatherSuitability());
        verify(eventRepository, times(1)).findById(1L);
        verify(eventRepository, times(1)).save(any(Event.class));
        verify(weatherService, times(1)).getWeatherForLocationAndDate(any(), any());
        verify(weatherService, times(1)).calculateWeatherSuitability(any(), any());
    }

    @Test
    void deleteEvent_Success() {
        when(eventRepository.existsById(1L)).thenReturn(true);
        doNothing().when(eventRepository).deleteById(1L);

        eventService.deleteEvent(1L);

        verify(eventRepository, times(1)).existsById(1L);
        verify(eventRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteEvent_NotFound() {
        when(eventRepository.existsById(1L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> eventService.deleteEvent(1L));
        verify(eventRepository, times(1)).existsById(1L);
        verify(eventRepository, never()).deleteById(anyLong());
    }

    @Test
    void checkEventWeatherSuitability_Success() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(weatherService.getWeatherForLocationAndDate(any(), any())).thenReturn(testWeatherData);
        when(weatherService.calculateWeatherSuitability(any(), any())).thenReturn("Good");

        String result = eventService.checkEventWeatherSuitability(1L);

        assertEquals("Good", result);
        verify(eventRepository, times(1)).findById(1L);
        verify(weatherService, times(1)).getWeatherForLocationAndDate(any(), any());
        verify(weatherService, times(1)).calculateWeatherSuitability(any(), any());
    }
} 