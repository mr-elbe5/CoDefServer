/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.user;

import de.elbe5.content.ContentCache;
import de.elbe5.project.ProjectData;
import de.elbe5.request.RequestData;
import de.elbe5.response.CloseDialogResponse;
import de.elbe5.response.ForwardResponse;
import de.elbe5.response.IResponse;
import de.elbe5.servlet.ResponseException;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

public class CodefUserController extends UserController {

    protected UserData getNewUserData(){
        return new CodefUserData();
    }

    @Override
    protected void initWebUser(UserData data, RequestData rdata){
        if (!(data instanceof CodefUserData user)){
            throw new ResponseException(HttpServletResponse.SC_BAD_REQUEST);
        }
        List<ProjectData> projects = ContentCache.getContents(ProjectData.class);
        user.getOwnProjectIds().clear();
        for (ProjectData project : projects){
            if (project.hasUserEditRight(data)){
                user.getOwnProjectIds().add(project.getId());
            }
        }
        if (user.getProjectId()!=0 && !user.getOwnProjectIds().contains(user.getProjectId())) {
            user.setProjectId(0);
            user.getCompanyIds().clear();
            CodefUserBean.getInstance().updateViewSettings(user);
        }
    }

    public IResponse openCompanyFilter(RequestData rdata) {
        assertRights(rdata.isLoggedIn());
        int contentId=rdata.getId();
        return new ForwardResponse("/WEB-INF/_jsp/user/companyFilter.ajax.jsp");
    }

    public IResponse setCompanyFilter(RequestData rdata) {
        assertRights(rdata.isLoggedIn());
        CodefUserData user = rdata.getLoginUser(CodefUserData.class);
        user.setCompanyIds(rdata.getAttributes().getIntegerList("companyIds"));
        CodefUserBean.getInstance().updateViewSettings(user);
        return new CloseDialogResponse("/ctrl/content/show/" + user.getProjectId());
    }

    public IResponse openViewFilter(RequestData rdata) {
        assertRights(rdata.isLoggedIn());
        return new ForwardResponse("/WEB-INF/_jsp/user/viewFilter.ajax.jsp");
    }

    public IResponse setViewFilter(RequestData rdata) {
        assertRights(rdata.isLoggedIn());
        CodefUserData user = rdata.getLoginUser(CodefUserData.class);
        user.setShowClosed(rdata.getAttributes().getBoolean("showClosed"));
        user.setViewRestriction(rdata.getAttributes().getString("viewRestriction"));
        CodefUserBean.getInstance().updateViewSettings(user);
        return new CloseDialogResponse("/ctrl/content/show/" + user.getProjectId());
    }

    @Override
    protected IResponse showLoginHome(RequestData rdata) {
        CodefUserData user = rdata.getLoginUser(CodefUserData.class);
        if (user.getProjectId()!=0){
            return new ForwardResponse("/ctrl/content/show/" + user.getProjectId());
        }
        return new ForwardResponse("/home.html");
    }

    @Override
    protected IResponse showLogoutHome() {
        return new ForwardResponse("/ctrl/user/openLogin");
    }

}
