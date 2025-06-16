package Event.Planner.Event.Planner.service;

import Event.Planner.Event.Planner.model.Venue;
import java.util.List;

public interface VenueService {
    Venue createVenue(Venue venue);
    List<Venue> getAllVenues();
    Venue getVenueById(Long id);
    Venue updateVenue(Venue venue);
    void deleteVenue(Long id);
} 