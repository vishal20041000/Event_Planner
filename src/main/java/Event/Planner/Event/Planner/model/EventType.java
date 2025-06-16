package Event.Planner.Event.Planner.model;

public enum EventType {
    WEDDING(
        new WeatherPreferences(18, 25, 0.1, 10, 
            new String[]{"Clear", "Clouds"}, 
            new String[]{"Rain", "Thunderstorm", "Snow"})
    ),
    CORPORATE(
        new WeatherPreferences(20, 26, 0.2, 15,
            new String[]{"Clear", "Clouds"},
            new String[]{"Rain", "Thunderstorm"})
    ),
    OUTDOOR_SPORTS(
        new WeatherPreferences(15, 28, 0.1, 20,
            new String[]{"Clear"},
            new String[]{"Rain", "Thunderstorm", "Snow", "Fog"})
    ),
    INDOOR_SPORTS(
        new WeatherPreferences(18, 24, 0.3, 25,
            new String[]{"Clear", "Clouds", "Rain"},
            new String[]{"Thunderstorm"})
    ),
    CONCERT(
        new WeatherPreferences(18, 26, 0.2, 15,
            new String[]{"Clear", "Clouds"},
            new String[]{"Rain", "Thunderstorm"})
    ),
    EXHIBITION(
        new WeatherPreferences(20, 25, 0.3, 20,
            new String[]{"Clear", "Clouds", "Rain"},
            new String[]{"Thunderstorm"})
    );

    private final WeatherPreferences preferences;

    EventType(WeatherPreferences preferences) {
        this.preferences = preferences;
    }

    public WeatherPreferences getPreferences() {
        return preferences;
    }
} 