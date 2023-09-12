/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.project;

import de.elbe5.application.Configuration;
import de.elbe5.base.JsonObject;
import de.elbe5.base.Log;
import de.elbe5.base.StringHelper;
import de.elbe5.content.ContentNavType;
import de.elbe5.unit.UnitData;
import de.elbe5.content.ContentBean;
import de.elbe5.content.ContentData;
import de.elbe5.file.FileData;
import de.elbe5.request.RequestData;

import jakarta.servlet.ServletException;
import jakarta.servlet.jsp.PageContext;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProjectData extends ContentData {

    public static List<Class<? extends ContentData>> childClasses = new ArrayList<>();
    public static List<Class<? extends FileData>> fileClasses = new ArrayList<>();

    static {
        childClasses.add(UnitData.class);
    }

    protected Set<Integer> companyIds = new HashSet<>();

    public ProjectData() {
    }

    public ContentBean getBean() {
        return ProjectBean.getInstance();
    }

    public Set<Integer> getCompanyIds() {
        return companyIds;
    }

    public void setCompanyIds(Set<Integer> companyIds) {
        this.companyIds = companyIds;
    }

    @Override
    public List<Class<? extends ContentData>> getChildClasses(){
        return ProjectData.childClasses;
    }

    public List<Class<? extends FileData>> getFileClasses(){
        return ProjectData.fileClasses;
    }

    @Override
    public String getBackendContentTreeJsp() {
        return "/WEB-INF/_jsp/project/backendTreeContent.inc.jsp";
    }

    @Override
    public String getBackendEditJsp() {
        return "/WEB-INF/_jsp/project/editBackendContent.ajax.jsp";
    }

    @Override
    public String getFrontendContentTreeJsp() {
        return "/WEB-INF/_jsp/project/frontendTreeContent.inc.jsp";
    }

    @Override
    public void displayContent(PageContext context, RequestData rdata) throws IOException, ServletException {
        Writer writer = context.getOut();
        writer.write("<div id=\"pageContent\" class=\"viewArea\">");
        context.include("/WEB-INF/_jsp/project/project.jsp");
        writer.write("</div>");
    }

    // multiple data

    @Override
    public void readBackendRequestData(RequestData rdata) {
        Log.log("ProjectData.readBackendRequestData");
        setDisplayName(rdata.getAttributes().getString("displayName").trim());
        setName(StringHelper.toSafeWebName(getDisplayName()));
        setDescription(rdata.getAttributes().getString("description"));
        setOpenAccess(rdata.getAttributes().getBoolean("openAccess"));
        setReaderGroupId(rdata.getAttributes().getInt("readerGroupId"));
        setEditorGroupId(rdata.getAttributes().getInt("editorGroupId"));
        setCompanyIds(rdata.getAttributes().getIntegerSet("companyIds"));
        setActive(rdata.getAttributes().getBoolean("active"));
        setNavType(ContentNavType.HEADER);
        if (getDisplayName().isEmpty()) {
            rdata.addIncompleteField("displayName");
        }
        if (Configuration.useEditorGroup() && getEditorGroupId() == 0){
            rdata.addIncompleteField("editorGroupId");
        }
        if (getCompanyIds().isEmpty()){
            rdata.addIncompleteField("companyIds");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public JsonObject getJson(){
        JSONArray jsCompanyIds = new JSONArray();
        jsCompanyIds.addAll(getCompanyIds());
        return super.getJson()
                .add("companyIds", jsCompanyIds);
    }

    @Override
    @SuppressWarnings("unchecked")
    public JsonObject getJsonRecursive(){
        JSONArray jsUnits = new JSONArray();
        for (UnitData unit : getChildren(UnitData.class)) {
            if (!unit.isActive())
                continue;
            jsUnits.add(unit.getJsonRecursive());
        }
        return getJson()
                .add("units", jsUnits);
    }

    @Override
    public void fromJson(JSONObject json) {
        super.fromJson(json);
    }

    @Override
    public void fromJsonRecursive(JSONObject json) {
        fromJson(json);
        addUnitsFromJson(json);
    }

    public void addUnitsFromJson(JSONObject json) {
        JSONArray jsUnits = getJSONArray(json, "units");
        if (jsUnits != null){
            for (Object obj : jsUnits){
                if (obj instanceof JSONObject jsObj){
                    UnitData unit = new UnitData();
                    unit.fromJsonRecursive(jsObj);
                    if (unit.hasValidData())
                        getChildren().add(unit);
                }
            }
        }
    }

}
