package Event.Planner.Event.Planner.service;

import Event.Planner.Event.Planner.model.Venue;
import Event.Planner.Event.Planner.repository.VenueRepository;
import Event.Planner.Event.Planner.service.impl.VenueServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VenueServiceTest {

    @Mock
    private VenueRepository venueRepository;

    @InjectMocks
    private VenueServiceImpl venueService;

    private Venue testVenue;

    @BeforeEach
    void setUp() {
        testVenue = new Venue();
        testVenue.setId(1L);
        testVenue.setName("Test Venue");
        testVenue.setLocation("Test Location");
        testVenue.setCapacity(100);
        testVenue.setPricePerDay(1000.0f);
    }

    @Test
    void createVenue_Success() {
        when(venueRepository.save(any(Venue.class))).thenReturn(testVenue);

        Venue createdVenue = venueService.createVenue(testVenue);

        assertNotNull(createdVenue);
        assertEquals(testVenue.getName(), createdVenue.getName());
        verify(venueRepository, times(1)).save(any(Venue.class));
    }

    @Test
    void getAllVenues_Success() {
        List<Venue> venues = Arrays.asList(testVenue);
        when(venueRepository.findAll()).thenReturn(venues);

        List<Venue> result = venueService.getAllVenues();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(venueRepository, times(1)).findAll();
    }

    @Test
    void getVenueById_Success() {
        when(venueRepository.findById(1L)).thenReturn(Optional.of(testVenue));

        Venue result = venueService.getVenueById(1L);

        assertNotNull(result);
        assertEquals(testVenue.getId(), result.getId());
        verify(venueRepository, times(1)).findById(1L);
    }

    @Test
    void getVenueById_NotFound() {
        when(venueRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> venueService.getVenueById(1L));
        verify(venueRepository, times(1)).findById(1L);
    }

    @Test
    void updateVenue_Success() {
        when(venueRepository.findById(1L)).thenReturn(Optional.of(testVenue));
        when(venueRepository.save(any(Venue.class))).thenReturn(testVenue);

        Venue updatedVenue = new Venue();
        updatedVenue.setName("Updated Venue");
        updatedVenue.setLocation("Updated Location");
        updatedVenue.setCapacity(200);
        updatedVenue.setPricePerDay(2000.0f);

        Venue result = venueService.updateVenue(1L, updatedVenue);

        assertNotNull(result);
        assertEquals(updatedVenue.getName(), result.getName());
        assertEquals(updatedVenue.getLocation(), result.getLocation());
        assertEquals(updatedVenue.getCapacity(), result.getCapacity());
        assertEquals(updatedVenue.getPricePerDay(), result.getPricePerDay());
        verify(venueRepository, times(1)).findById(1L);
        verify(venueRepository, times(1)).save(any(Venue.class));
    }

    @Test
    void deleteVenue_Success() {
        when(venueRepository.existsById(1L)).thenReturn(true);
        doNothing().when(venueRepository).deleteById(1L);

        venueService.deleteVenue(1L);

        verify(venueRepository, times(1)).existsById(1L);
        verify(venueRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteVenue_NotFound() {
        when(venueRepository.existsById(1L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> venueService.deleteVenue(1L));
        verify(venueRepository, times(1)).existsById(1L);
        verify(venueRepository, never()).deleteById(anyLong());
    }
} 