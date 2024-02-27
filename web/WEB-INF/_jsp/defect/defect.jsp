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
<%@ page import="de.elbe5.file.FileData" %>
<%@ page import="de.elbe5.defect.DefectData" %>
<%@ page import="de.elbe5.defectstatus.DefectStatusData" %>
<%@ page import="de.elbe5.content.ContentData" %>
<%@ page import="de.elbe5.configuration.CodefConfiguration" %>
<%@ page import="de.elbe5.file.ImageData" %>
<%@ page import="de.elbe5.base.LocalizedSystemStrings" %>
<%@ taglib uri="/WEB-INF/formtags.tld" prefix="form" %>
<%
    RequestData rdata = RequestData.getRequestData(request);

    DefectData contentData = ContentData.getCurrentContent(rdata, DefectData.class);
    assert(contentData !=null);
    if (contentData.hasUserReadRight(rdata.getLoginUser())){
%>
<form:message/>
<section class="contentSection" id="content">
    <div class="paragraph">
        <h3><%=$H(contentData.getDescription())%></h3>
        <%if (contentData.isRemainingWork()){%>
        <div><%=$SH("_remainingWork")%></div>
        <%}%>
        <div class="d-flex flex-wrap align-items-stretch boxContainer">
            <div class="box">
                <div class="boxTitle"><%=$SH("_id")%></div>
                <div class="boxText"><%=$I(contentData.getId())%></div>
            </div>
            <div class="box">
                <div class="boxTitle"><%=$SH("_creator")%></div>
                <div class="boxText"><%=$H(contentData.getCreatorName())%></div>
            </div>
            <div class="box">
                <div class="boxTitle"><%=$SH("_creationDate")%></div>
                <div class="boxText"><%=$H(contentData.getCreationDate())%></div>
            </div>
            <div class="box">
                <div class="boxTitle"><%=$SH("_projectPhase")%></div>
                <div class="boxText"><%=LocalizedSystemStrings.getInstance().html(contentData.getProjectPhaseString())%></div>
            </div>
            <div class="box">
                <div class="boxTitle"><%=$SH("_editedBy")%></div>
                <div class="boxText"><%=$H(contentData.getChangerName())%></div>
            </div>
            <div class="box">
                <div class="boxTitle"><%=$SH("_changeDate")%></div>
                <div class="boxText"><%=$H(contentData.getChangeDate())%></div>
            </div>
            <div class="box">
                <div class="boxTitle"><%=$SH("_assigned")%></div>
                <div class="boxText"><%=$H(contentData.getLastAssignedName())%></div>
            </div>
            <% if (CodefConfiguration.showNotified()){%>
            <div class="box">
                <div class="boxTitle"><%=$SH("_notified")%></div>
                <div class="boxText"><%=$SH(contentData.isNotified() ? "_yes" : "_no")%></div>
            </div>
            <%}%>
            <div class="box">
                <div class="boxTitle"><%=$SH("_status")%></div>
                <div class="boxText"><%=LocalizedSystemStrings.getInstance().html(contentData.getLastStatus().toString())%></div>
            </div>
            <div class="box">
                <div class="boxTitle"><%=$SH("_dueDate1")%></div>
                <div class="boxText"><%=DateHelper.toHtmlDate(contentData.getDueDate1())%></div>
            </div>
            <div class="box">
                <div class="boxTitle"><%=$SH("_dueDate2")%></div>
                <div class="boxText"><%=DateHelper.toHtmlDate(contentData.getDueDate2())%></div>
            </div>
            <div class="box">
                <div class="boxTitle"><%=$SH("_closeDate")%></div>
                <div class="boxText"><%=DateHelper.toHtmlDate(contentData.getCloseDate())%></div>
            </div>
        </div>
        <div class="d-flex flex-wrap align-items-stretch boxContainer">
            <div class="box">
                <div class="boxTitle"><%=$SH("_defectPosition")%></div>
                <% if (contentData.getPlanId()!=0 && contentData.hasValidPosition()){%>
                <div class="boxImage"><a href="#" onclick="return openModalDialog('/ctrl/defect/openFullDefectPlan/<%=contentData.getId()%>');"><img src="/ctrl/defect/showCroppedDefectPlan/<%=contentData.getId()%>" alt="" /></a></div>
                <%}%>
            </div>
            <div class="box">
                <div class="boxTitle"><%=$SH("_positionComment")%></div>
                <div class="boxText"><%=StringHelper.toHtmlMultiline(contentData.getPositionComment())%></div>
            </div>
        </div>

        <% if (!contentData.getFiles().isEmpty()){%>
        <div class="d-flex flex-wrap align-items-stretch boxContainer">
            <div class="box">
                <div class="boxTitle"><%=$SH("_images")%></div>
                <div class="boxImage">
            <% for (FileData file : contentData.getFiles(ImageData.class)){%>
                <a href="<%=file.getStaticURL()%>" target="_blank" title="<%=$SH("_view")%>"><img src="/ctrl/image/showPreview/<%=file.getId()%>" alt="" /></a>
                <%}%>
                </div>
            </div>
        </div>
        <%}%>
    </div>
    <% for (DefectStatusData statusData : contentData.getStatusChanges()){%>
    <div class="paragraph">
        <div class="boxContainer">
            <div class="box">
                <div class="boxTitle"><%=$SH("_statusChange")%>&nbsp;<%=$SH("_by")%>&nbsp;<%=$H(statusData.getCreatorName())%>&nbsp;
                    <%=$SH("_ofDate")%>&nbsp;<%=DateHelper.toHtml(statusData.getCreationDate())%>
                </div>
                <div class="boxText"><%=StringHelper.toHtmlMultiline(statusData.getDescription())%></div>
            </div>
        </div>
        <div class="d-flex flex-wrap align-items-stretch boxContainer">
            <div class="box">
                <div class="boxTitle"><%=$SH("_status")%></div>
                <div class="boxText"><%=LocalizedSystemStrings.getInstance().html(statusData.getStatus().toString())%></div>
            </div>
            <div class="box">
                <div class="boxTitle"><%=$SH("_assigned")%></div>
                <div class="boxText"><%=$H(statusData.getAssignedName())%></div>
            </div>
        </div>
        <%
        if (!statusData.getFiles().isEmpty()){%>
        <div class="d-flex flex-wrap align-items-stretch boxContainer">
            <div class="box">
                <div class="boxTitle"><%=$SH("_images")%></div>
                <div class="box">
                <% for (FileData file : statusData.getFiles(ImageData.class)){%>
                    <a href="<%=file.getStaticURL()%>" target="_blank" title="<%=$SH("_view")%>"><img src="/ctrl/image/showPreview/<%=file.getId()%>" alt="" /></a>
                    <%}%>
                </div>
            </div>
        </div>
        <%}%>
    </div>
    <%
    }
    if (!contentData.isClosed()){%>
    <div class=buttonLine>
        <%if (contentData.hasUserEditRight(rdata.getLoginUser())) {%>
        <button type="button" class="btn btn-sm btn-outline-secondary" onclick="return openModalDialog('/ctrl/defect/openEditFrontendContent/<%=contentData.getId()%>',null);"><%=$SH("_edit")%>
        </button>
        <button type="button" class="btn btn-sm btn-outline-secondary" onclick="return openModalDialog('/ctrl/defectstatus/openCreateFrontendContent?parentId=<%=contentData.getId()%>',null);"><%=$SH("_statusChange")%>
        </button>
        <button type="button" class="btn btn-sm btn-outline-secondary" onclick="return openModalDialog('/ctrl/defect/closeDefect/<%=contentData.getId()%>',null);"><%=$SH("_closeDefect")%>
        </button>
        <%}%>
        <button type="button" class="btn btn-sm btn-outline-secondary" onclick="return linkTo('/ctrl/defect/getPdfFile/<%=contentData.getId()%>');"><%=$SH("_downloadPdf")%>
        </button>
    </div>
    <%}%>
</section>
<%}%>
