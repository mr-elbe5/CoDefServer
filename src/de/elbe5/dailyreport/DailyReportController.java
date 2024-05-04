/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.dailyreport;

import de.elbe5.base.BinaryFile;
import de.elbe5.base.Log;
import de.elbe5.content.ContentBean;
import de.elbe5.content.ContentCache;
import de.elbe5.content.ContentController;
import de.elbe5.project.ProjectData;
import de.elbe5.request.RequestData;
import de.elbe5.request.RequestType;
import de.elbe5.response.IResponse;
import de.elbe5.response.JsonResponse;
import de.elbe5.response.MemoryFileResponse;
import de.elbe5.response.StatusResponse;
import de.elbe5.user.UserData;
import jakarta.servlet.http.HttpServletResponse;

public class DailyReportController extends ContentController {

    public static final String KEY = "dailyreport";

    private static DailyReportController instance = null;

    public static void setInstance(DailyReportController instance) {
        DailyReportController.instance = instance;
    }

    public static DailyReportController getInstance() {
        return instance;
    }

    @Override
    public String getKey() {
        return KEY;
    }

    public IResponse getPdf(RequestData rdata) {
        int contentId = rdata.getId();
        BinaryFile file = new DailyReportPdfCreator().getProjectDailyReport(contentId, rdata);
        assert(file!=null);
        MemoryFileResponse view=new MemoryFileResponse(file);
        view.setForceDownload(true);
        return view;
    }

    // api

    public IResponse uploadReport(RequestData rdata){
        Log.log("uploadReport");
        assertApiCall(rdata);
        UserData user = rdata.getLoginUser();
        if (user==null)
            return new StatusResponse(HttpServletResponse.SC_UNAUTHORIZED);
        int reportId=rdata.getId();
        Log.log("remote report id = " + reportId);
        int projectId=rdata.getAttributes().getInt("projectId");
        ProjectData project=ContentCache.getContent(projectId, ProjectData.class);
        if (project == null || !project.hasUserReadRight(user)) {
            return new StatusResponse(HttpServletResponse.SC_UNAUTHORIZED);
        }
        DailyReport data = new DailyReport();
        data.setCreateValues(rdata, RequestType.api);
        data.setParentValues(project);
        data.readRequestData(rdata, RequestType.api);
        data.setNewId();
        Log.log("new report id = " + data.getId());
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
