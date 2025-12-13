package org.example;

import org.example.ui.MainFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // (оставляем полезные системные настройки)
        String agent = System.getProperty("http.agent");
        if (agent == null || agent.trim().isEmpty() || agent.startsWith("Java")) {
            System.setProperty("http.agent", "AirQualityTracker/1.0 (JXMapViewer)");
        }
        if (System.getProperty("https.protocols") == null) {
            System.setProperty("https.protocols", "TLSv1.2,TLSv1.3");
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setTitle("Air Quality Tracker");
            frame.setVisible(true);

            // УБРАНО: диалог "API Configuration"
        });
    }
}
