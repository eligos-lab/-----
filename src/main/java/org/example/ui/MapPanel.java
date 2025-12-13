package org.example.ui;

import org.example.api.ElichensApiClient;
import org.example.model.AirQualityData;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.cache.FileBasedLocalCache;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCenter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;

import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.*;
import java.util.List;

public class MapPanel extends JXMapViewer {

    public static final String INDICATOR_GLOBAL = "GLOBAL";
    public static final String INDICATOR_PM25 = "PM25";
    public static final String INDICATOR_PM10 = "PM10";
    public static final String INDICATOR_NO2  = "NO2";
    public static final String INDICATOR_O3   = "O3";
    public static final String INDICATOR_CO   = "CO";

    private final List<AirQualityData> airQualityPoints = new ArrayList<>();
    private final Map<AirQualityData, GeoPosition> pointPositions = new HashMap<>();

    private GeoPosition selectedPosition;
    private final JLabel statusLabel = new JLabel(" Инициализация карты...");

    private final ElichensApiClient apiClient = new ElichensApiClient();
    private String selectedIndicator = INDICATOR_GLOBAL;

    public MapPanel() {
        statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

        setupTileFactory();

        setAddressLocation(new GeoPosition(55.7558, 37.6173));
        setZoom(4);

        setupAppearance();
        setupEventHandlers();
        setupPainter();

        updateStatus("Карта готова. Двойной клик — добавить точку (AQI)");
    }

    public void setSelectedIndicator(String indicator) {
        if (indicator == null || indicator.trim().isEmpty()) return;
        this.selectedIndicator = indicator;
        updateStatus("Выбран показатель: " + indicatorLabel(indicator));
    }

    public String getSelectedIndicator() {
        return selectedIndicator;
    }

    private String indicatorLabel(String indicator) {
        if (indicator == null) return "Глобальный AQI";
        switch (indicator) {
            case INDICATOR_PM25:
                return "PM2.5";
            case INDICATOR_PM10:
                return "PM10";
            case INDICATOR_NO2:
                return "NO2";
            case INDICATOR_O3:
                return "O3";
            case INDICATOR_CO:
                return "CO";
            case INDICATOR_GLOBAL:
            default:
                return "Глобальный AQI";
        }
    }

    private void setupTileFactory() {
        try {
            String agent = System.getProperty("http.agent");
            if (agent == null || agent.trim().isEmpty() || agent.startsWith("Java")) {
                System.setProperty("http.agent", "AirQualityTracker/1.0 (JXMapViewer)");
            }
            if (System.getProperty("https.protocols") == null) {
                System.setProperty("https.protocols", "TLSv1.2,TLSv1.3");
            }
            System.setProperty("sun.net.client.defaultConnectTimeout", "7000");
            System.setProperty("sun.net.client.defaultReadTimeout", "15000");

            TileFactoryInfo info = new OSMTileFactoryInfo();
            DefaultTileFactory tileFactory = new DefaultTileFactory(info);

            File cacheDir = new File("tilecache");
            if (!cacheDir.exists()) cacheDir.mkdirs();
            tileFactory.setLocalCache(new FileBasedLocalCache(cacheDir, false));
            tileFactory.setThreadPoolSize(8);

            setTileFactory(tileFactory);
            updateStatus("OSM тайлы подключены");
        } catch (Exception e) {
            updateStatus("Ошибка настройки карты: " + e.getMessage());
            setupOfflineMap();
        }
    }

    private void setupOfflineMap() {
        TileFactoryInfo info = new TileFactoryInfo(1, 15, 17, 256, true, true,
                "Offline", "", "", "") {
            @Override
            public String getTileUrl(int x, int y, int zoom) {
                return "";
            }
        };
        setTileFactory(new DefaultTileFactory(info));
        updateStatus("Используется оффлайн карта");
    }

