package de.elbe5.configuration;

import de.elbe5.base.IJsonData;
import de.elbe5.base.JsonObject;

public class ServerSettings implements IJsonData {

    public boolean useNotified = false;
    public String defaultCountry = "de";
    public String timeZoneName = "Europe/Berlin";
    public String meteoStatKey = "";

    @Override
    public JsonObject getJson(){
        return new JsonObject()
                .add("useNotified", useNotified)
                .add("country", defaultCountry)
                .add("timeZoneName", timeZoneName)
                .add("meteoStatKey", meteoStatKey);
    }

}
