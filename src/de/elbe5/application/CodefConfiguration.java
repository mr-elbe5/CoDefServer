/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.application;

import jakarta.servlet.ServletContext;

public class CodefConfiguration {

    private static boolean showInactiveContent = false;

    private static boolean showNotified = false;

    public static boolean isShowInactiveContent() {
        return showInactiveContent;
    }

    public static void setShowInactiveContent(boolean showInactiveContent) {
        CodefConfiguration.showInactiveContent = showInactiveContent;
    }

    public static boolean showNotified() {
        return showNotified;
    }

    public static String getSafeInitParameter(ServletContext servletContext, String key){
        String s=servletContext.getInitParameter(key);
        return s==null ? "" : s;
    }

    public static void setConfigs(ServletContext servletContext) {
        showNotified = "true".equals(getSafeInitParameter(servletContext,"showNotified"));
    }

}
