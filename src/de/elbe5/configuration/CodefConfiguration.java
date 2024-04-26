/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.configuration;

public class CodefConfiguration {

    private static boolean showInactiveContent = false;

    private static boolean useNotified = false;

    private static boolean syncProjectCompamiesOnly = true;

    private static String defaultCountry = "de";

    private static String timeZoneName = "Europe/Berlin";

    private static String meteoStatKey = "";

    public static boolean isShowInactiveContent() {
        return showInactiveContent;
    }

    public static void setShowInactiveContent(boolean showInactiveContent) {
        CodefConfiguration.showInactiveContent = showInactiveContent;
    }

    public static void setUseNotified(boolean useNotified) {
        CodefConfiguration.useNotified = useNotified;
    }

    public static boolean showNotified() {
        return useNotified;
    }

    public static boolean syncProjectCompamiesOnly() {
        return syncProjectCompamiesOnly;
    }

    public static String getDefaultCountry() {
        return defaultCountry;
    }

    public static void setDefaultCountry(String defaultCountry) {
        CodefConfiguration.defaultCountry = defaultCountry;
    }

    public static String getTimeZoneName() {
        return timeZoneName;
    }

    public static void setTimeZoneName(String timeZoneName) {
        CodefConfiguration.timeZoneName = timeZoneName;
    }

    public static String getMeteoStatKey() {
        return meteoStatKey;
    }

    public static void setMeteoStatKey(String meteoStatKey) {
        CodefConfiguration.meteoStatKey = meteoStatKey;
    }

    public static void setSyncProjectCompamiesOnly(boolean syncProjectCompamiesOnly) {
        CodefConfiguration.syncProjectCompamiesOnly = syncProjectCompamiesOnly;
    }
}
