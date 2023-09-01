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
<%@ page import="de.elbe5.application.ViewFilter" %>
<%@ page import="de.elbe5.project.ProjectData" %>
<%@ page import="de.elbe5.company.CompanyData" %>
<%@ page import="java.util.List" %>
<%@ page import="de.elbe5.company.CompanyCache" %>
<%@ page import="java.util.ArrayList" %>
<%@ taglib uri="/WEB-INF/formtags.tld" prefix="form" %>
<%
    RequestData rdata = RequestData.getRequestData(request);

    int contentId=rdata.getId();
    String url = "/ctrl/filter/setCompanyFilter/"+contentId;
    ViewFilter filter= ViewFilter.getFilter(rdata);
    ProjectData project=ContentCache.getContent(filter.getProjectId(), ProjectData.class);
    List<CompanyData> companies = project!= null ? CompanyCache.getCompanies(project.getCompanyIds()) : new ArrayList<>();
%>
<div class="modal-dialog modal-lg" role="document">
    <div class="modal-content">
        <div class="modal-header">
            <h5 class="modal-title"><%=$SH("_companyFilter")%>
            </h5>
            <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                <span aria-hidden="true">&times;</span>
            </button>
        </div>
        <form:form url="<%=url%>" name="filterform" ajax="true">
            <div class="modal-body">
                <form:formerror/>
                <div class="form-check">
                    <%int companyCount = companies.size();%>
                    <input class="form-check-input" type="checkbox" id="checkall" <%=filter.getWatchedIds().size() == companyCount ? "checked" : ""%> onchange="checkAll()">
                    <label class="form-check-label" for="checkall">
                        <%=$SH("_all")%>
                    </label>
                </div>
                <hr/>
                    <%for (CompanyData company : companies){
                %>
                <div class="form-check">
                    <input class="form-check-input companycheck" name="watchedIds" type="checkbox" value="<%=company.getId()%>" id="check<%=company.getId()%>" <%=filter.getWatchedIds().contains(company.getId()) ? "checked" : ""%>>
                    <label class="form-check-label" for="check<%=company.getId()%>">
                        <%=$H(company.getName())%>
                    </label>
                </div>
                <%}%>
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

<script type="text/javascript">
    function checkAll(){
        let checked = $('#checkall').prop("checked") === true;
        $('.companycheck').prop("checked", checked);
    }

</script>



