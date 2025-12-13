package org.example.ui;

import javax.swing.*;
import java.awt.*;

public class LegendPanel extends JPanel {
    private final String[] categories = {
            "Хорошо (0-50)",
            "Удовлетворительно (51-100)",
            "Вредно для чувствительных групп (101-150)",
            "Вредно (151-200)",
            "Очень вредно (201-300)",
            "Опасно (301-500)"
    };

    private final Color[] colors = {
            new Color(0, 228, 0),      // Зеленый
            new Color(255, 255, 0),    // Желтый
            new Color(255, 126, 0),    // Оранжевый
            new Color(255, 0, 0),      // Красный
            new Color(143, 63, 151),   // Фиолетовый
            new Color(126, 0, 35)      // Бордовый
    };

    public LegendPanel() {
        setPreferredSize(new Dimension(250, 600));
        setBorder(BorderFactory.createTitledBorder("Легенда AQI"));
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Заголовок
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("Индекс качества воздуха (AQI)", 20, 30);

        // Подзаголовок
        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawString("0-500 (чем выше, тем хуже)", 20, 45);

        int startY = 60;
        int boxSize = 20;

        // Рисуем категории
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        for (int i = 0; i < categories.length; i++) {
            // Цветной квадрат
            g2d.setColor(colors[i]);
            g2d.fillRect(20, startY + i * 35, boxSize, boxSize);

            // Обводка
            g2d.setColor(Color.BLACK);
            g2d.drawRect(20, startY + i * 35, boxSize, boxSize);

            // Текст категории
            g2d.setColor(Color.BLACK);
            g2d.drawString(categories[i], 50, startY + i * 35 + 15);
        }

        // Разделительная линия
        int separatorY = startY + categories.length * 35 + 20;
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawLine(10, separatorY, getWidth() - 20, separatorY);

        // Загрязнители
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("Основные загрязнители:", 20, separatorY + 30);

        g2d.setFont(new Font("Arial", Font.PLAIN, 11));
        String[] pollutants = {
                "PM2.5 - Мелкие частицы",
                "PM10 - Крупные частицы",
                "NO2 - Диоксид азота",
                "O3 - Озон",
                "CO - Окись углерода"
        };

        String[] units = {
                "   (мкг/м³)",
                "    (мкг/м³)",
                "(мкг/м³)",
                "(мкг/м³)",
                "(мг/м³)"
        };

        for (int i = 0; i < pollutants.length; i++) {
            g2d.setColor(Color.BLACK);
            g2d.drawString("• " + pollutants[i], 30, separatorY + 55 + i * 25);
            g2d.setColor(Color.DARK_GRAY);
            g2d.drawString(units[i], 160, separatorY + 55 + i * 25);
        }
    }
}