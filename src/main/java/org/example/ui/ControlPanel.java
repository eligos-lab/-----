package org.example.ui;

import javax.swing.*;
import java.awt.*;

public class ControlPanel extends JPanel {
    private final MapPanel mapPanel;

    public ControlPanel(MapPanel mapPanel) {
        this.mapPanel = mapPanel;

        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        setBorder(BorderFactory.createTitledBorder("Управление картой"));
        setBackground(new Color(240, 240, 240));

        initComponents();
    }

    private void initComponents() {
        // Панель координат
        JPanel coordPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        coordPanel.setBorder(BorderFactory.createTitledBorder("Координаты"));

        coordPanel.add(new JLabel("Широта:"));
        JTextField latField = new JTextField("55.7558", 10);
        coordPanel.add(latField);

        coordPanel.add(new JLabel("Долгота:"));
        JTextField lonField = new JTextField("37.6173", 10);
        coordPanel.add(lonField);

        JButton goToBtn = new JButton("Перейти");
        goToBtn.setToolTipText("Перейти к указанным координатам");
        goToBtn.addActionListener(e -> {
            try {
                double lat = Double.parseDouble(latField.getText());
                double lon = Double.parseDouble(lonField.getText());
                mapPanel.centerOn(lat, lon);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Некорректные координаты. Используйте формат: 55.7558",
                        "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });
        coordPanel.add(goToBtn);

        // Панель выбора загрязнителя + кнопка сброса справа
        JPanel pollutantPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        pollutantPanel.setBorder(BorderFactory.createTitledBorder("Загрязнитель"));

        String[] items = {
                "Глобальный AQI",
                "PM2.5 (PM25)",
                "PM10",
                "<html>NO<sub>2</sub></html>",
                "<html>O<sub>3</sub></html>",
                "CO"
        };

        JComboBox<String> pollutantBox = new JComboBox<>(items);
        pollutantBox.setSelectedIndex(0);

        pollutantBox.addActionListener(e -> {
            String selected = (String) pollutantBox.getSelectedItem();
            if (selected == null) return;

            // HTML в строках не мешает — проверяем по ключам
            String code = MapPanel.INDICATOR_GLOBAL;
            if (selected.contains("PM25")) code = MapPanel.INDICATOR_PM25;
            else if (selected.contains("PM10")) code = MapPanel.INDICATOR_PM10;
            else if (selected.contains("NO")) code = MapPanel.INDICATOR_NO2;
            else if (selected.contains("O"))  code = MapPanel.INDICATOR_O3;
            else if (selected.contains("CO")) code = MapPanel.INDICATOR_CO;

            mapPanel.setSelectedIndicator(code);
        });

        JButton resetBtn = new JButton("Сбросить");
        resetBtn.setToolTipText("Очистить ранее установленные точки и сбросить вид");
        resetBtn.addActionListener(e -> {
            mapPanel.clearData();   // сброс точек
            mapPanel.resetView();   // удобный сброс вида
        });

        pollutantPanel.add(new JLabel("Показатель:"));
        pollutantPanel.add(pollutantBox);
        pollutantPanel.add(resetBtn);

        // Информация
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Информация"));

        JLabel infoLabel = new JLabel(
                "<html>" +
                        "• Масштаб: колесико мыши<br>" +
                        "• Перемещение: зажать ЛКМ и тянуть, либо через блок «Координаты»<br>" +
                        "• Одиночный клик ЛКМ: прицельный крестик<br>" +
                        "• Двойной клик ЛКМ: точка с индексом качества воздуха<br>" +
                        "• Сброс ранее установленных точек: кнопка «Сбросить»" +
                        "</html>"
        );
        infoPanel.add(infoLabel);

        add(coordPanel);
        add(pollutantPanel);
        add(infoPanel);
    }
}
