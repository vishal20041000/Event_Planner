package Event.Planner.Event.Planner.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimitConfig {

    @Bean
    public Bucket createNewBucket() {
        // 1000 requests per day (OpenWeatherMap free tier limit)
        Bandwidth limit = Bandwidth.simple(1000, Duration.ofDays(1));
        return Bucket4j.builder()
            .addLimit(limit)
            .build();
    }
} 