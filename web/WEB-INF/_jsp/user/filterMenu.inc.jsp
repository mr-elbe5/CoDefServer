<%response.setContentType("text/html;charset=UTF-8");%>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ include file="/WEB-INF/_jsp/_include/_functions.inc.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="de.elbe5.request.RequestData" %>
<%@ page import="de.elbe5.user.CodefUserData" %>
<%
  RequestData rdata = RequestData.getRequestData(request);
  CodefUserData user = rdata.getLoginUser(CodefUserData.class);
%>

<ul class="nav filter justify-content-end">
  <li>
    <a class="fa fa-tablet" onclick="return openModalDialog('/ctrl/user/openSyncFilter/<%=rdata.getId()%>');">&nbsp;<%=$SH("_synchronization")%></a>
  </li>
  <li>
    <a class="fa fa-filter" onclick="return openModalDialog('/ctrl/user/openCompanyFilter/<%=rdata.getId()%>');">&nbsp;<%=$SH("_companies")%>&nbsp;(<%=user.getCompanyIds().size()%>)</a>
  </li>
  <li>
    <a class="fa fa-filter" onclick="return openModalDialog('/ctrl/user/openViewFilter/<%=rdata.getId()%>');">&nbsp;<%=$SH("_view")%></a>
  </li>
</ul>
