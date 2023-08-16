/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.codef.root;

import de.elbe5.codef.project.ProjectBean;
import de.elbe5.content.ContentController;
import de.elbe5.request.RequestData;
import de.elbe5.response.*;
import de.elbe5.user.UserCache;
import de.elbe5.user.UserData;
import jakarta.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;

public class RootController extends ContentController {

    public static final String KEY = "root";

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

    public IResponse getProjects(RequestData rdata) {
        /*UserData user = rdata.getLoginUser();
        if (user==null)
            return new StatusResponse(HttpServletResponse.SC_UNAUTHORIZED);
         */
        UserData user = UserCache.getUser(UserData.ID_ROOT);
        JSONObject json = getProjectsJson(user);
        return new JsonResponse(json.toJSONString());
    }

    private JSONObject getProjectsJson(UserData user) {
        RootData data = new RootData();
        data.projectIds= ProjectBean.getInstance().getUserProjectIds(user.getId(),user.hasContentEditRight());
        return data.getJson();
    }


}
