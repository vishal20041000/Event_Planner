package Event.Planner.Event.Planner.model;

public class WeatherPreferences {
    private final double minTemperature;
    private final double maxTemperature;
    private final double maxPrecipitation;
    private final double maxWindSpeed;
    private final String[] preferredConditions;
    private final String[] unsuitableConditions;

    public WeatherPreferences(double minTemperature, double maxTemperature, 
                            double maxPrecipitation, double maxWindSpeed,
                            String[] preferredConditions, String[] unsuitableConditions) {
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.maxPrecipitation = maxPrecipitation;
        this.maxWindSpeed = maxWindSpeed;
        this.preferredConditions = preferredConditions;
        this.unsuitableConditions = unsuitableConditions;
    }

    public double getMinTemperature() {
        return minTemperature;
    }

    public double getMaxTemperature() {
        return maxTemperature;
    }

    public double getMaxPrecipitation() {
        return maxPrecipitation;
    }

    public double getMaxWindSpeed() {
        return maxWindSpeed;
    }

    public String[] getPreferredConditions() {
        return preferredConditions;
    }

    public String[] getUnsuitableConditions() {
        return unsuitableConditions;
    }

    public boolean isTemperatureSuitable(double temperature) {
        return temperature >= minTemperature && temperature <= maxTemperature;
    }

    public boolean isPrecipitationSuitable(double precipitation) {
        return precipitation <= maxPrecipitation;
    }

    public boolean isWindSpeedSuitable(double windSpeed) {
        return windSpeed <= maxWindSpeed;
    }

    public boolean isWeatherConditionSuitable(String condition) {
        for (String unsuitable : unsuitableConditions) {
            if (unsuitable.equalsIgnoreCase(condition)) {
                return false;
            }
        }
        return true;
    }

    public double calculateSuitabilityScore(double temperature, double precipitation, 
                                          double windSpeed, String condition) {
        double score = 0.0;
        
        // Temperature score (40% weight)
        if (isTemperatureSuitable(temperature)) {
            double tempRange = maxTemperature - minTemperature;
            double tempScore = 1.0 - Math.abs(temperature - (minTemperature + tempRange/2)) / (tempRange/2);
            score += tempScore * 0.4;
        }
        
        // Precipitation score (30% weight)
        if (isPrecipitationSuitable(precipitation)) {
            double precipScore = 1.0 - (precipitation / maxPrecipitation);
            score += precipScore * 0.3;
        }
        
        // Wind speed score (20% weight)
        if (isWindSpeedSuitable(windSpeed)) {
            double windScore = 1.0 - (windSpeed / maxWindSpeed);
            score += windScore * 0.2;
        }
        
        // Weather condition score (10% weight)
        if (isWeatherConditionSuitable(condition)) {
            boolean isPreferred = false;
            for (String preferred : preferredConditions) {
                if (preferred.equalsIgnoreCase(condition)) {
                    isPreferred = true;
                    break;
                }
            }
            score += (isPreferred ? 1.0 : 0.5) * 0.1;
        }
        
        return score;
    }
} 