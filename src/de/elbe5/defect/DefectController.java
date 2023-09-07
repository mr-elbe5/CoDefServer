/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.defect;

import de.elbe5.base.BinaryFile;
import de.elbe5.base.LocalizedStrings;
import de.elbe5.base.Log;
import de.elbe5.base.Token;
import de.elbe5.unit.UnitData;
import de.elbe5.content.*;
import de.elbe5.file.FileBean;
import de.elbe5.file.ImageBean;
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

    public IResponse openCreateFrontendContent(RequestData rdata) {
        int parentId=rdata.getAttributes().getInt("parentId");
        UnitData parent= ContentCache.getContent(parentId, UnitData.class);
        assertRights(parent != null && parent.hasUserEditRight(rdata.getLoginUser()));
        DefectData data = new DefectData();
        data.setCreateValues(parent, rdata);
        data.setEditMode(true);
        rdata.setSessionObject(ContentRequestKeys.KEY_CONTENT,data);
        return new ContentResponse(data);
    }

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
    public IResponse saveFrontendContent(RequestData rdata) {
        int contentId=rdata.getId();
        DefectData data=rdata.getSessionObject(ContentRequestKeys.KEY_CONTENT,DefectData.class);
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
        rdata.setMessage(LocalizedStrings.string("_defectClosed"), RequestKeys.MESSAGE_TYPE_SUCCESS);
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
        byte[] arrowBytes = FileBean.getInstance().getImageBytes("redarrow.png");
        BinaryFile file = data.createCroppedDefectPlan(plan,arrowBytes,data.getDisplayId(),x,y);
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
        byte[] arrowBytes = FileBean.getInstance().getImageBytes("redarrow.png");
        BinaryFile file = data.createFullDefectPlan(plan,arrowBytes,data.getDisplayId(),x,y);
        assert(file!=null);
        return new MemoryFileResponse(file);
    }

    public IResponse getPdfFile(RequestData rdata) {
        int contentId = rdata.getId();
        DefectData data= ContentCache.getContent(contentId,DefectData.class);
        assert(data!=null);
        BinaryFile file = DefectPdfBean.getInstance().getDefectPdfFile(data, rdata);
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
        int unitId=rdata.getId();
        UnitData unit=ContentCache.getContent(unitId, UnitData.class);
        if (unit == null || !unit.hasUserReadRight(user)) {
            return new StatusResponse(HttpServletResponse.SC_UNAUTHORIZED);
        }
        DefectData data = new DefectData();
        data.setCreateValues(unit, rdata);
        data.readBackendRequestData(rdata);
        Log.log(data.getJson().toJSONString());
        if (!ContentBean.getInstance().saveContent(data)) {
            return new StatusResponse(HttpServletResponse.SC_BAD_REQUEST);
        }
        data.setNew(false);
        data.setEditMode(false);
        ContentCache.setDirty();
        return new JsonResponse(getIdJson(data.getId()).toJSONString());
    }

    public IResponse uploadDefectImage(RequestData rdata) {
        Log.log("uploadDefectImage");
        UserData user = rdata.getLoginUser();
        if (user == null)
            return new StatusResponse(HttpServletResponse.SC_UNAUTHORIZED);
        int defectId = rdata.getId();
        DefectData defect=ContentCache.getContent(defectId, DefectData.class);
        assert(defect != null);
        BinaryFile file = rdata.getAttributes().getFile("file");
        assert(file!=null);
        ImageData image = new ImageData();
        image.setCreateValues(defect, rdata);
        if (!image.createFromBinaryFile(file, image.getMaxWidth(), image.getMaxHeight(), image.getMaxPreviewWidth(),image.getMaxPreviewHeight(), false)) {
            return new StatusResponse(HttpServletResponse.SC_BAD_REQUEST);
        }
        image.setChangerId(rdata.getUserId());
        if (!ImageBean.getInstance().saveFile(image,true)) {
            return new StatusResponse(HttpServletResponse.SC_BAD_REQUEST);
        }
        image.setNew(false);
        ContentCache.setDirty();
        return new JsonResponse(getIdJson(image.getId()).toJSONString());
    }

}
