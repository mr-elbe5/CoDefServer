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
<%@ page import="java.util.List" %>
<%@ taglib uri="/WEB-INF/formtags.tld" prefix="form" %>
<%
    RequestData rdata = RequestData.getRequestData(request);
    @SuppressWarnings("unchecked")
    List<Integer> openIds = rdata.getAttributes().get("openIds", List.class);
    ContentData contentData = ContentData.getCurrentContent(rdata);
    assert contentData != null;
    String liClass = openIds != null
            ? openIds.contains(contentData.getId()) ? "open" : ""
            : "";
%>
<li class="<%=liClass%>">
    <span class="<%=contentData.isActive() ? "" : "inactive"%>">
        <%=$H(contentData.getDisplayName())%>
    </span>
    <%if (contentData.hasUserEditRight(rdata.getLoginUser())) {%>
    <div class="icons">
        <a class="icon fa fa-pencil" href="" onclick="return openModalDialog('/ctrl/defectstatus/openEditBackendContent/<%=contentData.getId()%>');" title="<%=$SH("_edit")%>"> </a>
        <a class="icon fa fa-trash-o" href="" onclick="if (confirmDelete()) return linkTo('/ctrl/content/deleteBackendContent/<%=contentData.getId()%>');" title="<%=$SH("_delete")%>"> </a>
    </div>
    <%}%>
    <ul>
        <jsp:include page="/WEB-INF/_jsp/defectstatus/backendTreeContentImages.inc.jsp" flush="true" />
    </ul>
</li>

