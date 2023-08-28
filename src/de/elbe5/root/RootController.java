/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.root;

import de.elbe5.application.ViewFilter;
import de.elbe5.content.ContentCache;
import de.elbe5.content.ContentData;
import de.elbe5.content.ContentResponse;
import de.elbe5.project.ProjectBean;
import de.elbe5.content.ContentController;
import de.elbe5.project.ProjectData;
import de.elbe5.request.RequestData;
import de.elbe5.response.*;
import de.elbe5.user.UserCache;
import de.elbe5.user.UserData;
import jakarta.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;

public class RootController extends ContentController {

    public static final String KEY = "root";

    public static int COOKIE_EXPIRATION_DAYS = 90;

    private static RootController instance = null;

    public static void setInstance(RootController instance) {
        RootController.instance = instance;
    }

    public static RootController getInstance() {
        return instance;
    }

    @Override
    public String getKey() {
        return KEY;
    }

    public IResponse getData(RequestData rdata) {
        UserData user = rdata.getLoginUser();
        if (user==null)
            return new StatusResponse(HttpServletResponse.SC_UNAUTHORIZED);
        RootData data = ContentCache.getContent(ContentData.ID_ROOT, RootData.class);
        if (data==null)
            return new StatusResponse(HttpServletResponse.SC_NOT_FOUND);
        JSONObject json = data.getAllDataJson(rdata);
        return new JsonResponse(json.toJSONString());
    }

    public IResponse uploadData(RequestData rdata) {
        UserData user = rdata.getLoginUser();
        if (user==null)
            return new StatusResponse(HttpServletResponse.SC_UNAUTHORIZED);
        RootData data = new RootData();
        data.readRequestData(rdata);
        return new JsonResponse(data.getJson().toJSONString());
    }

    public IResponse setEntryPoint(RequestData rdata){
        assertSessionCall(rdata);
        //Log.log("setEntryPoint");
        int contentId = rdata.getId();
        ContentData data = ContentCache.getContent(contentId);
        checkRights(data.hasUserReadRight(rdata));
        ProjectData project = findProject(data);
        if (project!=null){
            ViewFilter filter = ViewFilter.getFilter(rdata);
            filter.setProjectId(project.getId());
            rdata.addLoginCookie("projectId", Integer.toString(filter.getProjectId()),COOKIE_EXPIRATION_DAYS);
        }
        return data.getDefaultView();
    }

    private ProjectData findProject(ContentData content){
        ContentData data = content;
        while (data!=null){
            if (data instanceof ProjectData project){
                return project;
            }
            data = data.getParent();
        }
        return null;
    }

}
