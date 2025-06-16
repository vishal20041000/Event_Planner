package Event.Planner.Event.Planner.controller;

import Event.Planner.Event.Planner.model.Venue;
import Event.Planner.Event.Planner.service.VenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/venues")
@Tag(name = "Venue Management", description = "APIs for venue management")
public class VenueController {

    private static final Logger log = LoggerFactory.getLogger(VenueController.class);
    private final VenueService venueService;

    @Autowired
    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @PostMapping
    @Operation(summary = "Create a new venue")
    public ResponseEntity<Venue> createVenue(@RequestBody Venue venue) {
        log.info("Creating new venue: {}", venue.getName());
        venue.setAvailable(true); // Set default value
        venue.setType("Indoor"); // Set default value if not provided
        return ResponseEntity.ok(venueService.createVenue(venue));
    }

    @GetMapping
    @Operation(summary = "Get all venues")
    public ResponseEntity<List<Venue>> getAllVenues() {
        log.info("Getting all venues");
        return ResponseEntity.ok(venueService.getAllVenues());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get venue by ID")
    public ResponseEntity<Venue> getVenueById(@PathVariable Long id) {
        log.info("Getting venue with ID: {}", id);
        return ResponseEntity.ok(venueService.getVenueById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update venue")
    public ResponseEntity<Venue> updateVenue(@PathVariable Long id, @RequestBody Venue venue) {
        log.info("Updating venue with ID: {}", id);
        venue.setId(id);
        return ResponseEntity.ok(venueService.updateVenue(venue));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete venue")
    public ResponseEntity<Void> deleteVenue(@PathVariable Long id) {
        log.info("Deleting venue with ID: {}", id);
        venueService.deleteVenue(id);
        return ResponseEntity.ok().build();
    }
} 