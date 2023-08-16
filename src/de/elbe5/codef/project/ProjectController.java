/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.codef.project;

import de.elbe5.base.BinaryFile;
import de.elbe5.base.Log;
import de.elbe5.codef.ViewFilter;
import de.elbe5.codef.defect.DefectData;
import de.elbe5.codef.defectstatus.DefectStatusData;
import de.elbe5.codef.unit.PlanImageData;
import de.elbe5.codef.unit.UnitData;
import de.elbe5.content.ContentCache;
import de.elbe5.content.ContentController;
import de.elbe5.content.ContentResponse;
import de.elbe5.file.ImageData;
import de.elbe5.group.GroupBean;
import de.elbe5.group.GroupData;
import de.elbe5.request.RequestData;
import de.elbe5.response.*;
import de.elbe5.user.UserCache;
import de.elbe5.user.UserData;
import jakarta.servlet.http.HttpServletResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.List;

public class ProjectController extends ContentController {

    public static int COOKIE_EXPIRATION_DAYS = 90;

    public static final String KEY = "project";

    private static ProjectController instance = null;

    public static void setInstance(ProjectController instance) {
        ProjectController.instance = instance;
    }

    public static ProjectController getInstance() {
        return instance;
    }

    @Override
    public String getKey() {
        return KEY;
    }

    public IResponse openWatchFilter(RequestData rdata) {
        checkRights(rdata.isLoggedIn());
        int contentId=rdata.getId();
        return new ForwardResponse("/WEB-INF/_jsp/codef/project/watchFilter.ajax.jsp");
    }

    public IResponse setWatchFilter(RequestData rdata) {
        checkRights(rdata.isLoggedIn());
        ViewFilter filter = ViewFilter.getFilter(rdata);
        filter.setWatchedIds(rdata.getAttributes().getIntegerList("watchedIds"));
        return new CloseDialogResponse("/ctrl/content/show/" + filter.getProjectId());
    }

    public IResponse updateWatchedUsers(RequestData rdata) {
        checkRights(rdata.isLoggedIn());
        return new ForwardResponse("/WEB-INF/_jsp/codef/project/projectUsers.ajax.jsp");
    }

    public IResponse openStateFilter(RequestData rdata) {
        checkRights(rdata.isLoggedIn());
        return new ForwardResponse("/WEB-INF/_jsp/codef/project/stateFilter.ajax.jsp");
    }

    public IResponse setStateFilter(RequestData rdata) {
        checkRights(rdata.isLoggedIn());
        ViewFilter filter = ViewFilter.getFilter(rdata);
        filter.setShowClosed(rdata.getAttributes().getBoolean("showClosed"));
        return new CloseDialogResponse("/ctrl/content/show/" + filter.getProjectId());
    }

    public IResponse selectProject(RequestData rdata) {
        checkRights(rdata.isLoggedIn());
        int projectId=rdata.getId();
        ViewFilter filter = ViewFilter.getFilter(rdata);
        filter.setProjectId(projectId);
        rdata.addLoginCookie("projectId", Integer.toString(filter.getProjectId()),COOKIE_EXPIRATION_DAYS);
        ProjectData data= ContentCache.getContent(projectId,ProjectData.class);
        return new ContentResponse(data);
    }

    public IResponse getReport(RequestData rdata) {
        boolean includeComments = rdata.getAttributes().getBoolean("includeComments");
        int contentId = rdata.getId();
        BinaryFile file = ProjectPdfBean.getInstance().getProjectReport(contentId, rdata, includeComments);
        assert(file!=null);
        MemoryFileResponse view=new MemoryFileResponse(file);
        view.setForceDownload(true);
        return view;
    }

    public IResponse sort(RequestData rdata) {
        int sortType = rdata.getAttributes().getInt("sortType");
        ViewFilter filter = ViewFilter.getFilter(rdata);
        filter.setSortType(sortType);
        return show(rdata);
    }

}
