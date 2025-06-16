package Event.Planner.Event.Planner.service.impl;

import Event.Planner.Event.Planner.model.Event;
import Event.Planner.Event.Planner.model.WeatherData;
import Event.Planner.Event.Planner.repository.EventRepository;
import Event.Planner.Event.Planner.service.EventService;
import Event.Planner.Event.Planner.service.WeatherService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final WeatherService weatherService;

    public EventServiceImpl(EventRepository eventRepository, WeatherService weatherService) {
        this.eventRepository = eventRepository;
        this.weatherService = weatherService;
    }

    @Override
    @Transactional
    public Event createEvent(Event event) {
        // Check weather suitability before saving
        WeatherData weatherData = weatherService.getWeatherForLocationAndDate(
            event.getLocation(), event.getEventDate());
        String suitability = weatherService.calculateWeatherSuitability(weatherData, event.getEventType());
        event.setWeatherSuitability(suitability);
        
        return eventRepository.save(event);
    }

    @Override
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @Override
    public Event getEventById(Long id) {
        return eventRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Event not found with id: " + id));
    }

    @Override
    @Transactional
    public Event updateEvent(Long id, Event event) {
        Event existingEvent = getEventById(id);
        
        // Update fields
        existingEvent.setName(event.getName());
        existingEvent.setLocation(event.getLocation());
        existingEvent.setEventDate(event.getEventDate());
        existingEvent.setEventType(event.getEventType());
        existingEvent.setDescription(event.getDescription());
        
        // Update weather suitability
        WeatherData weatherData = weatherService.getWeatherForLocationAndDate(
            event.getLocation(), event.getEventDate());
        String suitability = weatherService.calculateWeatherSuitability(weatherData, event.getEventType());
        existingEvent.setWeatherSuitability(suitability);
        
        return eventRepository.save(existingEvent);
    }

    @Override
    @Transactional
    public void deleteEvent(Long id) {
        if (!eventRepository.existsById(id)) {
            throw new RuntimeException("Event not found with id: " + id);
        }
        eventRepository.deleteById(id);
    }

    @Override
    public String checkEventWeatherSuitability(Long eventId) {
        Event event = getEventById(eventId);
        WeatherData weatherData = weatherService.getWeatherForLocationAndDate(
            event.getLocation(), event.getEventDate());
        return weatherService.calculateWeatherSuitability(weatherData, event.getEventType());
    }

    @Override
    public List<String> getAlternativeDates(Long eventId) {
        Event event = getEventById(eventId);
        List<LocalDateTime> alternativeDates = weatherService.findAlternativeDates(
            event.getLocation(), event.getEventDate(), event.getEventType());
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return alternativeDates.stream()
            .map(date -> date.format(formatter))
            .collect(Collectors.toList());
    }
} 