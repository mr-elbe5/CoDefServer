/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.codef.unit;

import de.elbe5.base.*;
import de.elbe5.codef.ViewFilter;
import de.elbe5.codef.defect.DefectData;
import de.elbe5.codef.project.ProjectData;
import de.elbe5.content.ContentData;
import de.elbe5.file.FileData;
import de.elbe5.request.ContentRequestKeys;
import de.elbe5.request.RequestData;

import jakarta.servlet.ServletException;
import jakarta.servlet.jsp.PageContext;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class UnitData extends ContentData {

    public static List<Class<? extends ContentData>> childClasses = new ArrayList<>();
    public static List<Class<? extends FileData>> fileClasses = new ArrayList<>();

    static {
        childClasses.add(DefectData.class);
        fileClasses.add(PlanImageData.class);
    }

    protected int projectId=0;

    protected LocalDate approveDate = null;

    PlanImageData plan = null;

    public UnitData() {
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public LocalDate getApproveDate() {
        return approveDate;
    }

    public LocalDateTime getApproveDateTime() {
        return LocalDateTime.of(approveDate, LocalTime.MIDNIGHT);
    }

    public void setApproveDate(LocalDate approveDate) {
        this.approveDate = approveDate;
    }

    public void setApproveDateTime(LocalDateTime approveDate) {
        this.approveDate = approveDate.toLocalDate();
    }

    // set plans

    public void initializeChildren() {
        super.initializeChildren();
        plan=null;
        List<PlanImageData> candidates = getFiles(PlanImageData.class);
        if (candidates.size()==1)
            plan=candidates.get(0);
    }

    public PlanImageData getPlan() {
        return plan;
    }

    @Override
    public String getContentDataJsp() {
        return "/WEB-INF/_jsp/defecttracker/location/editContentData.ajax.jsp";
    }

    @Override
    public void displayContent(PageContext context, RequestData rdata) throws IOException, ServletException {
        Writer writer = context.getOut();
        writer.write("<div id=\"pageContent\" class=\"viewArea\">");
        context.include("/WEB-INF/_jsp/defecttracker/location/location.jsp");
        writer.write("</div>");
    }

    //used in jsp
    public void displayTreeContent(PageContext context, RequestData rdata) throws IOException, ServletException {
        if (hasUserReadRight(rdata)) {
            //backup
            ContentData currentContent=rdata.getCurrentDataInRequestOrSession(ContentRequestKeys.KEY_CONTENT, ContentData.class);
            rdata.setRequestObject(ContentRequestKeys.KEY_CONTENT, this);
            context.include("/WEB-INF/_jsp/defecttracker/location/treeContent.inc.jsp", true);
            //restore
            rdata.setRequestObject(ContentRequestKeys.KEY_CONTENT, currentContent);
        }
    }

    @Override
    public List<Class<? extends ContentData>> getChildClasses(){
        return UnitData.childClasses;
    }

    public List<Class<? extends FileData>> getFileClasses(){
        return UnitData.fileClasses;
    }

    @Override
    public boolean hasUserReadRight(RequestData rdata) {
        return ViewFilter.getFilter(rdata).hasProjectReadRight(getProjectId());
    }

    public boolean hasUserReadRight(ViewFilter filter) {
        return filter.hasProjectReadRight(getProjectId());
    }

    @Override
    public boolean hasUserEditRight(RequestData rdata) {
        return rdata.hasContentEditRight();
    }

    // multiple data

    @Override
    public void setCreateValues(ContentData parent, RequestData rdata) {
        super.setCreateValues(parent,rdata);
        if (!(parent instanceof ProjectData)){
            Log.error("parent of location part page should be project page");
            return;
        }
        setProjectId(parent.getId());
    }

    @Override
    public void readCreateRequestData(RequestData rdata) {
        setDisplayName(rdata.getAttributes().getString("displayName").trim());
        setName(StringHelper.toSafeWebName(getDisplayName()));
        setDescription(rdata.getAttributes().getString("description"));
        setApproveDate(rdata.getAttributes().getDate("approveDate"));
        setNavType(ContentData.NAV_TYPE_HEADER);
        setActive(rdata.getAttributes().getBoolean("active"));
        BinaryFile file = rdata.getAttributes().getFile("file");
        if (file != null){
            plan = new PlanImageData();
            plan.setCreateValues(this,rdata);
            plan.createFromBinaryFile(file, PlanImageData.STD_SIZE, PlanImageData.STD_SIZE, plan.getMaxPreviewWidth(), plan.getMaxPreviewHeight(), false);
            plan.setDisplayName(LocalizedStrings.string("_plan"));
        }
        if (getDisplayName().isEmpty()) {
            rdata.addIncompleteField("displayName");
        }
    }

    @Override
    public void readUpdateRequestData(RequestData rdata) {
        setDisplayName(rdata.getAttributes().getString("displayName").trim());
        setName(StringHelper.toSafeWebName(getDisplayName()));
        setDescription(rdata.getAttributes().getString("description"));
        setApproveDate(rdata.getAttributes().getDate("approveDate"));
        setNavType(ContentData.NAV_TYPE_HEADER);
        BinaryFile file = rdata.getAttributes().getFile("file");
        if (file != null && plan == null){
            plan = new PlanImageData();
            plan.setCreateValues(this,rdata);
            plan.createFromBinaryFile(file, PlanImageData.STD_SIZE, PlanImageData.STD_SIZE, plan.getMaxPreviewWidth(), plan.getMaxPreviewHeight(), false);
            plan.setDisplayName(LocalizedStrings.string("_plan"));
        }
        setActive(rdata.getAttributes().getBoolean("active"));
        if (getDisplayName().isEmpty()) {
            rdata.addIncompleteField("displayName");
        }
    }

    @SuppressWarnings("unchecked")
    public JsonObject getJson(){
        JsonObject json = super.getJson();
        json.put("id",getId());
        json.put("name",getDisplayName());
        json.put("description",getDescription());
        json.put("approveDate", DateHelper.asMillis(getApproveDate()));
        return json;
    }

}
