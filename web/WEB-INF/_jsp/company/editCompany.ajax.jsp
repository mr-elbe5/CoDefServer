<%--
  Bandika CMS - A Java based modular Content Management System
  Copyright (C) 2009-2021 Michael Roennau

  This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
--%>
<%response.setContentType("text/html;charset=UTF-8");%>
<%@ page trimDirectiveWhitespaces="true" %>
<%@include file="/WEB-INF/_jsp/_include/_functions.inc.jsp" %>
<%@ page import="de.elbe5.request.RequestData" %>
<%@ page import="de.elbe5.company.CompanyData" %>
<%@ page import="de.elbe5.user.UserBean" %>
<%@ page import="de.elbe5.user.UserData" %>
<%@ page import="java.util.List" %>
<%@ taglib uri="/WEB-INF/formtags.tld" prefix="form" %>
<%
    RequestData rdata = RequestData.getRequestData(request);
    CompanyData company = rdata.getSessionObject("companyData",CompanyData.class);
    List<UserData> users = UserBean.getInstance().getCompanyUsers(company.getId());
    String url = "/ctrl/company/saveCompany/" + company.getId();
%>
<div class="modal-dialog modal-lg" role="document">
    <div class="modal-content">
        <div class="modal-header">
            <h5 class="modal-title"><%=$SH("_editCompany")%>
            </h5>
            <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                <span aria-hidden="true">&times;</span>
            </button>
        </div>
        <form:form url="<%=url%>" name="companyform" ajax="true">
            <div class="modal-body">
                <form:formerror/>
                <h3><%=$SH("_settings")%>
                </h3>
                <form:line label="_id"><%=$I(company.getId())%>
                </form:line>
                <form:text name="name" label="_name" required="true" value="<%=$H(company.getName())%>"/>
                <form:text name="street" label="_street" value="<%=$H(company.getStreet())%>"/>
                <form:text name="zipCode" label="_zipCode" value="<%=$H(company.getZipCode())%>"/>
                <form:text name="city" label="_city" value="<%=$H(company.getCity())%>"/>
                <form:text name="country" label="_country" value="<%=$H(company.getCountry())%>"/>
                <form:text name="email" label="_email" required="true" value="<%=$H(company.getEmail())%>"/>
                <form:text name="phone" label="_phone" value="<%=$H(company.getPhone())%>"/>
                <form:text name="fax" label="_fax" value="<%=$H(company.getFax())%>"/>
                <form:line label="_employees">
                    <% for (UserData udata : users) {%>
                    <div><%=udata.getName()%></div>
                    <%}%>
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


