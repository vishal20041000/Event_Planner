package Event.Planner.Event.Planner.service.impl;

import Event.Planner.Event.Planner.model.Venue;
import Event.Planner.Event.Planner.repository.VenueRepository;
import Event.Planner.Event.Planner.service.VenueService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VenueServiceImpl implements VenueService {

    private static final Logger log = LoggerFactory.getLogger(VenueServiceImpl.class);
    private final VenueRepository venueRepository;

    @Autowired
    public VenueServiceImpl(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    @Override
    public Venue createVenue(Venue venue) {
        log.info("Creating new venue: {}", venue.getName());
        return venueRepository.save(venue);
    }

    @Override
    public List<Venue> getAllVenues() {
        log.info("Getting all venues");
        return venueRepository.findAll();
    }

    @Override
    public Venue getVenueById(Long id) {
        log.info("Getting venue with ID: {}", id);
        return venueRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venue not found with id: " + id));
    }

    @Override
    public Venue updateVenue(Venue venue) {
        log.info("Updating venue with ID: {}", venue.getId());
        if (!venueRepository.existsById(venue.getId())) {
            throw new RuntimeException("Venue not found with id: " + venue.getId());
        }
        return venueRepository.save(venue);
    }

    @Override
    public void deleteVenue(Long id) {
        log.info("Deleting venue with ID: {}", id);
        if (!venueRepository.existsById(id)) {
            throw new RuntimeException("Venue not found with id: " + id);
        }
        venueRepository.deleteById(id);
    }
} 