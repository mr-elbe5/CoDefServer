/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.unit;

import de.elbe5.base.BinaryFile;
import de.elbe5.base.Log;
import de.elbe5.base.Token;
import de.elbe5.application.ViewFilter;
import de.elbe5.defect.DefectData;
import de.elbe5.content.ContentCache;
import de.elbe5.content.ContentController;
import de.elbe5.file.ImageBean;
import de.elbe5.file.ImageData;
import de.elbe5.request.RequestData;
import de.elbe5.response.IResponse;
import de.elbe5.response.MemoryFileResponse;
import de.elbe5.response.StatusResponse;
import de.elbe5.servlet.ControllerCache;
import de.elbe5.user.UserData;
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

    public IResponse showDefectPlan(RequestData rdata) {
        int id = rdata.getId();
        UnitData data= (UnitData) ContentCache.getContent(id);
        if (!data.hasUserReadRight(rdata.getLoginUser())) {
            String token = rdata.getAttributes().getString("token");
            checkRights(Token.matchToken(id, token));
        }
        if (data.getPlan()==null)
            return new StatusResponse(HttpServletResponse.SC_NOT_FOUND);
        ImageData plan = ImageBean.getInstance().getFile(data.getPlan().getId(),true,ImageData.class);
        byte[] arrowBytes = UnitBean.getInstance().getImageBytes("redarrow.png");
        List<DefectData> defects = ViewFilter.getFilter(rdata).getUnitDefects(data.getId());
        BinaryFile file = data.createUnitDefectPlan(plan,arrowBytes,defects,1);
        assert(file!=null);
        return new MemoryFileResponse(file);
    }

    public IResponse getReport(RequestData rdata) {
        boolean includeComments = rdata.getAttributes().getBoolean("includeComments");
        int contentId = rdata.getId();
        BinaryFile file = UnitPdfBean.getInstance().getUnitReport(contentId, rdata, includeComments);
        assert(file!=null);
        MemoryFileResponse view=new MemoryFileResponse(file);
        view.setForceDownload(true);
        return view;
    }

    public IResponse sort(RequestData rdata) {
        int sortType = rdata.getAttributes().getInt("sortType");
        ViewFilter filter = ViewFilter.getFilter(rdata);
        filter.setSortType(sortType);
        return show(rdata);
    }

    // api

    public IResponse downloadUnitDefectPlan(RequestData rdata) {
        UserData user = rdata.getLoginUser();
        if (user==null)
            return new StatusResponse(HttpServletResponse.SC_UNAUTHORIZED);
        Log.info("loading unit defect plan");
        int scalePercent = rdata.getAttributes().getInt("scale", 100);
        boolean isEditor = user.hasGlobalContentEditRight();
        int id = rdata.getId();
        UnitData data= (UnitData) ContentCache.getContent(id);
        ViewFilter filter = new ViewFilter();
        filter.setEditor(isEditor);
        filter.setCurrentUserId(user.getId());
        if (!data.hasUserReadRight(rdata.getLoginUser())) {
            Log.error("plan is null");
            return new StatusResponse(HttpServletResponse.SC_NOT_FOUND);
        }
        if (data.getPlan()==null)
            return new StatusResponse(HttpServletResponse.SC_NOT_FOUND);
        ImageData plan = ImageBean.getInstance().getFile(data.getPlan().getId(),true,ImageData.class);
        byte[] arrowBytes = UnitBean.getInstance().getImageBytes("red_arrow.png");
        List<DefectData> defects = filter.getUnitDefects(data.getId());
        BinaryFile file = data.createUnitDefectPlan(plan,arrowBytes,defects,((float)scalePercent)/100);
        if (file==null) {
            Log.error("file is null");
            return new StatusResponse(HttpServletResponse.SC_NOT_FOUND);
        }
        return new MemoryFileResponse(file);
    }

}
