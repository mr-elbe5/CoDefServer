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
<%@ page import="java.util.List" %>
<%@ page import="de.elbe5.project.ProjectData" %>
<%@ page import="de.elbe5.content.ContentData" %>
<%@ page import="de.elbe5.company.CompanyData" %>
<%@ page import="de.elbe5.company.CompanyCache" %>
<%@ page import="de.elbe5.group.GroupData" %>
<%@ page import="de.elbe5.group.GroupCache" %>
<%@ page import="de.elbe5.configuration.StaticConfiguration" %>
<%@ taglib uri="/WEB-INF/formtags.tld" prefix="form" %>
<%
    RequestData rdata = RequestData.getRequestData(request);
    ProjectData project = ContentData.getCurrentContent(rdata, ProjectData.class);
    assert (project != null);
    List<CompanyData> companies = CompanyCache.getAllCompanies();
    String url = "/ctrl/project/saveBackendContent/" + project.getId();
    List<GroupData> groups = GroupCache.getAllGroups();
%>
<div class="modal-dialog modal-lg" role="document">
    <div class="modal-content">
        <div class="modal-header">
            <h5 class="modal-title"><%=$SH("_edit")%>
            </h5>
            <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                <span aria-hidden="true">&times;</span>
            </button>
        </div>
        <form:form url="<%=url%>" name="pageform" ajax="true">
            <div class="modal-body">
                <form:formerror/>
                <h3><%=$SH("_settings")%>
                </h3>
                <form:line label="_idAndUrl"><%=$I(project.getId())%> - <%=$H(project.getUrl())%>
                </form:line>
                <form:line label="_creation"><%=$H(project.getCreationDate())%> - <%=$H(project.getCreatorName())%>
                </form:line>
                <form:line label="_lastChange"><%=$H(project.getChangeDate())%> - <%=$H(project.getChangerName())%>
                </form:line>
                <form:text name="displayName" label="_name" required="true" value="<%=$H(project.getDisplayName())%>"/>
                <form:textarea name="description" label="_description" height="5em"><%=$H(project.getDescription())%></form:textarea>
                <% if (StaticConfiguration.useReadRights()){%>
                <form:line label="_openAccess" padded="true">
                    <form:check name="openAccess" value="true" checked="<%=project.isOpenAccess()%>"/>
                </form:line>
                <%}%>
                <% if (StaticConfiguration.useReadRights() && StaticConfiguration.useReadGroup()){%>
                <form:select name="readerGroupId" label="_readerGroup">
                    <option value="0"  <%=project.getReaderGroupId()==0 ? "selected" : ""%>><%=$SH("_none")%></option>
                    <% for (GroupData group : groups){%>
                    <option value="<%=group.getId()%>" <%=project.getReaderGroupId()==group.getId() ? "selected" : ""%>><%=$H(group.getName())%></option>
                    <%}%>
                </form:select>
                <%}%>
                <% if (StaticConfiguration.useEditorGroup()){%>
                <form:select name="editorGroupId" label="_editorGroup">
                    <option value="0"  <%=project.getEditorGroupId()==0 ? "selected" : ""%>><%=$SH("_none")%></option>
                    <% for (GroupData group : groups){%>
                    <option value="<%=group.getId()%>" <%=project.getEditorGroupId()==group.getId() ? "selected" : ""%>><%=$H(group.getName())%></option>
                    <%}%>
                </form:select>
                <%}%>
                <form:line label="_companies" name="companyIds" padded="true" required="true">
                    <% for (CompanyData company : companies){%>
                    <form:check name="companyIds" value="<%=Integer.toString(company.getId())%>" checked="<%=project.getCompanyIds().contains(company.getId())%>"><%=$H(company.getName())%>
                    </form:check><br/>
                    <%}%>
                </form:line>
                <form:line label="_active" padded="true">
                    <form:check name="active" value="true" checked="<%=project.isActive()%>"/>
                </form:line>
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


