/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.company;

import de.elbe5.base.LocalizedStrings;
import de.elbe5.base.BaseData;
import de.elbe5.request.*;
import de.elbe5.response.StatusResponse;
import de.elbe5.rights.GlobalRight;
import de.elbe5.servlet.Controller;
import de.elbe5.servlet.ControllerCache;
import de.elbe5.response.CloseDialogResponse;
import de.elbe5.response.IResponse;
import de.elbe5.response.ForwardResponse;

import jakarta.servlet.http.HttpServletResponse;

public class CompanyController extends Controller {

    public static final String KEY = "company";

    private static CompanyController instance = null;

    public static void setInstance(CompanyController instance) {
        CompanyController.instance = instance;
    }

    public static CompanyController getInstance() {
        return instance;
    }

    public static void register(CompanyController controller){
        setInstance(controller);
        ControllerCache.addController(controller.getKey(),getInstance());
    }

    @Override
    public String getKey() {
        return KEY;
    }

    public IResponse openCreateCompany(RequestData rdata) {
        assertSessionCall(rdata);
        assertRights(GlobalRight.hasGlobalUserEditRight(rdata.getLoginUser()));
        CompanyData data = new CompanyData();
        data.setCreateValues(rdata);
        data.setId(CompanyBean.getInstance().getNextId());
        rdata.setSessionObject("companyData", data);
        return showEditCompany();
    }

    public IResponse openEditCompany(RequestData rdata) {
        assertSessionCall(rdata);
        assertRights(GlobalRight.hasGlobalUserEditRight(rdata.getLoginUser()));
        int companyId = rdata.getId();
        CompanyData data = CompanyBean.getInstance().getCompany(companyId);
        data.setUpdateValues(rdata);
        rdata.setSessionObject("companyData", data);
        return showEditCompany();
    }

    public IResponse saveCompany(RequestData rdata) {
        assertSessionCall(rdata);
        assertRights(GlobalRight.hasGlobalUserEditRight(rdata.getLoginUser()));
        CompanyData data = (CompanyData) rdata.getSessionObject("companyData");
        if (data==null){
            return new StatusResponse(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        data.readRequestData(rdata, RequestType.backend);
        if (!rdata.checkFormErrors()) {
            return showEditCompany();
        }
        CompanyBean.getInstance().saveCompany(data);
        CompanyCache.setDirty();
        rdata.setMessage(LocalizedStrings.string("_companySaved"), RequestKeys.MESSAGE_TYPE_SUCCESS);
        return new CloseDialogResponse("/ctrl/admin/openPersonAdministration?companyId=" + data.getId());
    }

    public IResponse deleteCompany(RequestData rdata) {
        assertSessionCall(rdata);
        assertRights(GlobalRight.hasGlobalUserEditRight(rdata.getLoginUser()));
        int id = rdata.getId();
        if (id < BaseData.ID_MIN) {
            rdata.setMessage(LocalizedStrings.string("_notDeletable"), RequestKeys.MESSAGE_TYPE_ERROR);
            return new ForwardResponse("/ctrl/admin/openPersonAdministration");
        }
        CompanyBean.getInstance().deleteCompany(id);
        CompanyCache.setDirty();
        rdata.setMessage(LocalizedStrings.string("_companyDeleted"), RequestKeys.MESSAGE_TYPE_SUCCESS);
        return new ForwardResponse("/ctrl/admin/openPersonAdministration");
    }

    protected IResponse showEditCompany() {
        return new ForwardResponse("/WEB-INF/_jsp/company/editCompany.ajax.jsp");
    }
}
