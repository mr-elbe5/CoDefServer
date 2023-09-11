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
<%@ page import="de.elbe5.project.ProjectData" %>
<%@ page import="de.elbe5.user.CodefUserData" %>
<%@ page import="de.elbe5.project.ProjectPhase" %>
<%@ page import="java.util.List" %>
<%@ taglib uri="/WEB-INF/formtags.tld" prefix="form" %>
<%
    RequestData rdata = RequestData.getRequestData(request);

    int contentId=rdata.getId();
    String url = "/ctrl/user/setViewFilter/"+contentId;
    CodefUserData user = rdata.getLoginUser(CodefUserData.class);
    GroupData group=null;
    ProjectData project=ContentCache.getContent(user.getProjectId(), ProjectData.class);
    List<ProjectData> projects = ContentCache.getContents(ProjectData.class);
    List<Integer> allowedProjectIds = user.getAllowedProjectIds();
    projects.removeIf(data -> !allowedProjectIds.contains(data.getId()));
%>
<div class="modal-dialog modal-lg" role="document">
    <div class="modal-content">
        <div class="modal-header">
            <h5 class="modal-title"><%=$SH("_setFilter")%>
            </h5>
            <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                <span aria-hidden="true">&times;</span>
            </button>
        </div>
        <form:form url="<%=url%>" name="filterform" ajax="true">
            <div class="modal-body">
                <form:formerror/>
                <form:line label="_projects" padded="true">
                    <%for (ProjectData data : projects){
                    %>
                    <div class="form-check">
                        <input class="form-check-input projectcheck" name="projectIds" type="checkbox" value="<%=data.getId()%>" id="check<%=data.getId()%>" <%=user.getProjectIds().contains(data.getId()) ? "checked" : ""%>>
                        <label class="form-check-label" for="check<%=data.getId()%>">
                            <%=$H(data.getName())%>
                        </label>
                    </div>
                    <%}%>
                </form:line>
                <form:line label="_showClosedDefects" padded="true">
                    <form:check name="showClosed" value="true" checked="<%=user.isShowClosed()%>"> </form:check>
                </form:line>
                <form:select name="projectPhase" label="_restrictToProjectPhase">
                    <option value="" <%=(user.getProjectPhase() == null) ? "selected" : ""%>><%=$SH("_noRestriction")%></option>
                    <option value="<%=ProjectPhase.PREAPPROVAL.toString()%>" <%=ProjectPhase.PREAPPROVAL.equals(user.getProjectPhase()) ? "selected" : ""%>><%=$SH("_showPreapproval")%></option>
                    <option value="<%=ProjectPhase.APPROVAL.toString()%>" <%=ProjectPhase.APPROVAL.equals(user.getProjectPhase()) ? "selected" : ""%>><%=$SH("_showApproval")%></option>
                    <option value="<%=ProjectPhase.LIABILITY.toString()%>" <%=ProjectPhase.LIABILITY.equals(user.getProjectPhase()) ? "selected" : ""%>><%=$SH("_showLiability")%></option>
                </form:select>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-outline-secondary" data-dismiss="modal"><%=$SH("_close")%>
                </button>
                <button type="submit" class="btn btn-primary"><%=$SH("_save")%>
                </button>
            </div>
        </form:form>
    </div>
</div>




