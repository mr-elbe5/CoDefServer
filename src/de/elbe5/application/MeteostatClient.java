package de.elbe5.application;

import de.elbe5.base.*;
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
        public double weatherWspd = 0;
        public double weatherWdir = 0;
        public double weatherTemp = 0;
        public double weatherRhum = 0;

        public String toJsonString(){
            return new JsonObject()
                    .add("weatherCoco", getWeatherCoco(weatherCoco))
                    .add("weatherWspd", weatherWspd + " km/h")
                    .add("weatherWdir", getWindDirection(weatherWdir))
                    .add("weatherTemp", weatherTemp + " °C")
                    .add("weatherRhum", weatherRhum + " %")
                    .toJSONString();
        }
    }

    static public WeatherData getWeatherData(String station, LocalDateTime time, String timezone){
        HttpsURLConnection connection = null;
        try {
            String sb = "https://meteostat.p.rapidapi.com/stations/hourly?station=" +
                    station +
                    "&start=" +
                    time.format(dateFormatter) +
                    "&end=" +
                    time.format(dateFormatter) +
                    "&tz=" +
                    StringHelper.toUrl(timezone) +
                    "&units=metric";
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
                            weatherData.weatherWspd = getDouble(data.get("wspd"));
                            weatherData.weatherWdir = getDouble(data.get("wdir"));
                            weatherData.weatherTemp = getDouble(data.get("temp"));
                            weatherData.weatherRhum = getDouble(data.get("rhum"));
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
        if (obj instanceof Long){
            return ((Long)obj).intValue();
        }
        return -1;
    }

    static double getDouble(Object obj){
        if (obj instanceof Double){
            return (double)obj;
        }
        return -1;
    }

    static public String getWeatherCoco(int value) {
        return switch (value) {
            case 1 -> LocalizedSystemStrings.getInstance().string("weather.clear");
            case 2 -> LocalizedSystemStrings.getInstance().string("weather.fair");
            case 3 -> LocalizedSystemStrings.getInstance().string("weather.cloudy");
            case 4 -> LocalizedSystemStrings.getInstance().string("weather.overcast");
            case 5 -> LocalizedSystemStrings.getInstance().string("weather.fog");
            case 6 -> LocalizedSystemStrings.getInstance().string("weather.freezingFog");
            case 7 -> LocalizedSystemStrings.getInstance().string("weather.lightRain");
            case 8 -> LocalizedSystemStrings.getInstance().string("weather.rain");
            case 9 -> LocalizedSystemStrings.getInstance().string("weather.heavyRain");
            case 10 -> LocalizedSystemStrings.getInstance().string("weather.freezingRain");
            case 11 -> LocalizedSystemStrings.getInstance().string("weather.heavyFreezingRain");
            case 12 -> LocalizedSystemStrings.getInstance().string("weather.sleet");
            case 13 -> LocalizedSystemStrings.getInstance().string("weather.heavySleet");
            case 14 -> LocalizedSystemStrings.getInstance().string("weather.lightSnowfall");
            case 15 -> LocalizedSystemStrings.getInstance().string("weather.snowfall");
            case 16 -> LocalizedSystemStrings.getInstance().string("weather.heavySnowfall");
            case 17 -> LocalizedSystemStrings.getInstance().string("weather.rainShower");
            case 18 -> LocalizedSystemStrings.getInstance().string("weather.heavyRainShower");
            case 19 -> LocalizedSystemStrings.getInstance().string("weather.sleetShower");
            case 20 -> LocalizedSystemStrings.getInstance().string("weather.heavySleetShower");
            case 21 -> LocalizedSystemStrings.getInstance().string("weather.snowShower");
            case 22 -> LocalizedSystemStrings.getInstance().string("weather.heavySnowShower");
            case 23 -> LocalizedSystemStrings.getInstance().string("weather.lightning");
            case 24 -> LocalizedSystemStrings.getInstance().string("weather.hail");
            case 25 -> LocalizedSystemStrings.getInstance().string("weather.thunderstorm");
            case 26 -> LocalizedSystemStrings.getInstance().string("weather.heavyThunderstorm");
            case 27 -> LocalizedSystemStrings.getInstance().string("weather.storm");
            default -> LocalizedSystemStrings.getInstance().string("weather.unknown");
        };
    }

    public static String getWindDirection(double value) {
        if (value < 12.25)
            return "N";
        if (value < 33.75)
            return "NNW";
        if (value < 56.25)
            return "NW";
        if (value < 78.75)
            return "WNW";
        if (value < 101.25)
            return "W";
        if (value < 123.75)
            return "WSW";
        if (value < 146.25)
            return "SW";
        if (value < 168.75)
            return "SSW";
        if (value < 191.25)
            return "S";
        if (value < 213.75)
            return "SSO";
        if (value < 236.25)
            return "SO";
        if (value < 258.75)
            return "OSO";
        if (value < 281.25)
            return "O";
        if (value < 303.75)
            return "ONO";
        if (value < 326.25)
            return "NO";
        if (value < 348.75)
            return "NNO";
        return "N";
    }

}
