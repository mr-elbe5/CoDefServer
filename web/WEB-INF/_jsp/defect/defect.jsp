<%--
  Elbe 5 CMS - A Java based modular Content Management System
  Copyright (C) 2009-2020 Michael Roennau

  This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
--%>
<%response.setContentType("text/html;charset=UTF-8");%>
<%@ page trimDirectiveWhitespaces="true" %>
<%@include file="/WEB-INF/_jsp/_include/_functions.inc.jsp" %>
<%@ page import="de.elbe5.request.RequestData" %>
<%@ page import="de.elbe5.user.UserData" %>
<%@ page import="de.elbe5.user.UserCache" %>
<%@ page import="de.elbe5.file.FileData" %>
<%@ page import="de.elbe5.rights.SystemZone" %>
<%@ page import="de.elbe5.defect.DefectData" %>
<%@ page import="de.elbe5.defectstatus.StatusChangeData" %>
<%@ page import="de.elbe5.content.ContentData" %>
<%@ page import="de.elbe5.defectstatus.StatusChangeData" %>
<%@ taglib uri="/WEB-INF/formtags.tld" prefix="form" %>
<%
    RequestData rdata = RequestData.getRequestData(request);

    DefectData defect = ContentData.getCurrentContent(rdata, DefectData.class);
    assert(defect !=null);
    UserData assignedUser = UserCache.getUser(defect.getAssignedId());
    String assignedName = assignedUser == null ? "" : assignedUser.getName();
    boolean isEditor=defect.hasUserEditRight(rdata);
    boolean isAssigned=rdata.getUserId()==defect.getAssignedId();
    if (isEditor || isAssigned){
%>
<form:message/>
<section class="contentSection" id="content">
    <div class="paragraph">
        <h3><%=$H(defect.getDescription())%></h3>
        <div class="d-flex flex-wrap align-items-stretch boxContainer">
            <div class="box">
                <div class="boxTitle"><%=$SH("_id")%></div>
                <div class="boxText"><%=$I(defect.getDisplayId())%></div>
            </div>
            <div class="box">
                <div class="boxTitle"><%=$SH("_creator")%></div>
                <div class="boxText"><%=$H(UserCache.getUser(defect.getCreatorId()).getName())%></div>
            </div>
            <div class="box">
                <div class="boxTitle"><%=$SH("_creationDate")%></div>
                <div class="boxText"><%=DateHelper.toHtmlDateTime(defect.getCreationDate())%></div>
            </div>
            <div class="box">
                <div class="boxTitle"><%=$SH("_editedBy")%></div>
                <div class="boxText"><%=$H(defect.getChangerName())%></div>
            </div>
            <div class="box">
                <div class="boxTitle"><%=$SH("_changeDate")%></div>
                <div class="boxText"><%=DateHelper.toHtmlDateTime(defect.getChangeDate())%></div>
            </div>
            <div class="box">
                <div class="boxTitle"><%=$SH("_assigned")%></div>
                <div class="boxText"><%=$H(assignedName)%></div>
            </div>
            <div class="box">
                <div class="boxTitle"><%=$SH("_notified")%></div>
                <div class="boxText"><%=$SH(defect.isNotified() ? "_yes" : "_no")%></div>
            </div>
            <div class="box">
                <div class="boxTitle"><%=$SH("_lot")%></div>
                <div class="boxText"><%=$H(defect.getLot())%></div>
            </div>
            <div class="box">
                <div class="boxTitle"><%=$SH("_status")%></div>
                <div class="boxText"><%=$SH(defect.getStatus())%></div>
            </div>
            <div class="box">
                <div class="boxTitle"><%=$SH("_dueDate1")%></div>
                <div class="boxText"><%=DateHelper.toHtmlDate(defect.getDueDate1())%></div>
            </div>
            <div class="box">
                <div class="boxTitle"><%=$SH("_dueDate2")%></div>
                <div class="boxText"><%=DateHelper.toHtmlDate(defect.getDueDate2())%></div>
            </div>
            <div class="box">
                <div class="boxTitle"><%=$SH("_closeDate")%></div>
                <div class="boxText"><%=DateHelper.toHtmlDate(defect.getCloseDate())%></div>
            </div>
        </div>
        <div class="d-flex flex-wrap align-items-stretch boxContainer">
            <div class="box">
                <div class="boxTitle"><%=$SH("_position")%></div>
                <% if (defect.getPlanId()!=0){%>
                <div class="boxImage"><a href="#" onclick="return openModalDialog('/ctrl/defect/openFullDefectPlan/<%=defect.getId()%>');"><img src="/ctrl/defect/showCroppedDefectPlan/<%=defect.getId()%>" alt="" /></a></div>
                <%}%>
            </div>
            <div class="box">
                <div class="boxTitle"><%=$SH("_positionComment")%></div>
                <div class="boxText"><%=StringHelper.toHtmlMultiline(defect.getPositionComment())%></div>
            </div>
        </div>

        <% if (!defect.getFiles().isEmpty()){%>
        <div class="d-flex flex-wrap align-items-stretch boxContainer">
            <% for (FileData file : defect.getFiles()){%>
            <div class="box">
                <div class="boxTitle"><%=StringHelper.toHtml(file.getDisplayName())%></div>
                <div class="boxImage">
                    <% if (file.isImage()){%>
                    <a href="/ctrl/image/show/<%=file.getId()%>" target="_blank" title="<%=$SH("_view")%>"><img src="/ctrl/image/showPreview/<%=file.getId()%>" alt="" /></a>
                    <%} else{%>
                    <a href="/ctrl/document/show/<%=file.getId()%>" target="_blank" title="<%=$SH("_view")%>"><img src="/static-content/img/document.png" alt="" /></a>
                    <%}%></div>
                <div class="boxSubtitle"><%=StringHelper.toHtmlMultiline(file.getDescription())%></div>
            </div>
            <%}%>
        </div>
        <%}%>
    </div>
    <% for (StatusChangeData statusChange : defect.getStatusChanges()){%>
    <div class="paragraph">
        <div class="boxContainer">
            <div class="box">
                <div class="boxTitle"><%=$SH("_statusChange")%>&nbsp;<%=$SH("_by")%>&nbsp;<%=$H(UserCache.getUser(statusChange.getCreatorId()).getName())%>&nbsp;
                    <%=$SH("_ofDate")%>&nbsp;<%=DateHelper.toHtmlDateTime(statusChange.getCreationDate())%> - <%=$SH("_status")%>:<%=$SH(statusChange.getStatus())%>
                </div>
                <div class="boxText"><%=StringHelper.toHtmlMultiline(statusChange.getDescription())%></div>
            </div>
        </div>
        <%
        if (!statusChange.getFiles().isEmpty()){%>
        <div class="d-flex flex-wrap align-items-stretch boxContainer">
            <% for (FileData file : statusChange.getFiles()){
            %>
            <div class="box">
                <div class="boxTitle"><%=StringHelper.toHtml(file.getDisplayName())%></div>
                <div class="boxImage">
                    <% if (file.isImage()){%>
                    <a href="/ctrl/image/show/<%=file.getId()%>" target="_blank" title="<%=$SH("_view")%>"><img src="/ctrl/image/showPreview/<%=file.getId()%>" alt="" /></a>
                    <%} else{%>
                    <a href="/ctrl/document/show/<%=file.getId()%>" target="_blank" title="<%=$SH("_view")%>"><img src="/static-content/img/document.png" alt="" /></a>
                    <%}%></div>
                <div class="boxSubtitle"><%=StringHelper.toHtmlMultiline(file.getDescription())%></div>
            </div>
            <%}%>
        </div>
        <%}%>
    </div>
    <%
    }
    if (!defect.isClosed()){
        if (rdata.hasSystemRight(SystemZone.CONTENTADMINISTRATION)) {%>
    <div class=buttonLine>
        <button type="button" class="btn btn-outline-secondary" onclick="return openModalDialog('/ctrl/defect/openEditContentFrontend/<%=defect.getId()%>',null);"><%=$SH("_edit")%>
        </button>
        <button type="button" class="btn btn-outline-secondary" onclick="return openModalDialog('/ctrl/defect/closeDefect/<%=defect.getId()%>',null);"><%=$SH("_closeDefect")%>
        </button>
    </div>
        <%
        }%>
    <div class=buttonLine>
        <button type="button" class="btn btn-outline-secondary" onclick="return openModalDialog('/ctrl/defect/openCreateDefectComment/<%=defect.getId()%>',null);"><%=$SH("_comment")%>
        </button>
    </div>
    <%}%>
    <div class=buttonLine>
        <button type="button" class="btn btn-outline-secondary" onclick="return linkTo('/ctrl/defect/getPdfFile/<%=defect.getId()%>');"><%=$SH("_downloadPdf")%>
        </button>
    </div>
</section>
<%}%>
