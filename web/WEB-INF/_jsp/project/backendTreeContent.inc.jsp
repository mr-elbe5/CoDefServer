<%--
  Elbe 5 CMS - A Java based modular Content Management System
  Copyright (C) 2009-2020 Michael Roennau

  This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
--%><%response.setContentType("text/html;charset=UTF-8");%>
<%@ page trimDirectiveWhitespaces="true" %>
<%@include file="/WEB-INF/_jsp/_include/_functions.inc.jsp" %>
<%@ page import="de.elbe5.request.RequestData" %>
<%@ page import="de.elbe5.content.ContentData" %>
<%@ page import="de.elbe5.configuration.CodefConfiguration" %>
<%@ page import="de.elbe5.project.ProjectData" %>
<%@ page import="de.elbe5.unit.UnitData" %>
<%@ page import="java.util.List" %>
<%@ page import="de.elbe5.dailyreport.DailyReport" %>
<%@ taglib uri="/WEB-INF/formtags.tld" prefix="form" %>
<%
    RequestData rdata = RequestData.getRequestData(request);

    ProjectData contentData = ContentData.getCurrentContent(rdata, ProjectData.class);
    assert contentData != null;
%>
<% if (contentData.isActive() || CodefConfiguration.isShowInactiveContent()){%>
<li class="">
    <span class="<%=!contentData.isActive() ? "inactive" : ""%>">
        <%=$H(contentData.getDisplayName())%>
    </span>
    <%if (contentData.hasUserEditRight(rdata.getLoginUser())) {%>
    <div class="icons">
        <a class="icon fa fa-eye" href="" onclick="return linkTo('/ctrl/content/show/<%=contentData.getId()%>');" title="<%=$SH("_view")%>"> </a>
        <a class="icon fa fa-pencil" href="" onclick="return openModalDialog('/ctrl/project/openEditBackendContent/<%=contentData.getId()%>');" title="<%=$SH("_edit")%>"> </a>
        <a class="icon fa fa-trash-o" href="" onclick="if (confirmDelete()) return linkTo('/ctrl/content/deleteBackendContent/<%=contentData.getId()%>');" title="<%=$SH("_delete")%>"> </a>
        <a class="icon fa fa-plus" onclick="return openModalDialog('/ctrl/unit/openCreateBackendContent?parentId=<%=contentData.getId()%>&type=de.elbe5.unit.UnitData');" title="<%=$SH("_newUnit")%>"></a>
        <a class="icon fa fa-calendar-o" onclick="return openModalDialog('/ctrl/dailyreport/openCreateBackendContent?parentId=<%=contentData.getId()%>&type=de.elbe5.dailyreport.DailyReport');" title="<%=$SH("_newDailyReport")%>"></a>
    </div>
    <%}%>
    <ul>
        <%List<UnitData> units = contentData.getChildren(UnitData.class);
        if (!units.isEmpty()) {%>
            <li class= "open">
                <span>
                    <%=$SH("_units")%>
                </span>
                <ul>
            <%for (UnitData unit : units) {
                unit.displayBackendTreeContent(pageContext, rdata);
            }%>
                </ul>
            </li>
        <%}
        List<DailyReport> diaries = contentData.getChildren(DailyReport.class);
        if (!diaries.isEmpty()) {%>
            <li class= "open">
                <span class="icon fa fa-calendar-o">
                    <%=$SH("_projectDailyReports")%>
                </span>
                <ul>
                    <%for (DailyReport diary : diaries) {
                        diary.displayBackendTreeContent(pageContext, rdata);
                    }%>
                </ul>
            </li>
        <%}%>
    </ul>
</li>
<%}%>

