<%response.setContentType("text/html;charset=UTF-8");%>
<%@ page trimDirectiveWhitespaces="true" %>
<%@ include file="/WEB-INF/_jsp/_include/_functions.inc.jsp" %>
<%@ page import="de.elbe5.request.RequestData" %>
<%@ page import="de.elbe5.content.ContentData" %>
<%@ page import="de.elbe5.content.ContentCache" %>
<%@ page import="de.elbe5.project.ProjectData" %>
<%@ page import="de.elbe5.user.CodefUserData" %>
<%@ page import="de.elbe5.unit.UnitData" %>
<%@ page import="de.elbe5.dailyreport.DailyReport" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%
    RequestData rdata = RequestData.getRequestData(request);
    CodefUserData user = rdata.getLoginUser(CodefUserData.class);
    if (user!=null && user.getProjectId()!=0){
        ProjectData project = ContentCache.getContent(user.getProjectId(),ProjectData.class);
        if (project!=null && project.hasUserReadRight(rdata.getLoginUser())){
%>
<li class="nav-item">
    <a class="nav-link" href="<%=project.getUrl()%>"><%=$H(project.getDisplayName())%>
    </a>
</li>
<li class="nav-item dropdown">
    <a class="nav-link dropdown-toggle" data-toggle="dropdown" href="" role="button" aria-haspopup="true" aria-expanded="false"><%=$SH("_units")%>
    </a>
    <div class="dropdown-menu">
        <% for (ContentData child : project.getChildren(UnitData.class)){
            if (child.isActive()){
        %>
        <a class="dropdown-item" href="<%=child.getUrl()%>"><%=$H(child.getDisplayName())%></a>
        <%}
        }%>
    </div>
</li>
<li class="nav-item dropdown">
    <a class="nav-link dropdown-toggle" data-toggle="dropdown" href="" role="button" aria-haspopup="true" aria-expanded="false"><%=$SH("_projectDailyReports")%>
    </a>
    <div class="dropdown-menu">
        <a class="dropdown-item" style="font-weight: bold" href="/ctrl/dailyreport/openCreateFrontendContent?parentId=<%=project.getId()%>"><%=$SH("_createNewDailyReport")%></a>
        <%List<DailyReport> reports = new ArrayList(project.getChildren(DailyReport.class));
        Collections.reverse(reports);
        for (ContentData child : reports){
            if (child.isActive()){
        %>
        <a class="dropdown-item" href="<%=child.getUrl()%>"><%=$H(child.getDisplayName())%></a>
        <%}
        }%>
    </div>
</li>
<%}
    }%>
