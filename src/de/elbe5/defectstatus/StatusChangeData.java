/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.defectstatus;

import de.elbe5.base.*;
import de.elbe5.content.ContentNavType;
import de.elbe5.defect.DefectData;
import de.elbe5.content.ContentBean;
import de.elbe5.content.ContentData;
import de.elbe5.defect.DefectStatus;
import de.elbe5.file.FileData;
import de.elbe5.file.ImageData;
import de.elbe5.request.RequestData;
import de.elbe5.user.UserCache;
import de.elbe5.user.UserData;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StatusChangeData extends ContentData {

    public static List<Class<? extends ContentData>> childClasses = new ArrayList<>();
    public static List<Class<? extends FileData>> fileClasses = new ArrayList<>();

    static {
        fileClasses.add(ImageData.class);
    }

    protected int assignedId = 0;
    protected DefectStatus status = DefectStatus.OPEN;

    public ContentBean getBean() {
        return StatusChangeBean.getInstance();
    }

    @Override
    public String getDisplayName(){
        if (parent instanceof DefectData defect){
            String idx = isNew() ? "" : " " + String.valueOf(parent.getChildIndex(this)+1);
            return defect.getDisplayName() + "-" + LocalizedStrings.string("_statusChange") + idx;
        }
        return LocalizedStrings.string("_statusChange");
    }

    public int getAssignedId() {
        return assignedId;
    }

    public void setAssignedId(int assignedId) {
        this.assignedId = assignedId;
    }

    public String getAssignedName() {
        if (assignedId==0)
            return "";
        UserData data= UserCache.getUser(assignedId);
        if (data!=null)
            return data.getName();
        return "";
    }

    public DefectStatus getStatus() {
        return status;
    }

    public String getStatusString() {
        return getStatus().toString();
    }

    public void setStatus(DefectStatus status) {
        this.status = status;
    }

    public void setStatus(String statusString) {
        try {
            this.status = DefectStatus.valueOf(statusString);
        }
        catch (IllegalArgumentException ignore){
            this.status = DefectStatus.OPEN;
        }
    }

    public String geTitle(){
        return LocalizedStrings.string("_statusChange")
                +" "+ LocalizedStrings.string("_by")
                +" "+ UserCache.getUser(getCreatorId()).getName()
                +" "+ LocalizedStrings.string("_ofDate")
                +" "+ DateHelper.toHtml(getCreationDate());
    }

    @Override
    public List<Class<? extends ContentData>> getChildClasses(){
        return StatusChangeData.childClasses;
    }

    public List<Class<? extends FileData>> getFileClasses(){
        return StatusChangeData.fileClasses;
    }

    @Override
    public void setCreateValues(ContentData parent, RequestData rdata) {
        Log.log("StatusChangeData.setBackendCreateValues");
        super.setCreateValues(parent, rdata);
        if (parent instanceof DefectData data) {
            setStatus(data.getStatus());
        }
    }

    public void readBackendRequestData(RequestData rdata) {
        Log.log("StatusChangeData.readBackendRequestData");
        setDescription(rdata.getAttributes().getString("description"));
        setOpenAccess(true);
        setNavType(ContentNavType.NONE);
        setActive(rdata.getAttributes().getBoolean("active"));
        setDescription(rdata.getAttributes().getString("description"));
        setAssignedId(rdata.getAttributes().getInt("assignedId"));
        setStatus(rdata.getAttributes().getString("status"));
        if (getDescription().isEmpty()) {
            rdata.addIncompleteField("description");
        }
        if (getAssignedId()==0) {
            rdata.addIncompleteField("assigned");
        }
    }

    public void readFrontendRequestData(RequestData rdata) {
        Log.log("StatusChangeData.readFrontendRequestData");
        setDescription(rdata.getAttributes().getString("description"));
        setAssignedId(rdata.getAttributes().getInt("assignedId"));
        setOpenAccess(true);
        setNavType(ContentNavType.NONE);
        setActive(true);
        setStatus(rdata.getAttributes().getString("status"));
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
        if (getDescription().isEmpty()) {
            rdata.addIncompleteField("description");
        }
    }

    public void readApiRequestData(RequestData rdata) {
        Log.log("StatusChangeData.readApiRequestData");
        setCreatorId(rdata.getAttributes().getInt("creatorId"));
        setStatus(rdata.getAttributes().getString("status"));
        setCreationDate(DateHelper.asLocalDateTime(rdata.getAttributes().getLong("creationDate")));
        setDescription(rdata.getAttributes().getString("description"));
        setAssignedId(rdata.getAttributes().getInt("assignedId"));
    }

    @Override
    public String getBackendContentTreeJsp() {
        return "/WEB-INF/_jsp/defectstatus/backendTreeContent.inc.jsp";
    }

    @Override
    public String getBackendEditJsp() {
        return "/WEB-INF/_jsp/defectstatus/editBackendContent.ajax.jsp";
    }

    @Override
    public String getFrontendEditJsp() {
        return "/WEB-INF/_jsp/defectstatus/editFrontendContent.ajax.jsp";
    }

    @Override
    public JsonObject getJson(){
        return super.getJson()
                .add("status", getStatusString());
    }

    @Override
    public JsonObject getJsonRecursive(){
        return getJson()
                .add("images", getImagesForJson())
                .add("documents", getDocumentsForJson());
    }

    @Override
    public void fromJson(JSONObject json) {
        super.fromJson(json);
        String s = getString(json, "status");
        if (s!=null)
            setStatus(s);
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
                    StatusChangeData statusData = new StatusChangeData();
                    statusData.fromJsonRecursive(jsObj);
                    if (statusData.hasValidData())
                        getChildren().add(statusData);
                }
            }
        }
    }
}
