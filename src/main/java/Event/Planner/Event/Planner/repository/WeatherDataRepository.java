package Event.Planner.Event.Planner.repository;

import Event.Planner.Event.Planner.model.WeatherData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WeatherDataRepository extends JpaRepository<WeatherData, Long> {
    List<WeatherData> findByLocationAndForecastDateBetween(String location, LocalDateTime startDate, LocalDateTime endDate);
    WeatherData findFirstByLocationAndForecastDateOrderByCreatedAtDesc(String location, LocalDateTime forecastDate);
} 