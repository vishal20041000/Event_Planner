package Event.Planner.Event.Planner.repository;

import Event.Planner.Event.Planner.model.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {
} 