package org.example.model;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

public class AirQualityData {
    private final double latitude;
    private final double longitude;
    private final LocalDateTime timestamp;
    private final Map<String, Double> pollutants;

    private final double aqiValue;
    private final String colorHex;

    public AirQualityData(double latitude, double longitude, LocalDateTime timestamp, Map<String, Double> pollutants) {
        this(latitude, longitude, timestamp, pollutants, Double.NaN, "#808080");
    }

    public AirQualityData(double latitude, double longitude, LocalDateTime timestamp,
                          Map<String, Double> pollutants, double aqiValue, String colorHex) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.pollutants = (pollutants == null) ? Collections.emptyMap() : pollutants;
        this.aqiValue = aqiValue;
        this.colorHex = (colorHex == null || colorHex.isBlank()) ? "#808080" : colorHex;
    }

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public Map<String, Double> getPollutants() { return pollutants; }

    // если тебе где-то нужен численный индекс
    public double getAqi() { return aqiValue; }

    // MapPanel использует это для цвета точки
    public String getColorByAQI() { return colorHex; }
}