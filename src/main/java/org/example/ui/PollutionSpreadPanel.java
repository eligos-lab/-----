package org.example.ui;

import org.example.model.ForecastData;
import org.example.utils.WindSimulation;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

public class PollutionSpreadPanel extends JPanel {
    private final List<ForecastData> forecastPoints;
    private final WindSimulation windSimulation;
    private int simulationHours = 24;

    public PollutionSpreadPanel() {
        forecastPoints = new ArrayList<>();
        windSimulation = new WindSimulation();
        setPreferredSize(new Dimension(600, 400));
        setBorder(BorderFactory.createTitledBorder("Прогноз распространения загрязнения"));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Рисование сетки
        drawGrid(g2d);

        // Рисование прогноза распространения
        for (ForecastData forecast : forecastPoints) {
            drawPollutionSpread(g2d, forecast);
        }

        // Легенда
        drawLegend(g2d);

        // Информация
        drawInfo(g2d);
    }

    private void drawGrid(Graphics2D g2d) {
        g2d.setColor(new Color(240, 240, 240));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setStroke(new BasicStroke(1));

        int gridSize = 50;
        for (int x = 0; x < getWidth(); x += gridSize) {
            g2d.drawLine(x, 0, x, getHeight());
        }
        for (int y = 0; y < getHeight(); y += gridSize) {
            g2d.drawLine(0, y, getWidth(), y);
        }
    }

    private void drawPollutionSpread(Graphics2D g2d, ForecastData forecast) {
        if (forecast.getWindData() == null) return;

        double windSpeed = forecast.getWindData().getSpeed();
        double windDirection = forecast.getWindData().getDirection();

        // Конвертируем направление ветра в радианы
        double windRad = Math.toRadians(270 - windDirection); // 0° = север, 90° = восток

        // Центр источника загрязнения
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        // Рисуем источник
        g2d.setColor(Color.RED);
        g2d.fillOval(centerX - 8, centerY - 8, 16, 16);
        g2d.setColor(Color.BLACK);
        g2d.drawOval(centerX - 8, centerY - 8, 16, 16);

        // Рисуем вектор ветра
        int arrowLength = (int) (windSpeed * 15);
        int endX = centerX + (int) (arrowLength * Math.cos(windRad));
        int endY = centerY + (int) (arrowLength * Math.sin(windRad));

        g2d.setColor(Color.BLUE);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(centerX, centerY, endX, endY);

        // Стрелка
        drawArrow(g2d, centerX, centerY, endX, endY);

        // Рисуем распространение загрязнения
        g2d.setColor(new Color(255, 0, 0, 50));
        for (int hour = 1; hour <= simulationHours; hour++) {
            double distance = windSpeed * hour * 3600 / 1000; // км
            double spread = distance * 0.3; // Расширение облака

            int ellipseWidth = (int) (spread * 12);
            int ellipseHeight = (int) (spread * 6);

            int ellipseX = centerX + (int) (distance * 12 * Math.cos(windRad)) - ellipseWidth / 2;
            int ellipseY = centerY + (int) (distance * 12 * Math.sin(windRad)) - ellipseHeight / 2;

            g2d.setStroke(new BasicStroke(1));
            g2d.fillOval(ellipseX, ellipseY, ellipseWidth, ellipseHeight);
        }

        // Подписываем источник
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("Источник загрязнения", centerX + 20, centerY - 10);

        // Информация о ветре
        g2d.drawString(String.format("Ветер: %.1f м/с, %.0f°",
                windSpeed, windDirection), centerX + 20, centerY + 10);
    }

    private void drawArrow(Graphics2D g2d, int x1, int y1, int x2, int y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double angle = Math.atan2(dy, dx);
        int len = (int) Math.sqrt(dx*dx + dy*dy);

        Polygon arrowHead = new Polygon();
        arrowHead.addPoint(0, 0);
        arrowHead.addPoint(-8, -4);
        arrowHead.addPoint(-8, 4);

        AffineTransform tx = new AffineTransform();
        tx.translate(x2, y2);
        tx.rotate(angle - Math.PI/2);

        g2d.fill(tx.createTransformedShape(arrowHead));
    }

    private void drawLegend(Graphics2D g2d) {
        int legendX = 20;
        int legendY = 30;

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("Легенда:", legendX, legendY);

        g2d.setFont(new Font("Arial", Font.PLAIN, 12));

        // Источник загрязнения
        g2d.setColor(Color.RED);
        g2d.fillOval(legendX, legendY + 20, 12, 12);
        g2d.setColor(Color.BLACK);
        g2d.drawOval(legendX, legendY + 20, 12, 12);
        g2d.drawString("Источник загрязнения", legendX + 20, legendY + 30);

        // Ветер
        g2d.setColor(Color.BLUE);
        g2d.drawLine(legendX, legendY + 45, legendX + 30, legendY + 45);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Направление ветра", legendX + 40, legendY + 50);

        // Распространение
        g2d.setColor(new Color(255, 0, 0, 100));
        g2d.fillOval(legendX, legendY + 65, 30, 15);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Облако загрязнения", legendX + 40, legendY + 75);
    }

    private void drawInfo(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
        g2d.drawString("Прогноз на " + simulationHours + " часов", 20, getHeight() - 20);
        g2d.drawString("Масштаб: 1 см = 10 км", 20, getHeight() - 5);
    }

    public void addForecastData(ForecastData forecast) {
        forecastPoints.clear(); // Очищаем предыдущие прогнозы
        forecastPoints.add(forecast);
        repaint();
    }

    public void setSimulationHours(int hours) {
        this.simulationHours = Math.max(1, Math.min(hours, 72));
        repaint();
    }

    public void clearForecast() {
        forecastPoints.clear();
        repaint();
    }
}