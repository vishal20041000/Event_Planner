package Event.Planner.Event.Planner.controller;

import Event.Planner.Event.Planner.model.Venue;
import Event.Planner.Event.Planner.service.VenueService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VenueController.class)
class VenueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VenueService venueService;

    @Test
    void createVenue_Success() throws Exception {
        Venue venue = new Venue();
        venue.setName("Test Venue");
        venue.setLocation("Test Location");
        venue.setCapacity(100);
        venue.setPricePerDay(1000.0f);

        when(venueService.createVenue(any(Venue.class))).thenReturn(venue);

        mockMvc.perform(post("/venues")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(venue)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(venue.getName()))
                .andExpect(jsonPath("$.location").value(venue.getLocation()))
                .andExpect(jsonPath("$.capacity").value(venue.getCapacity()))
                .andExpect(jsonPath("$.pricePerDay").value(venue.getPricePerDay()));
    }

    @Test
    void getAllVenues_Success() throws Exception {
        List<Venue> venues = Arrays.asList(
            createTestVenue(1L, "Venue 1"),
            createTestVenue(2L, "Venue 2")
        );

        when(venueService.getAllVenues()).thenReturn(venues);

        mockMvc.perform(get("/venues"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Venue 1"))
                .andExpect(jsonPath("$[1].name").value("Venue 2"));
    }

    @Test
    void getVenueById_Success() throws Exception {
        Venue venue = createTestVenue(1L, "Test Venue");

        when(venueService.getVenueById(1L)).thenReturn(venue);

        mockMvc.perform(get("/venues/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Venue"));
    }

    @Test
    void updateVenue_Success() throws Exception {
        Venue venue = createTestVenue(1L, "Updated Venue");

        when(venueService.updateVenue(any(Long.class), any(Venue.class))).thenReturn(venue);

        mockMvc.perform(put("/venues/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(venue)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Venue"));
    }

    @Test
    void deleteVenue_Success() throws Exception {
        mockMvc.perform(delete("/venues/1"))
                .andExpect(status().isOk());
    }

    private Venue createTestVenue(Long id, String name) {
        Venue venue = new Venue();
        venue.setId(id);
        venue.setName(name);
        venue.setLocation("Test Location");
        venue.setCapacity(100);
        venue.setPricePerDay(1000.0f);
        return venue;
    }
} 