/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.user;

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
        List<Integer> projectIds = ((CodefUserData) data).getAllowedProjectIds();
        if (user.getProjectId()!=0 && !projectIds.contains(user.getProjectId())) {
            user.setProjectId(0);
            user.getSelectedCompanyIds().clear();
            CodefUserBean.getInstance().updateUserSettings(user);
        }
    }

    public IResponse openSyncFilter(RequestData rdata) {
        assertRights(rdata.isLoggedIn());
        return new ForwardResponse("/WEB-INF/_jsp/user/syncFilter.ajax.jsp");
    }

    public IResponse setSyncFilter(RequestData rdata) {
        assertRights(rdata.isLoggedIn());
        CodefUserData user = rdata.getLoginUser(CodefUserData.class);
        user.setProjectIds(rdata.getAttributes().getIntegerList("projectIds"));
        if (user.getProjectId() == 0 && !user.getSelectedProjectIds().isEmpty())
            user.setProjectId(user.getSelectedProjectIds().get(0));
        CodefUserBean.getInstance().updateUserSettings(user);
        return new CloseDialogResponse("/ctrl/content/show/" + rdata.getSafeId());
    }

    public IResponse openCompanyFilter(RequestData rdata) {
        assertRights(rdata.isLoggedIn());
        return new ForwardResponse("/WEB-INF/_jsp/user/companyFilter.ajax.jsp");
    }

    public IResponse setCompanyFilter(RequestData rdata) {
        assertRights(rdata.isLoggedIn());
        CodefUserData user = rdata.getLoginUser(CodefUserData.class);
        user.setCompanyIds(rdata.getAttributes().getIntegerList("companyIds"));
        CodefUserBean.getInstance().updateUserSettings(user);
        return new CloseDialogResponse("/ctrl/content/show/" + rdata.getSafeId());
    }

    public IResponse openViewFilter(RequestData rdata) {
        assertRights(rdata.isLoggedIn());
        return new ForwardResponse("/WEB-INF/_jsp/user/viewFilter.ajax.jsp");
    }

    public IResponse setViewFilter(RequestData rdata) {
        assertRights(rdata.isLoggedIn());
        CodefUserData user = rdata.getLoginUser(CodefUserData.class);
        user.setShowOpen(rdata.getAttributes().getBoolean("showOpen"));
        user.setShowDisputed(rdata.getAttributes().getBoolean("showDisputed"));
        user.setShowRejected(rdata.getAttributes().getBoolean("showRejected"));
        user.setShowDone(rdata.getAttributes().getBoolean("showDone"));
        user.setShowClosed(rdata.getAttributes().getBoolean("showClosed"));
        user.setProjectPhase(rdata.getAttributes().getString("projectPhase"));
        user.setShowOnlyRemainingWork(rdata.getAttributes().getBoolean("showOnlyRemainingWork"));
        CodefUserBean.getInstance().updateUserSettings(user);
        return new CloseDialogResponse("/ctrl/content/show/" + rdata.getSafeId());
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
