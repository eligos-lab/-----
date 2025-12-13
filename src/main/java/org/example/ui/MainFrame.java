package org.example.ui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private final MapPanel mapPanel;
    private final ControlPanel controlPanel;
    private final LegendPanel legendPanel;

    public MainFrame() {
        setTitle("Трекер качества воздуха");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(1200, 700);
        setMinimumSize(new Dimension(1050, 850));

        // Инициализация компонентов
        mapPanel = new MapPanel();
        controlPanel = new ControlPanel(mapPanel);
        legendPanel = new LegendPanel();

        // Размещаем компоненты
        add(mapPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
        add(legendPanel, BorderLayout.EAST);

        setLocationRelativeTo(null);

        // Показать информацию о приложении
        showWelcomeMessage();
    }

    private void showWelcomeMessage() {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this,
                    "Трекер качества воздуха v1.0\n\n" +
                            "Функции:\n" +
                            "• Отображение качества воздуха на карте мира\n" +
                            "• Цветовая индикация по AQI\n" +
                            "• Масштабирование и перемещение карты\n" +
                            "• Добавление точек вручную\n\n" +
                            "Используется OpenStreetMap и eLichens API",
                    "Добро пожаловать",
                    JOptionPane.INFORMATION_MESSAGE);
        });
    }
}