package Event.Planner.Event.Planner.service;

import Event.Planner.Event.Planner.model.Event;
import java.util.List;

public interface EventService {
    Event createEvent(Event event);
    List<Event> getAllEvents();
    Event getEventById(Long id);
    Event updateEvent(Long id, Event event);
    void deleteEvent(Long id);
    String checkEventWeatherSuitability(Long eventId);
    List<String> getAlternativeDates(Long eventId);
} 