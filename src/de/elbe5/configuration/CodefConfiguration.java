/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.configuration;

import de.elbe5.application.Configuration;
import jakarta.servlet.ServletContext;

public class CodefConfiguration extends Configuration {

    private static boolean showInactiveContent = false;

    private static boolean syncProjectCompamiesOnly = true;

    private final static ServerSettings serverSettings = new ServerSettings();

    public static boolean isShowInactiveContent() {
        return showInactiveContent;
    }

    public static void setShowInactiveContent(boolean showInactiveContent) {
        CodefConfiguration.showInactiveContent = showInactiveContent;
    }

    public static void setUseNotified(boolean useNotified) {
        serverSettings.useNotified = useNotified;
    }

    public static boolean showNotified() {
        return serverSettings.useNotified;
    }

    public static boolean syncProjectCompamiesOnly() {
        return syncProjectCompamiesOnly;
    }

    public static String getDefaultCountry() {
        return serverSettings.defaultCountry;
    }

    public static void setDefaultCountry(String defaultCountry) {
        serverSettings.defaultCountry = defaultCountry;
    }

    public static String getTimeZoneName() {
        return serverSettings.timeZoneName;
    }

    public static void setTimeZoneName(String timeZoneName) {
        serverSettings.timeZoneName = timeZoneName;
    }

    public static String getMeteoStatKey() {
        return serverSettings.meteoStatKey;
    }

    public static void setMeteoStatKey(String meteoStatKey) {
        serverSettings.meteoStatKey = meteoStatKey;
    }

    public static ServerSettings getServerSettings() {
        return serverSettings;
    }

    public static void setSyncProjectCompamiesOnly(boolean syncProjectCompamiesOnly) {
        CodefConfiguration.syncProjectCompamiesOnly = syncProjectCompamiesOnly;
    }

    public static void initialize(ServletContext context){
        Configuration.initialize(context);
        showInactiveContent = getSafeBoolean(context, "showInactiveContent");
        syncProjectCompamiesOnly = getSafeBoolean(context, "syncProjectCompanies");
        serverSettings.useNotified = getSafeBoolean(context, "useNotified");
        serverSettings.defaultCountry = getSafeString(context, "countryCode");
        serverSettings.timeZoneName = getSafeString(context, "timeZoneName");
        serverSettings.meteoStatKey = getSafeString(context, "meteostatKey");
        System.out.println("extended static configuration loaded");
    }
}
