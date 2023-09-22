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
<%@ page import="de.elbe5.content.ContentData" %>
<%@ page import="de.elbe5.defectstatus.DefectStatusData" %>
<%@ page import="de.elbe5.defect.DefectStatus" %>
<%@ page import="de.elbe5.company.CompanyCache" %>
<%@ page import="de.elbe5.company.CompanyData" %>
<%@ page import="de.elbe5.defect.DefectData" %>
<%@ page import="de.elbe5.project.ProjectData" %>
<%@ taglib uri="/WEB-INF/formtags.tld" prefix="form" %>
<%
    RequestData rdata = RequestData.getRequestData(request);

    DefectStatusData statusChange = ContentData.getCurrentContent(rdata, DefectStatusData.class);
    assert (statusChange != null);
    DefectData defect = statusChange.getParent(DefectData.class);
    assert (defect != null);
    ProjectData project = defect.getProject();
    assert (project != null);
    String url = "/ctrl/defectstatus/saveBackendContent/" + statusChange.getId();%>
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
                <form:line label="_id"><%=$I(statusChange.getId())%>
                </form:line>
                <form:line label="_creation"><%=$H(statusChange.getCreationDate())%> - <%=$H(statusChange.getCreatorName())%>
                </form:line>
                <form:line label="_lastChange"><%=$H(statusChange.getChangeDate())%> - <%=$H(statusChange.getChangerName())%>
                </form:line>
                <form:line label="_name"><%=$H(statusChange.getDisplayName())%>
                </form:line>
                <form:select name="assignedId" label="_assignTo" required="true">
                    <option value="0" <%=statusChange.getAssignedId() == 0 ? "selected" : ""%>><%=$SH("_pleaseSelect")%>
                    </option>
                    <% for (int companyId : project.getCompanyIds()) {
                        CompanyData company = CompanyCache.getCompany(companyId);%>
                    <option value="<%=companyId%>" <%=statusChange.getAssignedId() == company.getId() ? "selected" : ""%>><%=$H(company.getName())%>
                    </option>
                    <%}%>
                </form:select>
                <form:select name="status" label="_status">
                    <option value="<%=DefectStatus.OPEN.toString()%>" <%=DefectStatus.OPEN.equals(statusChange.getStatus()) ? "selected" : ""%>><%=$SH(DefectStatus.OPEN.toString())%></option>
                    <option value="<%=DefectStatus.DISPUTED.toString()%>" <%=DefectStatus.DISPUTED.equals(statusChange.getStatus()) ? "selected" : ""%>><%=$SH(DefectStatus.DISPUTED.toString())%></option>
                    <option value="<%=DefectStatus.REJECTED.toString()%>" <%=DefectStatus.REJECTED.equals(statusChange.getStatus()) ? "selected" : ""%>><%=$SH(DefectStatus.REJECTED.toString())%></option>
                    <option value="<%=DefectStatus.DONE.toString()%>" <%=DefectStatus.DONE.equals(statusChange.getStatus()) ? "selected" : ""%>><%=$SH(DefectStatus.DONE.toString())%></option>
                </form:select>
                <form:textarea name="description" label="_notes" height="5em"><%=$H(statusChange.getDescription())%></form:textarea>
                <form:line label="_active" padded="true">
                    <form:check name="active" value="true" checked="<%=statusChange.isActive()%>"/>
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


