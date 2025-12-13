package org.example.model;

import java.time.LocalDateTime;
import java.util.List;

public class ForecastData {
    private double latitude;
    private double longitude;
    private List<HourlyForecast> hourlyForecasts;
    private WindData windData;

    public static class HourlyForecast {
        private LocalDateTime time;
        private double pm25;
        private double co;
        private double o3;
        private double aqi;

        // Геттеры и сеттеры
        public LocalDateTime getTime() { return time; }
        public void setTime(LocalDateTime time) { this.time = time; }

        public double getPm25() { return pm25; }
        public void setPm25(double pm25) { this.pm25 = pm25; }

        public double getCo() { return co; }
        public void setCo(double co) { this.co = co; }

        public double getO3() { return o3; }
        public void setO3(double o3) { this.o3 = o3; }

        public double getAqi() { return aqi; }
        public void setAqi(double aqi) { this.aqi = aqi; }
    }

    public static class WindData {
        private double speed; // м/с
        private double direction; // градусы
        private double gust;

        // Геттеры и сеттеры
        public double getSpeed() { return speed; }
        public void setSpeed(double speed) { this.speed = speed; }

        public double getDirection() { return direction; }
        public void setDirection(double direction) { this.direction = direction; }

        public double getGust() { return gust; }
        public void setGust(double gust) { this.gust = gust; }
    }

    // Геттеры и сеттеры
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public List<HourlyForecast> getHourlyForecasts() { return hourlyForecasts; }
    public void setHourlyForecasts(List<HourlyForecast> hourlyForecasts) {
        this.hourlyForecasts = hourlyForecasts;
    }

    public WindData getWindData() { return windData; }
    public void setWindData(WindData windData) { this.windData = windData; }
}