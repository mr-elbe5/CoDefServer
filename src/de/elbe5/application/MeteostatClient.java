package de.elbe5.application;

import de.elbe5.base.JsonDeserializer;
import de.elbe5.base.Log;
import de.elbe5.configuration.CodefConfiguration;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MeteostatClient {

    static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00:00");

    static public String findWeatherStation(Coordinate coordinate){
        if (coordinate == null)
            return "";
        HttpsURLConnection connection = null;
        try {
            String sb = "https://meteostat.p.rapidapi.com/stations/nearby?lat=" +
                    coordinate.lat +
                    "&lon=" +
                    coordinate.lon +
                    "&limit=1";
            URL url = new URL(sb);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("accept", "application/json");
            connection.setRequestProperty("X-RapidApi-Key", CodefConfiguration.getMeteoStatKey());
            connection.setConnectTimeout(5000);
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                try {
                    JSONObject jsonObject = (JSONObject) new JsonDeserializer().deserialize(connection.getInputStream());
                    JSONArray dataArray = (JSONArray) jsonObject.get("data");
                    JSONObject data = (JSONObject) dataArray.get(0);
                    String s = ((String)data.get("id"));
                    if (!s.isEmpty()) {
                        Log.info("found weather station: " + s);
                        return s;
                    }
                }
                catch (Exception e){
                    Log.error("unable to get weather station from json");
                }
            }
        } catch (Exception e) {
            Log.error("no connection to meteostat", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return "";
    }

    public static class WeatherData{
        public int weatherCoco = 0;
        public int weatherWspd = 0;
        public int weatherWdir = 0;
        public int weatherTemp = 0;
        public int weatherRhum = 0;
        public int weatherPrcp = 0;
    }

    static public WeatherData getWeatherData(String station, LocalDateTime time){
        HttpsURLConnection connection = null;
        try {
            String sb = "https://meteostat.p.rapidapi.com/stations/hourly?station=" +
                    station +
                    "&start=" +
                    time.format(dateFormatter) +
                    "&end=" +
                    time.format(dateFormatter) +
                    "&tz=Europe%2FBerlin&units=metric";
            URL url = new URL(sb);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("accept", "application/json");
            connection.setRequestProperty("X-RapidApi-Key", CodefConfiguration.getMeteoStatKey());
            connection.setConnectTimeout(5000);
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                try {
                    JSONObject jsonObject = (JSONObject) new JsonDeserializer().deserialize(connection.getInputStream());
                    JSONArray dataArray = (JSONArray) jsonObject.get("data");
                    String dateTime = time.format(dateTimeFormatter);
                    for (Object o : dataArray) {
                        JSONObject data = (JSONObject) o;
                        if (data.get("time").equals(dateTime)) {
                            WeatherData weatherData = new WeatherData();
                            weatherData.weatherCoco = getInt(data.get("coco"));
                            weatherData.weatherWspd = getInt(data.get("wspd"));
                            weatherData.weatherWdir = getInt(data.get("wdir"));
                            weatherData.weatherTemp = getInt(data.get("temp"));
                            weatherData.weatherRhum = getInt(data.get("rhum"));
                            weatherData.weatherPrcp = getInt(data.get("prcp"));
                            return weatherData;
                        }
                    }
                }
                catch (Exception e){
                    Log.error("unable to get weather station from json");
                }
            }
        } catch (Exception e) {
            Log.error("no connection to meteostat", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    static int getInt(Object obj){
        if (obj == null)
            return -1;
        if (obj instanceof Long){
            return ((Long)obj).intValue();
        }
        if (obj instanceof Double){
            return ((Double)obj).intValue();
        }
        return -1;
    }

}
