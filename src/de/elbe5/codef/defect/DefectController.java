/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.codef.defect;

import de.elbe5.base.BinaryFile;
import de.elbe5.base.LocalizedStrings;
import de.elbe5.base.Token;
import de.elbe5.codef.unit.PlanImageData;
import de.elbe5.codef.unit.UnitData;
import de.elbe5.content.*;
import de.elbe5.file.FileBean;
import de.elbe5.file.ImageBean;
import de.elbe5.file.ImageData;
import de.elbe5.request.ContentRequestKeys;
import de.elbe5.request.RequestData;
import de.elbe5.request.RequestKeys;
import de.elbe5.response.*;
import de.elbe5.rights.Right;
import de.elbe5.rights.SystemZone;
import de.elbe5.servlet.ControllerCache;
import de.elbe5.user.UserData;
import jakarta.servlet.http.HttpServletResponse;

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

    public IResponse openCreateContentFrontend(RequestData rdata) {
        int parentId=rdata.getAttributes().getInt("parentId");
        UnitData parent= ContentCache.getContent(parentId, UnitData.class);
        checkRights(parent != null && parent.hasUserEditRight(rdata));
        DefectData data = new DefectData();
        data.setCreateValues(parent, rdata);
        data.setViewType(ContentData.VIEW_TYPE_EDIT);
        rdata.setSessionObject(ContentRequestKeys.KEY_CONTENT,data);
        return new ContentResponse(data);
    }

    public IResponse openEditContentFrontend(RequestData rdata) {
        int defectId=rdata.getId();
        DefectData data = ContentBean.getInstance().getContent(defectId,DefectData.class);
        checkRights(data.hasUserEditRight(rdata));
        rdata.setSessionObject(ContentRequestKeys.KEY_CONTENT,data);
        data.setViewType(ContentData.VIEW_TYPE_EDIT);
        return new ContentResponse(data);
    }

    //frontend
    public IResponse saveContentFrontend(RequestData rdata) {
        int contentId=rdata.getId();
        DefectData data=rdata.getSessionObject(ContentRequestKeys.KEY_CONTENT,DefectData.class);
        assert(data != null && data.getId() == contentId);
        checkRights(data.hasUserEditRight(rdata));
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
        data.setViewType(ContentData.VIEW_TYPE_SHOW);
        ContentCache.setDirty();
        rdata.setMessage(LocalizedStrings.string("_contentSaved"), RequestKeys.MESSAGE_TYPE_SUCCESS);
        return show(rdata);
    }

    public IResponse closeDefect(RequestData rdata) {
        int contentId=rdata.getId();
        DefectData data = ContentBean.getInstance().getContent(contentId,DefectData.class);
        checkRights(data.hasUserEditRight(rdata));
        data.setCloseDate(DefectBean.getInstance().getServerTime().toLocalDate());
        data.setChangerId(rdata.getUserId());
        if (!DefectBean.getInstance().closeDefect(data)) {
            setSaveError(rdata);
            return new ContentResponse(data);
        }
        data.setViewType(ContentData.VIEW_TYPE_SHOW);
        ContentCache.setDirty();
        rdata.setMessage(LocalizedStrings.string("_defectClosed"), RequestKeys.MESSAGE_TYPE_SUCCESS);
        UnitData unit = ContentCache.getContent(data.getUnitId(), UnitData.class);
        return new ContentResponse(unit);
    }

    public IResponse showCroppedDefectPlan(RequestData rdata) {
        int id = rdata.getId();
        DefectData data=ContentCache.getContent(id,DefectData.class);
        assert(data!=null);
        if (!data.hasUserReadRight(rdata)) {
            String token = rdata.getAttributes().getString("token");
            checkRights(Token.matchToken(id, token));
        }
        int x=rdata.getAttributes().getInt("x",data.getPositionX());
        int y=rdata.getAttributes().getInt("y",data.getPositionY());
        PlanImageData plan = FileBean.getInstance().getFile(data.getPlanId(),true,PlanImageData.class);
        byte[] arrowBytes = FileBean.getInstance().getImageBytes("redarrow.png");
        BinaryFile file = plan.createCroppedDefectPlan(arrowBytes,data.getDisplayId(),x,y);
        assert(file!=null);
        return new MemoryFileResponse(file);
    }

    public IResponse openFullDefectPlan(RequestData rdata) {
        return new ForwardResponse("/WEB-INF/_jsp/codef/defect/defectPlan.ajax.jsp");
    }

    public IResponse showFullDefectPlan(RequestData rdata) {
        int id = rdata.getId();
        DefectData data=ContentCache.getContent(id,DefectData.class);
        assert(data!=null);
        if (!data.hasUserReadRight(rdata)) {
            String token = rdata.getAttributes().getString("token");
            checkRights(Token.matchToken(id, token));
        }
        int x=rdata.getAttributes().getInt("x",data.getPositionX());
        int y=rdata.getAttributes().getInt("y",data.getPositionY());
        PlanImageData plan = FileBean.getInstance().getFile(data.getPlanId(),true,PlanImageData.class);
        byte[] arrowBytes = FileBean.getInstance().getImageBytes("redarrow.png");
        BinaryFile file = plan.createFullDefectPlan(arrowBytes,data.getDisplayId(),x,y);
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

    public IResponse uploadNewDefect(RequestData rdata) {
        //Log.log("uploadNewDefect");
        UserData user = rdata.getLoginUser();
        if (user==null)
            return new StatusResponse(HttpServletResponse.SC_UNAUTHORIZED);
        int unitId=rdata.getId();
        UnitData unit=ContentCache.getContent(unitId, UnitData.class);
        if (unit == null || !user.hasSystemRight(SystemZone.CONTENTREAD) && !unit.hasUserRight(user, Right.READ)) {
            return new StatusResponse(HttpServletResponse.SC_UNAUTHORIZED);
        }
        DefectData data = new DefectData();
        data.setCreateValues(unit, rdata);
        data.readRequestData(rdata);
        //Log.log(data.getJson().toJSONString());
        if (!ContentBean.getInstance().saveContent(data)) {
            return new StatusResponse(HttpServletResponse.SC_BAD_REQUEST);
        }
        data.setNew(false);
        data.setViewType(ContentData.VIEW_TYPE_SHOW);
        ContentCache.setDirty();
        return new JsonResponse(getIdJson(data.getId()).toJSONString());
    }

    public IResponse uploadNewDefectImage(RequestData rdata) {
        //Log.log("uploadNewDefectImage");
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
