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
<%@ page import="de.elbe5.defect.DefectData" %>
<%@ page import="de.elbe5.unit.UnitData" %>
<%@ page import="de.elbe5.project.ProjectData" %>
<%@ page import="de.elbe5.content.ContentData" %>
<%@ page import="de.elbe5.company.CompanyData" %>
<%@ page import="java.util.List" %>
<%@ page import="de.elbe5.company.CompanyCache" %>
<%@ page import="de.elbe5.project.ProjectPhase" %>
<%@ taglib uri="/WEB-INF/formtags.tld" prefix="form" %>
<%
    RequestData rdata = RequestData.getRequestData(request);

    DefectData defect = ContentData.getCurrentContent(rdata, DefectData.class);
    assert(defect !=null);
    UnitData unit = defect.getUnit();
    assert(unit !=null);
    ProjectData project= defect.getProject();
    assert(project!=null);
    List<CompanyData> companies = CompanyCache.getCompanies(project.getCompanyIds());
    String url = "/ctrl/defect/saveFrontendContent/" + defect.getId();
    if (defect.hasUserEditRight(rdata.getLoginUser())){
%>
<form:message/>
<section class="contentTop">
    <h1>
        <%=$H(defect.getDisplayName())%>
    </h1>
</section>
<section class="contentSection" id="content">
    <form:form url="<%=url%>" name="pageform" multi="true">
        <form:formerror/>
        <form:line label="_id" padded="true"><%=Integer.toString(defect.getDisplayId())%></form:line>
        <form:line label="_description" padded="true"><%=$HML(defect.getDescription())%></form:line>
        <form:line label="_editedBy" padded="true"><%=$H(defect.getChangerName())%> (<%=$DT(defect.getChangeDate())%>)</form:line>
        <form:select name="assignedId" label="_assignTo" required="true">
            <option value="0" <%=defect.getAssignedId()==0 ? "selected" : ""%>><%=$SH("_pleaseSelect")%></option>
            <% for (CompanyData company : companies){%>
            <option value="<%=company.getId()%>" <%=defect.getAssignedId()==company.getId() ? "selected" : ""%>><%=$H(company.getName())%></option>
            <%}%>
        </form:select>
        <form:select name="defectType" label="_defectType">
            <option value="<%=ProjectPhase.PREAPPROVE.toString()%>" <%=ProjectPhase.PREAPPROVE.equals(defect.getProjectPhase()) ? "selected" : ""%>><%=$SH(ProjectPhase.PREAPPROVE.name())%></option>
            <option value="<%=ProjectPhase.LIABILITY.toString()%>" <%=ProjectPhase.LIABILITY.equals(defect.getProjectPhase()) ? "selected" : ""%>><%=$SH(ProjectPhase.LIABILITY.name())%></option>
        </form:select>
        <form:line label="_notified" padded = "true"><form:check name="notified" value="true" checked="<%=defect.isNotified()%>"/></form:line>
        <form:date name="dueDate2" label="_dueDate2" value="<%=$D(defect.getDueDate2())%>" required="true"/>
        <% if (defect.getPlan()!=null){%>
        <form:line label="_position"><img src="/ctrl/defect/showCroppedDefectPlan/<%=defect.getId()%>" alt="" /></form:line>
        <%}%>
        <form:line label="_positionComment" padded="true"><%=$HML(defect.getPositionComment())%></form:line>
        <form:file name="files" label="_addDocumentsAndImages" required="false" multiple="true"/>
        <div>
            <button type="button" class="btn btn-outline-secondary" onclick="linkTo('/ctrl/defect/show/<%=defect.getId()%>');"><%=$SH("_cancel")%>
            </button>
            <button type="submit" class="btn btn-primary"><%=$SH("_save")%>
            </button>
        </div>
    </form:form>
</section>
<%}%>





