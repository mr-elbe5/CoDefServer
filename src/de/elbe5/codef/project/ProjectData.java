/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.codef.project;

import de.elbe5.base.JsonObject;
import de.elbe5.base.StringHelper;
import de.elbe5.codef.ViewFilter;
import de.elbe5.codef.unit.UnitData;
import de.elbe5.content.ContentBean;
import de.elbe5.content.ContentData;
import de.elbe5.file.FileData;
import de.elbe5.request.ContentRequestKeys;
import de.elbe5.request.RequestData;

import jakarta.servlet.ServletException;
import jakarta.servlet.jsp.PageContext;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class ProjectData extends ContentData {

    public static List<Class<? extends ContentData>> childClasses = new ArrayList<>();
    public static List<Class<? extends FileData>> fileClasses = new ArrayList<>();

    static {
        childClasses.add(UnitData.class);
    }

    protected int groupId=0;

    public ProjectData() {
    }

    public ContentBean getBean() {
        return ProjectBean.getInstance();
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    @Override
    public List<Class<? extends ContentData>> getChildClasses(){
        return ProjectData.childClasses;
    }

    public List<Class<? extends FileData>> getFileClasses(){
        return ProjectData.fileClasses;
    }

    @Override
    public boolean hasUserReadRight(RequestData rdata) {
        return ViewFilter.getFilter(rdata).hasProjectReadRight(getId());
    }

    @Override
    public boolean hasUserEditRight(RequestData rdata) {
        return rdata.hasContentEditRight();
    }
    // view

    @Override
    public String getContentDataJsp() {
        return "/WEB-INF/_jsp/codef/project/editContentData.ajax.jsp";
    }

    @Override
    public void displayContent(PageContext context, RequestData rdata) throws IOException, ServletException {
        Writer writer = context.getOut();
        writer.write("<div id=\"pageContent\" class=\"viewArea\">");
        context.include("/WEB-INF/_jsp/codef/project/project.jsp");
        writer.write("</div>");
    }

    //used in jsp
    public void displayTreeContent(PageContext context, RequestData rdata) throws IOException, ServletException {
        if (hasUserReadRight(rdata)) {
            //backup
            ContentData currentContent=rdata.getCurrentDataInRequestOrSession(ContentRequestKeys.KEY_CONTENT, ContentData.class);
            rdata.setRequestObject(ContentRequestKeys.KEY_CONTENT, this);
            context.include("/WEB-INF/_jsp/codef/project/treeContent.inc.jsp", true);
            //restore
            rdata.setRequestObject(ContentRequestKeys.KEY_CONTENT, currentContent);
        }
    }

    // multiple data

    @Override
    public void readRequestData(RequestData rdata) {
        setDisplayName(rdata.getAttributes().getString("displayName").trim());
        setName(StringHelper.toSafeWebName(getDisplayName()));
        setDescription(rdata.getAttributes().getString("description"));
        setGroupId(rdata.getAttributes().getInt("groupId"));
        setActive(rdata.getAttributes().getBoolean("active"));
        setNavType(ContentData.NAV_TYPE_HEADER);
        if (getDisplayName().isEmpty()) {
            rdata.addIncompleteField("displayName");
        }
        if (getGroupId()==0) {
            rdata.addIncompleteField("groupId");
        }
    }

    @SuppressWarnings("unchecked")
    public JsonObject getJson(){
        JsonObject json = super.getJson();
        json.put("id",getId());
        json.put("name",getDisplayName());
        json.put("description",getDescription());
        json.put("phase", "DEFAULT");
        return json;
    }

}
