/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.codef.defect;

import de.elbe5.base.*;
import de.elbe5.codef.defectstatus.DefectStatusData;
import de.elbe5.content.ContentBean;
import de.elbe5.content.ContentCache;
import de.elbe5.codef.ViewFilter;
import de.elbe5.codef.unit.UnitData;
import de.elbe5.content.ContentData;
import de.elbe5.codef.project.ProjectData;
import de.elbe5.file.FileData;
import de.elbe5.file.ImageData;
import de.elbe5.request.RequestData;
import de.elbe5.rights.SystemZone;
import de.elbe5.user.UserCache;
import de.elbe5.user.UserData;

import jakarta.servlet.ServletException;
import jakarta.servlet.jsp.PageContext;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.Writer;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class DefectData extends ContentData {

    public static final String STATUS_OPEN = "OPEN";
    public static final String STATUS_DISPUTED = "DISPUTED";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_DONE = "DONE";

    public static List<Class<? extends ContentData>> childClasses = new ArrayList<>();
    public static List<Class<? extends FileData>> fileClasses = new ArrayList<>();

    static {
        childClasses.add(DefectStatusData.class);
        fileClasses.add(ImageData.class);
    }

    protected int displayId = 0;
    protected int unitId = 0;
    protected int projectId = 0;
    protected int planId = 0;
    protected int assignedId = 0;

    protected boolean notified = false;
    protected String lot = "";
    protected String status = STATUS_OPEN;
    protected int costs = 0;
    protected int positionX = 0; // Percent * 100
    protected int positionY = 0; // Percent * 100
    protected String positionComment = "";
    protected LocalDate dueDate1 = null;
    protected LocalDate dueDate2 = null;
    protected LocalDate closeDate = null;

    protected String projectName="";
    protected String unitName ="";

    // base data

    public ContentBean getBean() {
        return DefectBean.getInstance();
    }

    @Override
    public String getName(){
        return "defect-"+getDisplayId();
    }

    @Override
    public String getDisplayName(){
        return "ID "+getDisplayId();
    }

    public int getDisplayId() {
        return displayId;
    }

    public void setDisplayId(int displayId) {
        this.displayId = displayId;
    }

    public void setUnitId(int unitId) {
        this.unitId = unitId;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public int getUnitId() {
        return parentId;
    }

    public int getPlanId() {
        return planId;
    }

    public void setPlanId(int planId) {
        this.planId = planId;
    }

    public int getAssignedId() {
        return assignedId;
    }

    public void setAssignedId(int assignedId) {
        this.assignedId = assignedId;
    }

    public boolean isNotified() {
        return notified;
    }

    public void setNotified(boolean notified) {
        this.notified = notified;
    }

    public String getLot() {
        return lot;
    }

    public void setLot(String lot) {
        this.lot = lot;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isClosed(){
        return getCloseDate()!=null;
    }

    public int getCosts() {
        return costs;
    }

    public String getCostsString() {
        return costs==0 ? "" : Integer.toString(costs);
    }

    public void setCosts(int costs) {
        this.costs = costs;
    }

    public int getPositionX() {
        return positionX;
    }

    public void setPositionX(int positionX) {
        this.positionX = positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public void setPositionY(int positionY) {
        this.positionY = positionY;
    }

    public String getPositionComment() {
        return positionComment;
    }

    public void setPositionComment(String positionComment) {
        this.positionComment = positionComment;
    }

    public LocalDate getDueDate1() {
        return dueDate1;
    }

    public void setDueDate1(LocalDate dueDate1) {
        this.dueDate1 = dueDate1;
    }

    public LocalDate getDueDate2() {
        return dueDate2;
    }

    public void setDueDate2(LocalDate dueDate2) {
        this.dueDate2 = dueDate2;
    }

    public LocalDate getDueDate() {
        return dueDate2 != null ? dueDate2 : dueDate1;
    }

    public LocalDate getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(LocalDate closeDate) {
        this.closeDate = closeDate;
    }

    public List<DefectStatusData> getStatuses() {
        return new ArrayList<>(getChildren(DefectStatusData.class));
    }

    public String getProjectName() {
        if (projectName.isEmpty()){
            ProjectData data= ContentCache.getContent(projectId,ProjectData.class);
            if (data!=null)
                projectName=data.getDisplayName();
        }
        return projectName;
    }

    public String getUnitName() {
        if (unitName.isEmpty()){
            UnitData data= ContentCache.getContent(unitId, UnitData.class);
            if (data!=null)
                unitName =data.getDisplayName();
        }
        return unitName;
    }

    public String getAssignedName() {
        if (assignedId==0)
            return "";
        UserData data= UserCache.getUser(assignedId);
        if (data!=null)
            return data.getName();
        return "";
    }

    @Override
    public List<Class<? extends ContentData>> getChildClasses(){
        return DefectData.childClasses;
    }

    public List<Class<? extends FileData>> getFileClasses(){
        return DefectData.fileClasses;
    }

    @Override
    public boolean hasUserReadRight(RequestData rdata) {
        return ViewFilter.getFilter(rdata).hasProjectReadRight(getProjectId()) && (rdata.hasContentEditRight() || rdata.getUserId()==getAssignedId());
    }

    public boolean hasUserReadRight(ViewFilter filter, UserData user) {
        return filter.hasProjectReadRight(getProjectId()) && (user.hasSystemRight(SystemZone.CONTENTEDIT) || user.getId()==getAssignedId());
    }

    @Override
    public boolean hasUserEditRight(RequestData rdata) {
        return rdata.hasContentEditRight();
    }

    // view

    @Override
    public String getContentDataJsp() {
        return "/WEB-INF/_jsp/defect/editContentData.ajax.jsp";
    }

    public String getAdminContentTreeJsp() {
        return "/WEB-INF/_jsp/defect/adminTreeContent.inc.jsp";
    }

    @Override
    public void displayContent(PageContext context, RequestData rdata) throws IOException, ServletException {
        Writer writer = context.getOut();
        writer.write("<div id=\"pageContent\" class=\"viewArea\">");
        if (ContentData.VIEW_TYPE_EDIT.equals(getViewType())) {
            if (isNew())
                context.include("/WEB-INF/_jsp/defect/createDefect.jsp");
            else
                context.include("/WEB-INF/_jsp/defect/editDefect.jsp");
        } else {
            context.include("/WEB-INF/_jsp/defect/defect.jsp");
        }
        writer.write("</div>");
    }

    // multiple data

    @Override
    public void setCreateValues(ContentData parent, RequestData rdata) {
        super.setCreateValues(parent, rdata);
        if (!(this.parent instanceof UnitData unit)) {
            Log.error("parent of defect page should be unit page");
            return;
        }
        setDisplayId(DefectBean.getInstance().getNextDisplayId());
        ProjectData project = (ProjectData) unit.getParent();
        setUnitId(unit.getId());
        setProjectId(project.getId());
        setStatus(STATUS_OPEN);
        setNavType(NAV_TYPE_NONE);
        setPlanId(unit.getPlan() == null ? 0 : unit.getPlan().getId());
    }

    @Override
    public void readFrontendCreateRequestData(RequestData rdata) {
        readCommonRequestData(rdata);
        setDescription(rdata.getAttributes().getString("description").trim());
        setDueDate1(rdata.getAttributes().getDate("dueDate1"));
        setPositionX(rdata.getAttributes().getInt("positionX"));
        setPositionY(rdata.getAttributes().getInt("positionY"));
        setPositionComment(rdata.getAttributes().getString("positionComment"));
        if (getDescription().isEmpty()) {
            rdata.addIncompleteField("description");
        }
        if (getAssignedId()==0) {
            rdata.addIncompleteField("assigned");
        }
        if (getDueDate()==null) {
            rdata.addIncompleteField("dueDate1");
        }
    }

    @Override
    public void readFrontendUpdateRequestData(RequestData rdata) {
        readCommonRequestData(rdata);
        setDueDate2(rdata.getAttributes().getDate("dueDate2"));
        if (getAssignedId()==0) {
            rdata.addIncompleteField("assigned");
        }
    }

    public void readCommonRequestData(RequestData rdata) {
        setAssignedId(rdata.getAttributes().getInt("assigned"));
        setNotified(rdata.getAttributes().getBoolean("notified"));
        setLot(rdata.getAttributes().getString("lot"));
        setCosts(rdata.getAttributes().getInt("costs"));
        List<BinaryFile> newFiles = rdata.getAttributes().getFileList("files");
        for (BinaryFile f : newFiles) {
            if (f.isImage()){
                ImageData image = new ImageData();
                image.setCreateValues(this, rdata);
                if (!image.createFromBinaryFile(f, image.getMaxWidth(), image.getMaxHeight(), image.getMaxPreviewWidth(),image.getMaxPreviewHeight(), false))
                    continue;
                image.setChangerId(rdata.getUserId());
                getFiles().add(image);
            }
        }
    }

    public void readRequestData(RequestData rdata) {
        setCreatorId(rdata.getAttributes().getInt("creatorId"));
        setDescription(rdata.getAttributes().getString("description"));
        setAssignedId(rdata.getAttributes().getInt("assignedId"));
        setLot(rdata.getAttributes().getString("lot"));
        setPositionX(rdata.getAttributes().getInt("positionX"));
        setPositionY(rdata.getAttributes().getInt("positionY"));
        setPositionComment(rdata.getAttributes().getString("positionComment"));
        setStatus(rdata.getAttributes().getString("state"));
        setCreationDate(DateHelper.asLocalDateTime(rdata.getAttributes().getLong("creationDate")));
        setDueDate1(DateHelper.asLocalDate(rdata.getAttributes().getLong("dueDate")));
        setUnitId(rdata.getAttributes().getInt("unitId"));
    }

    @Override
    public JsonObject getJson(){
        return super.getJson()
                .add("displayId",getDisplayId())
                .add("assignedId",getAssignedId())
                .add("assignedName",getAssignedName())
                .add("planId",getPlanId())
                .add("positionX",getPositionX())
                .add("positionY",getPositionY())
                .add("positionComment",getPositionComment())
                .add("state", getStatus())
                .add("dueDate", getDueDate())
                .add("phase", "DEFAULT");
    }

    @Override
    public JsonObject getJsonRecursive(){
        JsonArray jsStatusChanges = new JsonArray();
        for (DefectStatusData statusChange : getStatuses()) {
            JsonObject jsStatusChange = statusChange.getJsonRecursive();
            jsStatusChanges.add(jsStatusChange);
        }
        return getJson()
                .add("statusChanges", jsStatusChanges)
                .add("images", getImagesForJson())
                .add("documents", getDocumentsForJson());
    }

    @Override
    public void fromJson(JSONObject json) {
        super.fromJson(json);
        int i = getInt(json, "displayId");
        if (i!=0)
            setDisplayId(i);
        i = getInt(json, "assignedId");
        if (i!=0)
            setAssignedId(i);
        i = getInt(json, "planId");
        if (i!=0)
            setPlanId(i);
        i = getInt(json, "positionX");
        if (i!=0)
            setPositionX(i);
        i = getInt(json, "positionY");
        if (i!=0)
            setPositionY(i);
        String s = getString(json, "positionComment");
        if (s != null)
            setPositionComment(s);
        s = getString(json, "state");
        if (s != null)
            setStatus(s);
        LocalDate date = getLocalDate(json, "dueDate");
        if (date != null)
            setDueDate1(date);
    }

    @Override
    public void fromJsonRecursive(JSONObject json) {
        fromJson(json);
        addImagesFromJson(json);
        addDocumentsFromJson(json);
        addStatusChangesFromJson(json);
    }

    public void addStatusChangesFromJson(JSONObject json) {
        JSONArray jsStatusChanges = getJSONArray(json, "statusChanges");
        if (jsStatusChanges != null){
            for (Object obj : jsStatusChanges){
                if (obj instanceof JSONObject jsObj){
                    DefectStatusData statusChange = new DefectStatusData();
                    statusChange.fromJsonRecursive(jsObj);
                    if (statusChange.hasValidData())
                        getChildren().add(statusChange);
                }
            }
        }
    }

}
