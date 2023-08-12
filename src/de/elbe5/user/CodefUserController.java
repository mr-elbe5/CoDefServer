/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.user;

import de.elbe5.codef.ViewFilter;
import de.elbe5.codef.project.ProjectBean;
import de.elbe5.request.RequestData;

import java.util.List;
import java.util.Map;

public class CodefUserController extends UserController {

    @Override
    protected void initWebUser(UserData data, RequestData rdata){
        ViewFilter filter = ViewFilter.getFilter(rdata);
        boolean isEditor = data.hasContentEditRight();
        filter.setEditor(isEditor);
        filter.setCurrentUserId(data.getId());
        Map<String,String> cookieValues = rdata.readLoginCookies();
        if (cookieValues.containsKey("showClosed"))
            filter.setShowClosed(Boolean.parseBoolean(cookieValues.get("showClosed")));
        List<Integer> projectIds= ProjectBean.getInstance().getUserProjectIds(data.getId(),isEditor);
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
        filter.initWatchedUsers();
    }

}
