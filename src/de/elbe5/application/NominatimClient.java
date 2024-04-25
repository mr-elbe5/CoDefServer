package de.elbe5.application;

import de.elbe5.base.JsonDeserializer;
import de.elbe5.base.Log;
import de.elbe5.base.StringHelper;
import de.elbe5.configuration.CodefConfiguration;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.net.URL;

public class NominatimClient {

    static public Coordinate getCoordinate(String countryCode, String city, String street){
        HttpsURLConnection connection = null;
        Coordinate coordinate = null;
        try {
            String sb = "https://nominatim.openstreetmap.org/search?country=" +
                    CodefConfiguration.getDefaultCountry() +
                    "&city=" +
                    StringHelper.toUrl(city) +
                    "&street=" +
                    StringHelper.toUrl(street) +
                    "&format=json&limit=1";
            URL url = new URL(sb);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("accept", "application/json");
            connection.setConnectTimeout(5000);
            connection.connect();
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                try {
                    JSONArray jsonArray = (JSONArray) new JsonDeserializer().deserialize(connection.getInputStream());
                    if (!jsonArray.isEmpty()){
                        JSONObject jsonObject = (JSONObject) jsonArray.get(0);
                        double lat = Double.parseDouble(jsonObject.get("lat").toString());
                        double lon = Double.parseDouble(jsonObject.get("lon").toString());
                        Log.log("received coordinates:" + lat + ", " + lon);
                        coordinate = new Coordinate(lat, lon);
                    }
                }
                catch (Exception e){
                    Log.error("unable to get coordinates from json");
                }
            }
        } catch (Exception e) {
            Log.error("no connection to nominatim", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return coordinate;
    }

}
