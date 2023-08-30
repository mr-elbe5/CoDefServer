/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.filter;

import de.elbe5.application.ViewFilter;
import de.elbe5.request.RequestData;
import de.elbe5.response.CloseDialogResponse;
import de.elbe5.response.ForwardResponse;
import de.elbe5.response.IResponse;
import de.elbe5.servlet.Controller;
import de.elbe5.servlet.ControllerCache;

public class FilterController extends Controller {

    public static final String KEY = "filter";

    private static FilterController instance = null;

    public static void setInstance(FilterController instance) {
        FilterController.instance = instance;
    }

    public static FilterController getInstance() {
        return instance;
    }

    public static void register(FilterController controller){
        setInstance(controller);
        ControllerCache.addController(controller.getKey(),getInstance());
    }

    @Override
    public String getKey() {
        return KEY;
    }

    public IResponse openCompanyFilter(RequestData rdata) {
        assertRights(rdata.isLoggedIn());
        int contentId=rdata.getId();
        return new ForwardResponse("/WEB-INF/_jsp/filter/companyFilter.ajax.jsp");
    }

    public IResponse setCompanyFilter(RequestData rdata) {
        assertRights(rdata.isLoggedIn());
        ViewFilter filter = ViewFilter.getFilter(rdata);
        filter.setWatchedIds(rdata.getAttributes().getIntegerList("watchedIds"));
        return new CloseDialogResponse("/ctrl/content/show/" + filter.getProjectId());
    }

    public IResponse openStatusFilter(RequestData rdata) {
        assertRights(rdata.isLoggedIn());
        return new ForwardResponse("/WEB-INF/_jsp/filter/statusFilter.ajax.jsp");
    }

    public IResponse setStatusFilter(RequestData rdata) {
        assertRights(rdata.isLoggedIn());
        ViewFilter filter = ViewFilter.getFilter(rdata);
        filter.setShowClosed(rdata.getAttributes().getBoolean("showClosed"));
        return new CloseDialogResponse("/ctrl/content/show/" + filter.getProjectId());
    }

}
