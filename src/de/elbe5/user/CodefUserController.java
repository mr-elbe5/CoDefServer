/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.user;

import de.elbe5.application.ViewFilter;
import de.elbe5.content.ContentCache;
import de.elbe5.project.ProjectData;
import de.elbe5.request.RequestData;
import de.elbe5.response.ForwardResponse;
import de.elbe5.response.IResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CodefUserController extends UserController {

    @Override
    protected void initWebUser(UserData data, RequestData rdata){
        ViewFilter filter = ViewFilter.getFilter(rdata);
        boolean isEditor = data.hasGlobalContentEditRight();
        filter.setEditor(isEditor);
        filter.setCurrentUserId(data.getId());
        Map<String,String> cookieValues = rdata.readLoginCookies();
        if (cookieValues.containsKey("showClosed"))
            filter.setShowClosed(Boolean.parseBoolean(cookieValues.get("showClosed")));
        List<ProjectData> projects = ContentCache.getContents(ProjectData.class);
        List<Integer> projectIds = new ArrayList<>();
        for (ProjectData project : projects){
            if (project.hasUserEditRight(data)){
                projectIds.add(project.getId());
            }
        }
        filter.getOwnProjectIds().clear();
        filter.getOwnProjectIds().addAll(projectIds);
        switch (projectIds.size()){
            case 0:
                break;
            case 1:
                filter.setProjectId(projectIds.get(0));
                break;
            default:
                String s =  cookieValues.get("projectId");
                if (s!=null) {
                    int id = Integer.parseInt(s);
                    if (projectIds.contains(id))
                        filter.setProjectId(id);
                }
                break;

        }
        filter.initWatchedCompanies();
    }

    @Override
    protected IResponse showLoginHome() {
        return new ForwardResponse("/home.html");
    }

    @Override
    protected IResponse showLogoutHome() {
        return new ForwardResponse("/ctrl/user/openLogin");
    }

}
