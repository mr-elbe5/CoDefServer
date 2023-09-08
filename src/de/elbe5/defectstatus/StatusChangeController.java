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
import de.elbe5.response.*;
import de.elbe5.servlet.ControllerCache;
import de.elbe5.user.UserData;
import jakarta.servlet.http.HttpServletResponse;

public class StatusChangeController extends ContentController {

    public static final String KEY = "defectstatus";

    private static StatusChangeController instance = null;

    public static void setInstance(StatusChangeController instance) {
        StatusChangeController.instance = instance;
    }

    public static StatusChangeController getInstance() {
        return instance;
    }

    public static void register(StatusChangeController controller){
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
        assertRights(parent != null && parent.hasUserEditRight(rdata.getLoginUser()));
        StatusChangeData data = new StatusChangeData();
        data.setCreateValues(parent, rdata);
        rdata.setSessionObject(ContentRequestKeys.KEY_CONTENT,data);
        return new ForwardResponse(data.getFrontendEditJsp());
    }

    public IResponse openEditFrontendContent(RequestData rdata) {
        int statusId=rdata.getId();
        StatusChangeData data = ContentData.getCurrentContent(rdata, StatusChangeData.class);
        assertRights(data.hasUserEditRight(rdata.getLoginUser()));
        rdata.setSessionObject(ContentRequestKeys.KEY_CONTENT,data);
        return new ForwardResponse(data.getFrontendEditJsp());
    }

    //frontend
    public IResponse saveFrontendContent(RequestData rdata) {
        int contentId=rdata.getId();
        StatusChangeData data= ContentData.getCurrentContent(rdata, StatusChangeData.class);
        assert(data != null && data.getId() == contentId);
        assertRights(data.hasUserEditRight(rdata.getLoginUser()));
        if (data.isNew())
            data.readFrontendCreateRequestData(rdata);
        else
            data.readFrontendUpdateRequestData(rdata);
        if (!rdata.checkFormErrors()) {
            return new ContentResponse(data);
        }
        data.setChangerId(rdata.getUserId());
        if (!ContentBean.getInstance().saveContent(data)) {
            setSaveError(rdata);
            return new ContentResponse(data);
        }
        data.setNew(false);
        data.setEditMode(false);
        ContentCache.setDirty();
        rdata.setMessage(LocalizedStrings.string("_contentSaved"), RequestKeys.MESSAGE_TYPE_SUCCESS);
        rdata.setId(data.getParentId());
        return show(rdata);
    }

    //api

    public IResponse uploadStatusChange(RequestData rdata) {
        Log.log("uploadStatusChange");
        UserData user = rdata.getLoginUser();
        if (user == null)
            return new StatusResponse(HttpServletResponse.SC_UNAUTHORIZED);
        int defectId = rdata.getId();
        DefectData defect = ContentCache.getContent(defectId, DefectData.class);
        if (defect == null || !defect.hasUserReadRight(user)) {
            return new StatusResponse(HttpServletResponse.SC_UNAUTHORIZED);
        }
        StatusChangeData data = new StatusChangeData();
        data.setCreateValues(defect, rdata);
        data.readApiRequestData(rdata);
        if (!StatusChangeBean.getInstance().saveContent(data)) {
            return new StatusResponse(HttpServletResponse.SC_BAD_REQUEST);
        }
        data.setNew(false);
        ContentCache.setDirty();
        return new JsonResponse(getIdJson(data.getId()).toJSONString());
    }

}
