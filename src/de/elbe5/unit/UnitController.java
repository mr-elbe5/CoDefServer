/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.unit;

import de.elbe5.base.BinaryFile;
import de.elbe5.base.LocalizedStrings;
import de.elbe5.base.Log;
import de.elbe5.base.Token;
import de.elbe5.content.ContentBean;
import de.elbe5.content.ContentData;
import de.elbe5.defect.DefectData;
import de.elbe5.content.ContentCache;
import de.elbe5.content.ContentController;
import de.elbe5.file.ImageBean;
import de.elbe5.file.ImageData;
import de.elbe5.request.ContentRequestKeys;
import de.elbe5.request.RequestData;
import de.elbe5.request.RequestKeys;
import de.elbe5.response.CloseDialogResponse;
import de.elbe5.response.IResponse;
import de.elbe5.response.MemoryFileResponse;
import de.elbe5.response.StatusResponse;
import de.elbe5.rights.GlobalRight;
import de.elbe5.servlet.ControllerCache;
import de.elbe5.user.CodefUserData;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

public class UnitController extends ContentController {

    public static final String KEY = "unit";

    private static UnitController instance = null;

    public static void setInstance(UnitController instance) {
        UnitController.instance = instance;
    }

    public static UnitController getInstance() {
        return instance;
    }

    public static void register(UnitController controller){
        setInstance(controller);
        ControllerCache.addController(controller.getKey(),getInstance());
    }

    @Override
    public String getKey() {
        return KEY;
    }

    @Override
    public IResponse saveBackendContent(RequestData rdata) {
        assertLoggedInSessionCall(rdata);
        assertRights(GlobalRight.hasGlobalContentEditRight(rdata.getLoginUser()));
        int contentId = rdata.getId();
        UnitData data = ContentData.getSessionContent(rdata, UnitData.class);
        if (data.isNew())
            data.readBackendCreateRequestData(rdata);
        else
            data.readBackendUpdateRequestData(rdata);
        if (!rdata.checkFormErrors()) {
            return showEditBackendContent(data);
        }
        data.setChangerId(rdata.getUserId());
        if (!ContentBean.getInstance().saveContent(data)) {
            setSaveError(rdata);
            return showEditBackendContent(data);
        }
        ImageData plan = data.readPlanFile(rdata);
        if (plan != null){
            ImageBean.getInstance().saveFile(plan, true);
        }
        data.setNew(false);
        rdata.removeSessionObject(ContentRequestKeys.KEY_CONTENT);
        ContentCache.setDirty();
        rdata.setMessage(LocalizedStrings.string("_contentSaved"), RequestKeys.MESSAGE_TYPE_SUCCESS);
        return new CloseDialogResponse("/ctrl/admin/openContentAdministration?contentId=" + data.getId());
    }

    public IResponse showDefectPlan(RequestData rdata) {
        CodefUserData user = rdata.getLoginUser(CodefUserData.class);
        if (user==null)
            return new StatusResponse(HttpServletResponse.SC_UNAUTHORIZED);
        int id = rdata.getId();
        UnitData data= (UnitData) ContentCache.getContent(id);
        if (!data.hasUserReadRight(rdata.getLoginUser())) {
            String token = rdata.getAttributes().getString("token");
            assertRights(Token.matchToken(id, token));
        }
        if (data.getPlan()==null)
            return new StatusResponse(HttpServletResponse.SC_NOT_FOUND);
        ImageData plan = ImageBean.getInstance().getFile(data.getPlan().getId(),true,ImageData.class);
        byte[] arrowBytes = UnitBean.getInstance().getImageBytes("redarrow.png");
        List<DefectData> defects = user.getUnitDefects(data.getId());
        BinaryFile file = data.createUnitDefectPlan(plan,arrowBytes,defects,1);
        assert(file!=null);
        return new MemoryFileResponse(file);
    }

    public IResponse getReport(RequestData rdata) {
        boolean includeStatusChanges = rdata.getAttributes().getBoolean("includeStatusChanges");
        int contentId = rdata.getId();
        BinaryFile file = UnitPdfBean.getInstance().getUnitReport(contentId, rdata, includeStatusChanges);
        assert(file!=null);
        MemoryFileResponse view=new MemoryFileResponse(file);
        view.setForceDownload(true);
        return view;
    }

    public IResponse sort(RequestData rdata) {
        CodefUserData user = rdata.getLoginUser(CodefUserData.class);
        if (user==null)
            return new StatusResponse(HttpServletResponse.SC_UNAUTHORIZED);
        int sortType = rdata.getAttributes().getInt("sortType");
        user.setSortType(sortType);
        return show(rdata);
    }

    // api

    public IResponse downloadUnitDefectPlan(RequestData rdata) {
        CodefUserData user = rdata.getLoginUser(CodefUserData.class);
        if (user==null)
            return new StatusResponse(HttpServletResponse.SC_UNAUTHORIZED);
        Log.info("loading unit defect plan");
        int scalePercent = rdata.getAttributes().getInt("scale", 100);
        //todo
        boolean isEditor = GlobalRight.hasGlobalContentEditRight(user);
        int id = rdata.getId();
        UnitData data= (UnitData) ContentCache.getContent(id);
        if (!data.hasUserReadRight(user)) {
            Log.error("plan is null");
            return new StatusResponse(HttpServletResponse.SC_NOT_FOUND);
        }
        if (data.getPlan()==null)
            return new StatusResponse(HttpServletResponse.SC_NOT_FOUND);
        ImageData plan = ImageBean.getInstance().getFile(data.getPlan().getId(),true,ImageData.class);
        byte[] arrowBytes = UnitBean.getInstance().getImageBytes("red_arrow.png");
        List<DefectData> defects = user.getUnitDefects(data.getId());
        BinaryFile file = data.createUnitDefectPlan(plan,arrowBytes,defects,((float)scalePercent)/100);
        if (file==null) {
            Log.error("file is null");
            return new StatusResponse(HttpServletResponse.SC_NOT_FOUND);
        }
        return new MemoryFileResponse(file);
    }

}