    private void setupAppearance() {
        setLayout(new BorderLayout());

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBackground(new Color(240, 240, 240));
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        MouseInputListener pan = new PanMouseInputListener(this);
        addMouseListener(pan);
        addMouseMotionListener(pan);
        addMouseWheelListener(new ZoomMouseWheelListenerCenter(this));

        // CenterMapListener НЕ добавляем — чтобы двойной клик не влиял на масштаб/центр

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    GeoPosition geoPos = screenPointToGeo(e.getPoint());
                    if (geoPos == null) return;

                    selectedPosition = geoPos;
                    repaint();

                    if (e.getClickCount() == 1) {
                        updateStatus(String.format("Прицел: %.4f°N, %.4f°E",
                                geoPos.getLatitude(), geoPos.getLongitude()));
                        return;
                    }

                    if (e.getClickCount() == 2) {
                        fetchAndAddAirQualityPoint(geoPos);
                    }
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    showContextMenu(e);
                }
            }
        });
    }

    private GeoPosition screenPointToGeo(Point screenPoint) {
        Rectangle viewport = getViewportBounds();
        Point2D worldPoint = new Point2D.Double(
                screenPoint.getX() + viewport.getX(),
                screenPoint.getY() + viewport.getY()
        );
        return getTileFactory().pixelToGeo(worldPoint, getZoom());
    }

    public void fetchAndAddAirQualityPoint(GeoPosition position) {
        final double lat = position.getLatitude();
        final double lon = position.getLongitude();
        final String indicator = this.selectedIndicator;

        updateStatus("Запрос " + indicatorLabel(indicator) + "… " + String.format("(%.4f, %.4f)", lat, lon));

        new SwingWorker<AirQualityData, Void>() {
            @Override
            protected AirQualityData doInBackground() throws Exception {
                return apiClient.getNow(lat, lon, indicator);
            }

            @Override
            protected void done() {
                try {
                    AirQualityData data = get();
                    addAirQualityData(data);
                    updateStatus("Точка добавлена: " + indicatorLabel(indicator));
                } catch (Exception ex) {
                    updateStatus("Ошибка API: " + ex.getMessage());
                    JOptionPane.showMessageDialog(MapPanel.this,
                            "Не удалось получить данные по воздуху.\n" + ex.getMessage(),
                            "Ошибка API", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void setupPainter() {
        setOverlayPainter((g, map, w, h) -> {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Rectangle viewport = getViewportBounds();

            for (Map.Entry<AirQualityData, GeoPosition> entry : pointPositions.entrySet()) {
                AirQualityData data = entry.getKey();
                GeoPosition pos = entry.getValue();

                Point2D world = getTileFactory().geoToPixel(pos, getZoom());
                int x = (int) (world.getX() - viewport.getX());
                int y = (int) (world.getY() - viewport.getY());

                drawAirQualityPoint(g2d, data, x, y);
            }

            if (selectedPosition != null) {
                Point2D world = getTileFactory().geoToPixel(selectedPosition, getZoom());
                int x = (int) (world.getX() - viewport.getX());
                int y = (int) (world.getY() - viewport.getY());
                drawSelectedPosition(g2d, x, y);
            }

            g2d.dispose();
        });
    }

    private void drawAirQualityPoint(Graphics2D g2d, AirQualityData data, int x, int y) {
        int pointSize = Math.max(8, Math.min(20, 8 + getZoom()));

        Color pointColor;
        try {
            pointColor = Color.decode(data.getColorByAQI());
        } catch (Exception e) {
            pointColor = Color.GRAY;
        }

        g2d.setColor(pointColor);
        g2d.fillOval(x - pointSize / 2, y - pointSize / 2, pointSize, pointSize);

        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.drawOval(x - pointSize / 2, y - pointSize / 2, pointSize, pointSize);
    }

    private void drawSelectedPosition(Graphics2D g2d, int x, int y) {
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(2f));
        int s = 15;
        g2d.drawLine(x - s, y, x + s, y);
        g2d.drawLine(x, y - s, x, y + s);
    }

    private void updateStatus(String message) {
        statusLabel.setText(" " + message);
    }

    private void showContextMenu(MouseEvent e) {
        JPopupMenu menu = new JPopupMenu();

        GeoPosition geoPos = screenPointToGeo(e.getPoint());
        if (geoPos != null) {
            JMenuItem add = new JMenuItem(
                    String.format("Добавить точку здесь (%.4f, %.4f)",
                            geoPos.getLatitude(), geoPos.getLongitude())
            );
            add.addActionListener(evt -> fetchAndAddAirQualityPoint(geoPos));
            menu.add(add);
        }

        JMenuItem reset = new JMenuItem("Сбросить вид");
        reset.addActionListener(evt -> resetView());
        menu.addSeparator();
        menu.add(reset);

        menu.show(this, e.getX(), e.getY());
    }

    public void centerOn(double lat, double lon) {
        setAddressLocation(new GeoPosition(lat, lon));
        repaint();
    }

    public GeoPosition getSelectedPosition() {
        return selectedPosition;
    }

    public void clearData() {
        airQualityPoints.clear();
        pointPositions.clear();
        selectedPosition = null;
        repaint();
    }

    public void updateData() {
        updateStatus("Обновление данных… (пока не реализовано)");
    }

    public void addAirQualityData(AirQualityData data) {
        airQualityPoints.add(data);
        pointPositions.put(data, new GeoPosition(data.getLatitude(), data.getLongitude()));
        repaint();
    }

    public void resetView() {
        setZoom(4);
        setAddressLocation(new GeoPosition(55.7558, 37.6173));
        repaint();
    }

    private static class OSMTileFactoryInfo extends TileFactoryInfo {
        public OSMTileFactoryInfo() {
            super("OpenStreetMap", 1, 15, 17, 256, true, true,
                    "https://tile.openstreetmap.org", "x", "y", "z");
        }

        @Override
        public String getTileUrl(int x, int y, int zoom) {
            int osmZ = getTotalMapZoom() - zoom;

            String sub;
            int idx = Math.floorMod(x + y, 3);
            if (idx == 0) sub = "a";
            else if (idx == 1) sub = "b";
            else sub = "c";

            return "https://" + sub + ".tile.openstreetmap.org/" + osmZ + "/" + x + "/" + y + ".png";
        }
    }
}