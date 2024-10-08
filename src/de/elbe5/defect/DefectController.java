/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.defect;

import de.elbe5.base.BinaryFile;
import de.elbe5.base.Log;
import de.elbe5.base.Token;
import de.elbe5.request.RequestType;
import de.elbe5.rights.GlobalRight;
import de.elbe5.unit.UnitData;
import de.elbe5.content.*;
import de.elbe5.file.FileBean;
import de.elbe5.file.ImageData;
import de.elbe5.request.ContentRequestKeys;
import de.elbe5.request.RequestData;
import de.elbe5.request.RequestKeys;
import de.elbe5.response.*;
import de.elbe5.servlet.ControllerCache;
import de.elbe5.user.UserData;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;

public class DefectController extends ContentController {

    public static final String KEY = "defect";

    private static DefectController instance = null;

    public static void setInstance(DefectController instance) {
        DefectController.instance = instance;
    }

    public static DefectController getInstance() {
        return instance;
    }

    public static void register(DefectController controller){
        setInstance(controller);
        ControllerCache.addController(controller.getKey(),getInstance());
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public IResponse openCreateBackendContent(RequestData rdata) {
        assertLoggedInSessionCall(rdata);
        assertRights(GlobalRight.hasGlobalContentEditRight(rdata.getLoginUser()));
        int parentId = rdata.getAttributes().getInt("parentId");
        boolean isRemainingWork=rdata.getAttributes().getBoolean("remainingWork");
        ContentData parentData = ContentCache.getContent(parentId);
        String type = rdata.getAttributes().getString("type");
        DefectData data = new DefectData();
        data.setRemainingWork(isRemainingWork);
        data.setCreateValues(rdata, RequestType.backend);
        data.setParentValues(parentData);
        rdata.setSessionObject(ContentRequestKeys.KEY_CONTENT, data);
        return showEditBackendContent(data);
    }

    @Override
    public IResponse openCreateFrontendContent(RequestData rdata) {
        int parentId=rdata.getAttributes().getInt("parentId");
        UnitData parent= ContentCache.getContent(parentId, UnitData.class);
        assert parent != null;
        assertRights(parent.hasUserEditRight(rdata.getLoginUser()));
        DefectData data = new DefectData();
        data.setCreateValues(rdata, RequestType.frontend);
        data.setParentValues(parent);
        data.setEditMode(true);
        rdata.setSessionObject(ContentRequestKeys.KEY_CONTENT,data);
        return new ContentResponse(data);
    }

    @Override
    public IResponse openEditFrontendContent(RequestData rdata) {
        int defectId=rdata.getId();
        DefectData data = ContentBean.getInstance().getContent(defectId,DefectData.class);
        assertRights(data.hasUserEditRight(rdata.getLoginUser()));
        DefectData cachedData = ContentCache.getContent(data.getId(), DefectData.class);
        data.setUpdateValues(cachedData, rdata);
        rdata.setSessionObject(ContentRequestKeys.KEY_CONTENT,data);
        data.setEditMode(true);
        return new ContentResponse(data);
    }

    //frontend
    @Override
    public IResponse saveFrontendContent(RequestData rdata) {
        int contentId=rdata.getId();
        DefectData data=rdata.getSessionObject(ContentRequestKeys.KEY_CONTENT,DefectData.class);
        assert(data != null && data.getId() == contentId);
        assertRights(data.hasUserEditRight(rdata.getLoginUser()));
        data.readRequestData(rdata, RequestType.frontend);
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
        rdata.setMessage($S("_contentSaved"), RequestKeys.MESSAGE_TYPE_SUCCESS);
        return show(rdata);
    }

    public IResponse closeDefect(RequestData rdata) {
        int contentId=rdata.getId();
        DefectData data = ContentBean.getInstance().getContent(contentId,DefectData.class);
        assertRights(data.hasUserEditRight(rdata.getLoginUser()));
        data.setCloseDate(LocalDate.now());
        data.setUpdateValues(rdata);
        if (!DefectBean.getInstance().closeDefect(data)) {
            setSaveError(rdata);
            return new ContentResponse(data);
        }
        ContentCache.setDirty();
        rdata.setMessage($S("_defectClosed"), RequestKeys.MESSAGE_TYPE_SUCCESS);
        return new ForwardResponse("/ctrl/content/show/" + data.getParentId());
    }

    public IResponse showCroppedDefectPlan(RequestData rdata) {
        int id = rdata.getId();
        DefectData data=ContentCache.getContent(id,DefectData.class);
        assert(data!=null);
        if (!data.hasUserReadRight(rdata.getLoginUser())) {
            String token = rdata.getAttributes().getString("token");
            assertRights(Token.matchToken(id, token));
        }
        double x=rdata.getAttributes().getDouble("x",data.getPositionX());
        double y=rdata.getAttributes().getDouble("y",data.getPositionY());
        ImageData plan = FileBean.getInstance().getFile(data.getPlanId(),true,ImageData.class);
        byte[] arrowBytes = FileBean.getInstance().getImageBytes(data.getIconName());
        BinaryFile file = data.createCroppedDefectPlan(plan,arrowBytes,data.getId(),x,y);
        assert(file!=null);
        return new MemoryFileResponse(file);
    }

    public IResponse openFullDefectPlan(RequestData rdata) {
        return new ForwardResponse("/WEB-INF/_jsp/defect/defectPlan.ajax.jsp");
    }

    public IResponse showFullDefectPlan(RequestData rdata) {
        int id = rdata.getId();
        DefectData data=ContentCache.getContent(id,DefectData.class);
        assert(data!=null);
        if (!data.hasUserReadRight(rdata.getLoginUser())) {
            String token = rdata.getAttributes().getString("token");
            assertRights(Token.matchToken(id, token));
        }
        double x=rdata.getAttributes().getDouble("x",data.getPositionX());
        double y=rdata.getAttributes().getDouble("y",data.getPositionY());
        ImageData plan = FileBean.getInstance().getFile(data.getPlanId(),true,ImageData.class);
        byte[] arrowBytes = FileBean.getInstance().getImageBytes(data.getIconName());
        BinaryFile file = data.createFullDefectPlan(plan,arrowBytes,data.getId(),x,y);
        assert(file!=null);
        return new MemoryFileResponse(file);
    }

    public IResponse getPdfFile(RequestData rdata) {
        int contentId = rdata.getId();
        DefectData data= ContentCache.getContent(contentId,DefectData.class);
        assert(data!=null);
        BinaryFile file =new DefectPdfCreator().getDefectPdfFile(data, rdata);
        assert(file!=null);
        MemoryFileResponse view=new MemoryFileResponse(file);
        view.setForceDownload(true);
        return view;
    }

    //api

    public IResponse uploadDefect(RequestData rdata) {
        Log.log("uploadDefect");
        UserData user = rdata.getLoginUser();
        if (user==null)
            return new StatusResponse(HttpServletResponse.SC_UNAUTHORIZED);
        int defectId = rdata.getId();
        Log.log("remote defect id = " + defectId);
        int unitId=rdata.getAttributes().getInt("unitId");
        UnitData unit=ContentCache.getContent(unitId, UnitData.class);
        if (unit == null || !unit.hasUserReadRight(user)) {
            return new StatusResponse(HttpServletResponse.SC_UNAUTHORIZED);
        }
        DefectData data = new DefectData();
        data.setCreateValues(rdata, RequestType.api);
        data.setParentValues(unit);
        data.readRequestData(rdata, RequestType.api);
        data.setNewId();
        if (!ContentBean.getInstance().saveContent(data)) {
            return new StatusResponse(HttpServletResponse.SC_BAD_REQUEST);
        }
        data.setNew(false);
        data.setEditMode(false);
        ContentCache.setDirty();
        return new JsonResponse(data.getIdJson().toJSONString());
    }

}
