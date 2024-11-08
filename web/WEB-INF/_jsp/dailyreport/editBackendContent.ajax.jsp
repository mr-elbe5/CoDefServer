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
<%@ page import="de.elbe5.dailyreport.DailyReport" %>
<%@ page import="de.elbe5.company.CompanyData" %>
<%@ page import="java.util.List" %>
<%@ page import="de.elbe5.company.CompanyCache" %>
<%@ page import="de.elbe5.dailyreport.CompanyBriefing" %>
<%@ taglib uri="/WEB-INF/formtags.tld" prefix="form" %>
<%
    RequestData rdata = RequestData.getRequestData(request);

    DailyReport report = ContentData.getCurrentContent(rdata, DailyReport.class);
    assert (report != null);
    ProjectData project = report.getProject();
    assert (project != null);
    List<CompanyData> companies = CompanyCache.getCompanies(project.getCompanyIds());
    String url = "/ctrl/dailyreport/saveBackendContent/" + report.getId();%>
<div class="modal-dialog modal-xl" role="document">
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
                <form:line label="_id"><%=$I(report.getId())%>
                </form:line>
                <form:line label="_idx"><%=$I(report.getIdx())%>
                </form:line>
                <form:line label="_creation"><%=$H(report.getCreationDate())%> - <%=$H(report.getCreatorName())%>
                </form:line>
                <form:line label="_lastChange"><%=$H(report.getChangeDate())%> - <%=$H(report.getChangerName())%>
                </form:line>
                <form:line label="_name"><%=$H(report.getDisplayName())%>
                </form:line>
                <form:text name="reportDate" label="_reportDate" value="<%=DateHelper.toHtmlDateTime(report.getReportDate())%>" required="true"/>
                <form:text name="weatherCoco" label="_weatherConditions" required="false" value="<%=$H(report.getWeatherCoco())%>"/>
                <form:text name="weatherWspd" label="_windSpeed" required="false" value="<%=$H(report.getWeatherWspd())%>"/>
                <form:text name="weatherWdir" label="_windDirection" required="false" value="<%=$H(report.getWeatherWdir())%>"/>
                <form:text name="weatherTemp" label="_temperature" required="false" value="<%=$H(report.getWeatherTemp())%>"/>
                <form:text name="weatherRhum" label="_relativeHumidity" required="false" value="<%=$H(report.getWeatherRhum())%>"/>
                <form:line>
                    <button type="button" class="btn btn-sm btn-outline-secondary" onclick="updateWeather()"><%=$SH("_updateWeatherData")%>
                    </button>
                </form:line>
                <form:line label="_presentCompanies" padded="true">
                    <table style="width:100%">
                        <tr><th><%=$SH("_company")%><th><%=$SH("_activity")%></th><th><%=$SH("_briefing")%></tr>
                        <% for (CompanyData company : companies){
                            CompanyBriefing briefing = report.getCompanyBriefing(company.getId());
                        %>
                                <tr>
                                    <td style="vertical-align: top;"><input type="checkbox" name="company_<%=company.getId()%>_present" value="true" <%=briefing == null ? "" : "checked"%> /> <label><%=$H(company.getName())%></label></td>
                                    <td><textarea style="height: 60px;"  name="company_<%=company.getId()%>_activity" class="form-control"><%=briefing == null ? "" : $H(briefing.getActivity())%></textarea></td>
                                    <td><textarea style="height: 60px;"  name="company_<%=company.getId()%>_briefing" class="form-control"><%=briefing == null ? "" : $H(briefing.getBriefing())%></textarea></td>
                                </tr>
                        <%}%>
                    </table>
                </form:line>
                <form:textarea name="comment" label="_generalComment" height="5rem" required="false"><%=$H(report.getComment())%></form:textarea>
                <form:line label="_active" padded="true">
                    <form:check name="active" value="true" checked="<%=report.isActive()%>"/>
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
<script type="application/javascript">
    function updateWeather(){
        let reportDate = $('#reportDate').val()
        //console.log(reportDate);
        $.ajax({
            url: '/ctrl/dailyreport/updateWeather/<%=report.getId()%>', type: 'POST', data: {reportDate : reportDate}, cache: false, dataType: 'json'
        }).success(function (json, textStatus) {
            $('#weatherCoco').val(json.weatherCoco);
            $('#weatherWspd').val(json.weatherWspd);
            $('#weatherWdir').val(json.weatherWdir);
            $('#weatherTemp').val(json.weatherTemp);
            $('#weatherRhum').val(json.weatherRhum);
        });
        return false;
    }
</script>


