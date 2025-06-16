package Event.Planner.Event.Planner.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "openweathermap", url = "https://api.openweathermap.org/data/2.5", configuration = Event.Planner.Event.Planner.config.FeignConfig.class)
public interface OpenWeatherMapClient {

    @GetMapping("/weather")
    String getCurrentWeather(
        @RequestParam("q") String location,
        @RequestParam("appid") String apiKey,
        @RequestParam("units") String units
    );

    @GetMapping("/forecast")
    String getWeatherForecast(
        @RequestParam("q") String location,
        @RequestParam("appid") String apiKey,
        @RequestParam("units") String units,
        @RequestParam(value = "cnt", defaultValue = "40") int count
    );
} 