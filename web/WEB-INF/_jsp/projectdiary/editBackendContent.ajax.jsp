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
<%@ page import="de.elbe5.project.ProjectData" %>
<%@ page import="de.elbe5.projectdiary.ProjectDiary" %>
<%@ page import="de.elbe5.company.CompanyData" %>
<%@ page import="java.util.List" %>
<%@ page import="de.elbe5.company.CompanyCache" %>
<%@ taglib uri="/WEB-INF/formtags.tld" prefix="form" %>
<%
    RequestData rdata = RequestData.getRequestData(request);

    ProjectDiary diary = ContentData.getCurrentContent(rdata, ProjectDiary.class);
    assert (diary != null);
    ProjectData project = diary.getProject();
    assert (project != null);
    List<CompanyData> companies = CompanyCache.getCompanies(project.getCompanyIds());
    String url = "/ctrl/projectdiary/saveBackendContent/" + diary.getId();%>
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
                <form:line label="_id"><%=$I(diary.getId())%>
                </form:line>
                <form:line label="_idx"><%=$I(diary.getIdx())%>
                </form:line>
                <form:line label="_creation"><%=$H(diary.getCreationDate())%> - <%=$H(diary.getCreatorName())%>
                </form:line>
                <form:line label="_lastChange"><%=$H(diary.getChangeDate())%> - <%=$H(diary.getChangerName())%>
                </form:line>
                <form:line label="_name"><%=$H(diary.getDisplayName())%>
                </form:line>
                <form:text name="weatherCoco" label="_weatherConditions" required="false" value="<%=$H(diary.getWeatherCoco())%>"/>
                <form:text name="weatherWsdp" label="_windSpeed" required="false" value="<%=$H(diary.getWeatherWspd())%>"/>
                <form:text name="weatherWdir" label="_windDirection" required="false" value="<%=$H(diary.getWeatherWdir())%>"/>
                <form:text name="weatherTemp" label="_temperature" required="false" value="<%=$H(diary.getWeatherTemp())%>"/>
                <form:text name="weatherRhum" label="_relativeHumidity" required="false" value="<%=$H(diary.getWeatherRhum())%>"/>
                <form:line label="_presentCompanies" name="companyIds" padded="true" required="true">
                    <% for (CompanyData company : companies){%>
                    <form:check name="companyIds" value="<%=Integer.toString(company.getId())%>" checked="<%=diary.getCompanyIds().contains(company.getId())%>"><%=$H(company.getName())%>
                    </form:check><br/>
                    <%}%>
                </form:line>
                <form:textarea name="activity" label="_activity" height="5em"><%=$H(diary.getActivity())%></form:textarea>
                <form:textarea name="briefing" label="_briefing" height="5em"><%=$H(diary.getBriefing())%></form:textarea>
                <form:line label="_active" padded="true">
                    <form:check name="active" value="true" checked="<%=diary.isActive()%>"/>
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


