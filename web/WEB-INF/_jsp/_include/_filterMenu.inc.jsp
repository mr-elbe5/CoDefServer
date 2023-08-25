<%response.setContentType("text/html;charset=UTF-8");%>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ include file="/WEB-INF/_jsp/_include/_functions.inc.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="de.elbe5.application.ViewFilter" %>
<%@ page import="de.elbe5.request.RequestData" %>
<%
  RequestData rdata = RequestData.getRequestData(request);
  ViewFilter filter= ViewFilter.getFilter(rdata);
%>

<ul class="nav filter justify-content-end">
  <% if (filter.isEditor()){%>
  <li>
    <a class="fa fa-filter" onclick="return openModalDialog('/ctrl/filter/openCompanyFilter/<%=rdata.getId()%>');">&nbsp;<%=$SH("_companies")%>&nbsp;(<%=filter.getWatchedIds().size()%>)</a>
  </li>
  <%}%>
  <li>
    <a class="fa fa-filter" onclick="return openModalDialog('/ctrl/filter/openStatusFilter/<%=rdata.getId()%>');">&nbsp;<%=$SH("_status")%></a>
  </li>
</ul>
