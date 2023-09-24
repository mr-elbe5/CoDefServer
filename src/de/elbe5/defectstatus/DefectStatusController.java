/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.defectstatus;

import de.elbe5.base.LocalizedStrings;
import de.elbe5.base.Log;
import de.elbe5.defect.DefectData;
import de.elbe5.content.*;
import de.elbe5.request.ContentRequestKeys;
import de.elbe5.request.RequestData;
import de.elbe5.request.RequestKeys;
import de.elbe5.request.RequestType;
import de.elbe5.response.*;
import de.elbe5.servlet.ControllerCache;
import de.elbe5.user.UserData;
import jakarta.servlet.http.HttpServletResponse;

public class DefectStatusController extends ContentController {

    public static final String KEY = "defectstatus";

    private static DefectStatusController instance = null;

    public static void setInstance(DefectStatusController instance) {
        DefectStatusController.instance = instance;
    }

    public static DefectStatusController getInstance() {
        return instance;
    }

    public static void register(DefectStatusController controller){
        setInstance(controller);
        ControllerCache.addController(controller.getKey(),getInstance());
    }

    @Override
    public String getKey() {
        return KEY;
    }

    public IResponse openCreateFrontendContent(RequestData rdata) {
        int parentId=rdata.getAttributes().getInt("parentId");
        DefectData parent = ContentCache.getContent(parentId, DefectData.class);
        assert parent != null;
        assertRights(parent.hasUserEditRight(rdata.getLoginUser()));
        DefectStatusData data = new DefectStatusData();
        data.setCreateValues(rdata, RequestType.frontend);
        data.setParentValues(parent);
        rdata.setSessionObject(ContentRequestKeys.KEY_CONTENT,data);
        return new ForwardResponse(data.getFrontendEditJsp());
    }

    public IResponse openEditFrontendContent(RequestData rdata) {
        int statusId=rdata.getId();
        DefectStatusData data = ContentData.getCurrentContent(rdata, DefectStatusData.class);
        assertRights(data.hasUserEditRight(rdata.getLoginUser()));
        rdata.setSessionObject(ContentRequestKeys.KEY_CONTENT,data);
        return new ForwardResponse(data.getFrontendEditJsp());
    }

    //frontend
    public IResponse saveFrontendContent(RequestData rdata) {
        int contentId=rdata.getId();
        DefectStatusData data= ContentData.getCurrentContent(rdata, DefectStatusData.class);
        assert(data != null && data.getId() == contentId);
        DefectData defect= ContentCache.getContent(data.getParentId(), DefectData.class);
        assert(defect != null);
        assertRights(defect.hasUserEditRight(rdata.getLoginUser()));
        data.readRequestData(rdata, RequestType.frontend);
        if (!rdata.checkFormErrors()) {
            return new ForwardResponse(data.getFrontendEditJsp());
        }
        data.setChangerId(rdata.getUserId());
        if (!ContentBean.getInstance().saveContent(data)) {
            setSaveError(rdata);
            return new ContentResponse(defect);
        }
        data.setNew(false);
        data.setEditMode(false);
        ContentCache.setDirty();
        rdata.setMessage(LocalizedStrings.string("_contentSaved"), RequestKeys.MESSAGE_TYPE_SUCCESS);
        rdata.setId(data.getParentId());
        return show(rdata);
    }

    //api

    public IResponse uploadStatusData(RequestData rdata) {
        Log.log("uploadStatusData");
        UserData user = rdata.getLoginUser();
        if (user == null)
            return new StatusResponse(HttpServletResponse.SC_UNAUTHORIZED);
        int defectId = rdata.getAttributes().getInt("defectId");
        int statusDataId = rdata.getId();
        Log.info("remote status data id = " + statusDataId);
        DefectData defect = ContentCache.getContent(defectId, DefectData.class);
        if (defect == null || !defect.hasUserReadRight(user)) {
            return new StatusResponse(HttpServletResponse.SC_UNAUTHORIZED);
        }
        DefectStatusData data = new DefectStatusData();
        data.setCreateValues(rdata, RequestType.api);
        data.setParentValues(defect);
        data.readRequestData(rdata, RequestType.api);
        data.setNewId();
        Log.info("new status data id = " + data.getId());
        if (!DefectStatusBean.getInstance().saveContent(data)) {
            return new StatusResponse(HttpServletResponse.SC_BAD_REQUEST);
        }
        data.setNew(false);
        ContentCache.setDirty();
        return new JsonResponse(data.getIdJson().toJSONString());
    }

}
