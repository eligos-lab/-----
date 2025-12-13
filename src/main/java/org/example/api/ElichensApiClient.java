package org.example.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.example.model.AirQualityData;
import org.example.ui.MapPanel;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ElichensApiClient {

    private static final String API_KEY = "16b6a67c3a8788d37395cafe3ff391da1680a5d9";
    private static final String BASE_URL = "https://api.elichens.com";
    private static final String NOW_PATH = "/v0/now";

    private final OkHttpClient http;

    public ElichensApiClient() {
        this.http = new OkHttpClient.Builder()
                .connectTimeout(8, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();
    }

    public AirQualityData getNow(double lat, double lon, String indicator) throws IOException {
        HttpUrl url = HttpUrl.parse(BASE_URL + NOW_PATH).newBuilder()
                .addQueryParameter("lat", String.valueOf(lat))
                .addQueryParameter("lon", String.valueOf(lon))
                // scale (epa/citeair/elichens/none) — в доке для geolocated API :contentReference[oaicite:2]{index=2}
                .addQueryParameter("aqi", "elichens")
                .addQueryParameter("api_key", API_KEY)
                .build();

        Request request = new Request.Builder().url(url).get().build();

        try (Response resp = http.newCall(request).execute()) {
            if (!resp.isSuccessful()) throw new IOException("HTTP " + resp.code() + " от eLichens");
            if (resp.body() == null) throw new IOException("Пустой ответ от eLichens");
            String body = resp.body().string();
            return parseNowResponse(body, lat, lon, indicator);
        }
    }

    private AirQualityData parseNowResponse(String json, double lat, double lon, String indicator) throws IOException {
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();

        // соберем концентрации (необязательно, но полезно хранить)
        Map<String, Double> pollutants = new HashMap<>();
        JsonObject pol = safeObj(root.get("pollutants"));
        if (pol != null) {
            for (String key : pol.keySet()) {
                JsonObject pObj = safeObj(pol.get(key));
                if (pObj == null) continue;

                // попытка вытащить concentration.value (формат может быть разный — парсим мягко)
                Double conc = null;
                JsonObject concObj = safeObj(pObj.get("concentration"));
                if (concObj != null) {
                    conc = tryReadNumber(concObj, "value");
                }
                if (conc == null) {
                    conc = tryReadNumber(pObj, "concentration");
                }
                if (conc != null) pollutants.put(key, conc);
            }
        }

        // --- выбираем AQI/цвет либо из global_aqi, либо из pollutants.<X>.aqi ---
        double aqiValue = Double.NaN;
        String colorHex = "#808080";

        if (indicator == null || indicator.isBlank()) indicator = MapPanel.INDICATOR_GLOBAL;

        if (MapPanel.INDICATOR_GLOBAL.equals(indicator)) {
            JsonObject globalAqi = safeObj(root.get("global_aqi"));
            JsonObject el = (globalAqi == null) ? null : safeObj(globalAqi.get("elichens"));
            if (el != null) {
                Double v = tryReadNumber(el, "value");
                if (v != null) aqiValue = v;
                String c = tryReadString(el, "color");
                if (c != null && !c.isBlank()) colorHex = c;
            }
        } else {
            // pollutants.<indicator>.aqi.elichens.{value,color}
            if (pol != null && pol.has(indicator)) {
                JsonObject pObj = safeObj(pol.get(indicator));
                JsonObject aqi = (pObj == null) ? null : safeObj(pObj.get("aqi"));
                JsonObject el = (aqi == null) ? null : safeObj(aqi.get("elichens"));
                if (el != null) {
                    Double v = tryReadNumber(el, "value");
                    if (v != null) aqiValue = v;
                    String c = tryReadString(el, "color");
                    if (c != null && !c.isBlank()) colorHex = c;
                }
            }
        }

        return new AirQualityData(lat, lon, LocalDateTime.now(), pollutants, aqiValue, colorHex);
    }

    private static JsonObject safeObj(JsonElement e) {
        if (e == null || e.isJsonNull() || !e.isJsonObject()) return null;
        return e.getAsJsonObject();
    }

    private static Double tryReadNumber(JsonObject obj, String key) {
        try {
            if (obj.has(key) && obj.get(key).isJsonPrimitive() && obj.get(key).getAsJsonPrimitive().isNumber()) {
                return obj.get(key).getAsDouble();
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static String tryReadString(JsonObject obj, String key) {
        try {
            if (obj.has(key) && obj.get(key).isJsonPrimitive() && obj.get(key).getAsJsonPrimitive().isString()) {
                return obj.get(key).getAsString();
            }
        } catch (Exception ignored) {}
        return null;
    }
}
