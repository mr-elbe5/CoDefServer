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
<%@ page import="de.elbe5.content.ContentCache" %>
<%@ page import="de.elbe5.group.GroupData" %>
<%@ page import="de.elbe5.group.GroupBean" %>
<%@ page import="de.elbe5.user.UserData" %>
<%@ page import="de.elbe5.user.UserCache" %>
<%@ page import="de.elbe5.application.ViewFilter" %>
<%@ page import="de.elbe5.project.ProjectData" %>
<%@ taglib uri="/WEB-INF/formtags.tld" prefix="form" %>
<%
    RequestData rdata = RequestData.getRequestData(request);
    int projectId=rdata.getAttributes().getInt("projectId");
    ViewFilter filter= ViewFilter.getFilter(rdata);
    GroupData group=null;
    ProjectData project=ContentCache.getContent(projectId, ProjectData.class);
    if (project!=null)
        group= GroupBean.getInstance().getGroup(project.getGroupId());
%>
                    <% if (group!=null){
                        for (int userId : group.getUserIds()){
                            UserData user= UserCache.getUser(userId);
                            if (user==null)
                                continue;%>
            <div class="form-check">
                <input class="form-check-input" name="users" type="checkbox" value="<%=user.getId()%>" id="check<%=user.getId()%>" <%=filter.getWatchedIds().contains(user.getId()) ? "checked" : ""%>>
                <label class="form-check-label" for="check<%=user.getId()%>">
                    <%=$H(user.getName())%>
                </label>
            </div>
<%
                        }
                    }%>

