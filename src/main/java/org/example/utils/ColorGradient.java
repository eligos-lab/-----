package org.example.utils;

import java.awt.Color;

public class ColorGradient {

    /**
     * Получает цвет на основе значения AQI
     */
    public static Color getColorByAQI(double aqi) {
        if (aqi <= 50) {
            return interpolateColor(aqi, 0, 50,
                    new Color(0, 228, 0), new Color(255, 255, 0));
        } else if (aqi <= 100) {
            return interpolateColor(aqi, 51, 100,
                    new Color(255, 255, 0), new Color(255, 126, 0));
        } else if (aqi <= 150) {
            return interpolateColor(aqi, 101, 150,
                    new Color(255, 126, 0), new Color(255, 0, 0));
        } else if (aqi <= 200) {
            return interpolateColor(aqi, 151, 200,
                    new Color(255, 0, 0), new Color(143, 63, 151));
        } else if (aqi <= 300) {
            return interpolateColor(aqi, 201, 300,
                    new Color(143, 63, 151), new Color(126, 0, 35));
        } else {
            return new Color(126, 0, 35);
        }
    }

    /**
     * Получает цвет на основе концентрации загрязнителя
     */
    public static Color getColorByConcentration(double concentration, String pollutant) {
        double normalized = 0;

        switch (pollutant.toUpperCase()) {
            case "PM25":
                normalized = Math.min(concentration / 250, 1.0);
                break;
            case "CO":
                normalized = Math.min(concentration / 35, 1.0);
                break;
            case "O3":
                normalized = Math.min(concentration / 500, 1.0);
                break;
            case "NO2":
                normalized = Math.min(concentration / 400, 1.0);
                break;
            default:
                normalized = Math.min(concentration / 100, 1.0);
        }

        return interpolateColor(normalized, 0, 1,
                new Color(0, 255, 0), new Color(255, 0, 0));
    }

    private static Color interpolateColor(double value, double min, double max,
                                          Color startColor, Color endColor) {
        double ratio = (value - min) / (max - min);
        ratio = Math.max(0, Math.min(1, ratio));

        int red = (int) (startColor.getRed() + ratio * (endColor.getRed() - startColor.getRed()));
        int green = (int) (startColor.getGreen() + ratio * (endColor.getGreen() - startColor.getGreen()));
        int blue = (int) (startColor.getBlue() + ratio * (endColor.getBlue() - startColor.getBlue()));

        return new Color(red, green, blue);
    }
}