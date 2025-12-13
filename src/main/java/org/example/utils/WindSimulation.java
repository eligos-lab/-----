package org.example.utils;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class WindSimulation {

    /**
     * Моделирует распространение загрязнения на основе ветра
     * @param sourceLat Широта источника
     * @param sourceLon Долгота источника
     * @param windSpeed Скорость ветра (м/с)
     * @param windDirection Направление ветра (градусы)
     * @param pollutantAmount Количество загрязнителя
     * @param hours Количество часов для симуляции
     * @return Список точек распространения
     */
    public List<Point2D.Double> simulatePollutionSpread(
            double sourceLat, double sourceLon,
            double windSpeed, double windDirection,
            double pollutantAmount, int hours) {

        List<Point2D.Double> spreadPoints = new ArrayList<>();

        // Конвертируем направление в радианы
        double windRad = Math.toRadians(windDirection);

        // Константы для расчета
        double diffusionCoefficient = 0.1; // Коэффициент диффузии
        double timeStep = 0.1; // Шаг времени (часы)

        // Начальная точка
        double currentLat = sourceLat;
        double currentLon = sourceLon;
        double currentConcentration = pollutantAmount;

        for (double time = 0; time < hours; time += timeStep) {
            // Перенос ветром
            double distance = windSpeed * timeStep * 3600 / 111000; // В градусах широты
            currentLon += distance * Math.cos(windRad) / Math.cos(Math.toRadians(currentLat));
            currentLat += distance * Math.sin(windRad);

            // Диффузия
            double spreadDistance = Math.sqrt(4 * diffusionCoefficient * time * 3600);
            currentConcentration = pollutantAmount / (Math.PI * spreadDistance * spreadDistance);

            // Добавляем точку
            spreadPoints.add(new Point2D.Double(currentLon, currentLat));
        }

        return spreadPoints;
    }

    /**
     * Рассчитывает концентрацию в заданной точке
     */
    public double calculateConcentrationAtPoint(
            double pointLat, double pointLon,
            double sourceLat, double sourceLon,
            double windSpeed, double windDirection,
            double pollutantAmount, double time) {

        // Расстояние от источника
        double dx = (pointLon - sourceLon) * 111000 * Math.cos(Math.toRadians(sourceLat));
        double dy = (pointLat - sourceLat) * 111000;

        // Влияние ветра
        double windRad = Math.toRadians(windDirection);
        double windX = windSpeed * Math.cos(windRad);
        double windY = windSpeed * Math.sin(windRad);

        // Гауссова модель распространения
        double sigmaX = 0.3 * time * 3600; // Дисперсия по X
        double sigmaY = 0.1 * time * 3600; // Дисперсия по Y

        double concentration = pollutantAmount / (2 * Math.PI * sigmaX * sigmaY) *
                Math.exp(-0.5 * (
                        Math.pow((dx - windX * time * 3600) / sigmaX, 2) +
                                Math.pow((dy - windY * time * 3600) / sigmaY, 2)
                ));

        return Math.max(0, concentration);
    }
}