package Event.Planner.Event.Planner.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "weather_data")
@Data
public class WeatherData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String location;

    @Column(name = "forecast_date", nullable = false)
    private LocalDateTime forecastDate;

    @Column(nullable = false)
    private double temperature;

    @Column(nullable = false)
    private double precipitation;

    @Column(name = "wind_speed", nullable = false)
    private double windSpeed;

    @Column(name = "weather_condition", nullable = false)
    private String weatherCondition;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
} 