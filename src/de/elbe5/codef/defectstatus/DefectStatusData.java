/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.codef.defectstatus;

import de.elbe5.base.DateHelper;
import de.elbe5.base.JsonObject;
import de.elbe5.base.LocalizedStrings;
import de.elbe5.codef.defect.DefectData;
import de.elbe5.content.ContentBean;
import de.elbe5.content.ContentData;
import de.elbe5.file.FileData;
import de.elbe5.file.ImageData;
import de.elbe5.request.RequestData;
import de.elbe5.user.UserCache;

import java.util.ArrayList;
import java.util.List;

public class DefectStatusData extends ContentData {

    public static List<Class<? extends ContentData>> childClasses = new ArrayList<>();
    public static List<Class<? extends FileData>> fileClasses = new ArrayList<>();

    static {
        fileClasses.add(ImageData.class);
    }

    protected String comment = "";
    protected String state="";

    public ContentBean getBean() {
        return DefectStatusBean.getInstance();
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String geTitle(){
        return LocalizedStrings.string("_comment")
                +" "+ LocalizedStrings.string("_by")
                +" "+ UserCache.getUser(getCreatorId()).getName()
                +" "+ LocalizedStrings.string("_ofDate")
                +" "+ DateHelper.toHtmlDateTime(getCreationDate());
    }

    @Override
    public List<Class<? extends ContentData>> getChildClasses(){
        return DefectStatusData.childClasses;
    }

    public List<Class<? extends FileData>> getFileClasses(){
        return DefectStatusData.fileClasses;
    }

    @Override
    public void setCreateValues(ContentData parent, RequestData rdata) {
        super.setCreateValues(parent, rdata);
        if (parent instanceof DefectData data) {
            setState(data.getState());
        }
    }

    public void readRequestData(RequestData rdata) {
        setComment(rdata.getAttributes().getString("comment"));
        setState(rdata.getAttributes().getString("state"));
        if (getComment().isEmpty()) {
            rdata.addIncompleteField("comment");
        }
    }

    public void readApiRequestData(RequestData rdata) {
        setCreatorId(rdata.getAttributes().getInt("creatorId"));
        setState(rdata.getAttributes().getString("state"));
        setCreationDate(DateHelper.asLocalDateTime(rdata.getAttributes().getLong("creationDate")));
        setComment(rdata.getAttributes().getString("comment"));
    }

    public String getAdminContentTreeJsp() {
        return "/WEB-INF/_jsp/codef/defectstatus/adminTreeContent.inc.jsp";
    }

    @SuppressWarnings("unchecked")
    public JsonObject getJson(){
        JsonObject json = super.getJson();
        json.put("id",getId());
        json.put("creationDate", DateHelper.asMillis(getCreationDate()));
        json.put("creatorId", getCreatorId());
        json.put("creatorName", getCreatorName());
        json.put("comment",getComment());
        json.put("state",getState());
        return json;
    }
}
