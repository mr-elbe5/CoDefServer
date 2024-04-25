package de.elbe5.application;

import de.elbe5.base.JsonDeserializer;
import de.elbe5.base.Log;
import de.elbe5.configuration.CodefConfiguration;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.net.URL;

public class MeteostatClient {

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
}
