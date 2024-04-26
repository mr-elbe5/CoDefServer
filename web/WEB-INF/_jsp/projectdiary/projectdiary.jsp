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
<%@ page import="de.elbe5.content.ContentData" %>
<%@ page import="de.elbe5.file.ImageData" %>
<%@ page import="de.elbe5.projectdiary.ProjectDiary" %>
<%@ taglib uri="/WEB-INF/formtags.tld" prefix="form" %>
<%
    RequestData rdata = RequestData.getRequestData(request);

    ProjectDiary contentData = ContentData.getCurrentContent(rdata, ProjectDiary.class);
    assert(contentData !=null);
    if (contentData.hasUserReadRight(rdata.getLoginUser())){
%>
<form:message/>
<section class="contentSection" id="content">
    <div class="paragraph">
        <h3><%=$SH("_diary")%>&nbsp;<%=$H(contentData.getDisplayName())%></h3>
        <div class="d-flex flex-wrap align-items-stretch boxContainer">
            <div class="box">
                <div class="boxTitle"><%=$SH("_weatherConditions")%></div>
                <div class="boxText"><%=$H(contentData.getWeatherCoco())%></div>
            </div>
            <div class="box">
                <div class="boxTitle"><%=$SH("_windSpeed")%></div>
                <div class="boxText"><%=$H(contentData.getWeatherWspd())%></div>
            </div>
            <div class="box">
                <div class="boxTitle"><%=$SH("_windDirection")%></div>
                <div class="boxText"><%=$H(contentData.getWeatherWdir())%></div>
            </div>
            <div class="box">
                <div class="boxTitle"><%=$SH("_temperature")%></div>
                <div class="boxText"><%=$H(contentData.getWeatherTemp())%></div>
            </div>
            <div class="box">
                <div class="boxTitle"><%=$SH("_relativeHumidity")%></div>
                <div class="boxText"><%=$H(contentData.getWeatherRhum())%></div>
            </div>
            <div class="box">
                <div class="boxTitle"><%=$SH("_activity")%></div>
                <div class="boxText"><%=StringHelper.toHtmlMultiline(contentData.getActivity())%></div>
            </div>
            <div class="box">
                <div class="boxTitle"><%=$SH("_presentCompanies")%></div>
                <div class="boxText"><%=contentData.getCompaniesBoxHtml()%></div>
            </div>
            <div class="box">
                <div class="boxTitle"><%=$SH("_briefing")%></div>
                <div class="boxText"><%=StringHelper.toHtmlMultiline(contentData.getBriefing())%></div>
            </div>
            <div class="box">
                <div class="boxTitle"><%=$SH("_creator")%></div>
                <div class="boxText"><%=$H(contentData.getCreatorName())%></div>
            </div>
            <% if (contentData.getChangerId() != contentData.getCreatorId()){%>
            <div class="box">
                <div class="boxTitle"><%=$SH("_editedBy")%></div>
                <div class="boxText"><%=$H(contentData.getChangerName())%></div>
            </div>
            <%}%>
            <% if (contentData.getChangeDate() != contentData.getCreationDate()){%>
            <div class="box">
                <div class="boxTitle"><%=$SH("_changeDate")%></div>
                <div class="boxText"><%=$H(contentData.getChangeDate())%></div>
            </div>
            <%}%>
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
        <% if (contentData.hasUserEditRight(rdata.getLoginUser())){%>
        <div class=buttonLine>
            <button type="button" class="btn btn-sm btn-outline-secondary" onclick="return linkTo('/ctrl/projectdiary/getPdf/<%=contentData.getId()%>');"><%=$SH("_downloadPdf")%>
            </button>
        </div>
        <%}%>
    </div>
</section>
<%}%>
