/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.project;

import de.elbe5.base.BinaryFile;
import de.elbe5.base.Log;
import de.elbe5.content.ContentBean;
import de.elbe5.content.ContentCache;
import de.elbe5.content.ContentController;
import de.elbe5.request.RequestData;
import de.elbe5.request.RequestType;
import de.elbe5.response.*;
import de.elbe5.user.CodefUserData;
import de.elbe5.user.UserData;
import jakarta.servlet.http.HttpServletResponse;

public class ProjectController extends ContentController {

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

    public IResponse getReport(RequestData rdata) {
        boolean includeStatusChanges = rdata.getAttributes().getBoolean("includeStatusChanges");
        int contentId = rdata.getId();
        BinaryFile file = new ProjectPdfCreator().getProjectReport(contentId, rdata, includeStatusChanges);
        assert(file!=null);
        MemoryFileResponse view=new MemoryFileResponse(file);
        view.setForceDownload(true);
        return view;
    }

    public IResponse getCsv(RequestData rdata) {
        boolean includeStatusChanges = rdata.getAttributes().getBoolean("includeStatusChanges");
        int contentId = rdata.getId();
        BinaryFile file = new ProjectCsvCreator().getCsvFile(contentId);
        assert(file!=null);
        MemoryFileResponse view=new MemoryFileResponse(file);
        view.setForceDownload(true);
        return view;
    }

    public IResponse sort(RequestData rdata) {
        CodefUserData user = rdata.getLoginUser(CodefUserData.class);
        if (user==null)
            return new StatusResponse(HttpServletResponse.SC_UNAUTHORIZED);
        int sortType = rdata.getAttributes().getInt("sortType");
        user.setSortType(sortType);
        return show(rdata);
    }

    public IResponse uploadProject(RequestData rdata){
        Log.log("uploadProject");
        assertApiCall(rdata);
        UserData user = rdata.getLoginUser();
        if (user==null)
            return new StatusResponse(HttpServletResponse.SC_UNAUTHORIZED);
        int projectId=rdata.getId();
        Log.log("remote project id = " + projectId);
        ProjectData data = new ProjectData();
        data.setCreateValues(rdata, RequestType.api);
        data.setParentValues(ContentCache.getContentRoot());
        data.readRequestData(rdata, RequestType.api);
        data.setNewId();
        Log.log("new project id = " + data.getId());
        Log.log(data.getJson().toJSONString());
        if (!ContentBean.getInstance().saveContent(data)) {
            return new StatusResponse(HttpServletResponse.SC_BAD_REQUEST);
        }
        data.setNew(false);
        data.setEditMode(false);
        ContentCache.setDirty();
        return new JsonResponse(data.getIdJson().toJSONString());
    }

}
