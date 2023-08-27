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
<%@ page import="de.elbe5.defect.DefectData" %>
<%@ page import="de.elbe5.company.CompanyData" %>
<%@ page import="de.elbe5.unit.UnitData" %>
<%@ page import="de.elbe5.content.ContentCache" %>
<%@ page import="de.elbe5.project.ProjectData" %>
<%@ page import="java.util.List" %>
<%@ page import="de.elbe5.company.CompanyCache" %>
<%@ taglib uri="/WEB-INF/formtags.tld" prefix="form" %>
<%
    RequestData rdata = RequestData.getRequestData(request);

    DefectData contentData = ContentData.getCurrentContent(rdata, DefectData.class);
    assert (contentData != null);
    UnitData unit = ContentCache.getContent(contentData.getUnitId(), UnitData.class);
    assert(unit !=null);
    ProjectData project= ContentCache.getContent(contentData.getProjectId(),ProjectData.class);
    assert(project!=null);
    List<CompanyData> companies = CompanyCache.getInstance().getCompanies(project.getCompanyIds());
    String url = "/ctrl/defect/saveBackendContent/" + contentData.getId();%>
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
                <form:line label="_idAndUrl"><%=$I(contentData.getId())%> - <%=$H(contentData.getUrl())%>
                </form:line>
                <form:line label="_creation"><%=$DT(contentData.getCreationDate())%> - <%=$H(contentData.getCreatorName())%>
                </form:line>
                <form:line label="_lastChange"><%=$DT(contentData.getChangeDate())%> - <%=$H(contentData.getChangerName())%>
                </form:line>
                <form:line label="_name"><%=$H(contentData.getDisplayName())%>
                </form:line>
                <form:textarea name="description" label="_description" height="5em"><%=$H(contentData.getDescription())%></form:textarea>
                <form:select name="status" label="_status">
                    <option value="<%=DefectData.STATUS_OPEN%>" <%=DefectData.STATUS_OPEN.equals(contentData.getStatus()) ? "selected" : ""%>><%=$SH(DefectData.STATUS_OPEN)%></option>
                    <option value="<%=DefectData.STATUS_DISPUTED%>" <%=DefectData.STATUS_DISPUTED.equals(contentData.getStatus()) ? "selected" : ""%>><%=$SH(DefectData.STATUS_DISPUTED)%></option>
                    <option value="<%=DefectData.STATUS_REJECTED%>" <%=DefectData.STATUS_REJECTED.equals(contentData.getStatus()) ? "selected" : ""%>><%=$SH(DefectData.STATUS_REJECTED)%></option>
                    <option value="<%=DefectData.STATUS_DONE%>" <%=DefectData.STATUS_DONE.equals(contentData.getStatus()) ? "selected" : ""%>><%=$SH(DefectData.STATUS_DONE)%></option>
                </form:select>
                <form:select name="assignedId" label="_assigned" required="true">
                    <option value="0" <%=contentData.getAssignedId()==0 ? "selected" : ""%>><%=$SH("_pleaseSelect")%></option>
                    <% for (CompanyData company : companies){%>
                    <option value="<%=company.getId()%>" <%=contentData.getAssignedId()==company.getId() ? "selected" : ""%>><%=$H(company.getName())%></option>
                    <%}%>
                </form:select>
                <form:line label="_notified" padded = "true"><form:check name="notified" value="true" checked="<%=contentData.isNotified()%>"/></form:line>
                <form:text name="lot" label="_lot" value="<%=$H(contentData.getLot())%>" />
                <form:text name="costs" label="_costs" value="<%=contentData.getCostsString()%>" />
                <form:date name="dueDate1" label="_dueDate1" value="<%=$D(contentData.getDueDate1())%>" required="true"/>
                <form:date name="dueDate2" label="_dueDate2" value="<%=$D(contentData.getDueDate2())%>"/>
                <% if (contentData.getPlanId()!=0){%>
                <form:line label="_position"><img src="/ctrl/defect/showCroppedDefectPlan/<%=contentData.getId()%>" alt="" /></form:line>
                <%}%>
                <form:line label="_positionComment" padded="true"><%=$HML(contentData.getPositionComment())%></form:line>
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


