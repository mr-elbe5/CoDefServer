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
<%@ page import="de.elbe5.unit.UnitData" %>
<%@ page import="de.elbe5.defect.DefectData" %>
<%@ page import="java.util.List" %>
<%@ page import="de.elbe5.content.ContentData" %>
<%@ page import="de.elbe5.defect.DefectComparator" %>
<%@ page import="de.elbe5.user.CodefUserData" %>
<%@ taglib uri="/WEB-INF/formtags.tld" prefix="form" %>
<%
    RequestData rdata = RequestData.getRequestData(request);
    CodefUserData user = rdata.getLoginUser(CodefUserData.class);
    UnitData unit = ContentData.getCurrentContent(rdata, UnitData.class);
    assert (unit != null);
    int id= unit.getId();
    List<DefectData> defects = user.getUnitDefects(id);
%>
<form:message/>
<section class="contentTop">
    <h1>
        <%=$SH("_unit")%>&nbsp;<%=$H(unit.getDisplayName())%>
    </h1>
</section>
<section class="contentSection tableContent" id="content">
    <% if (unit.hasUserEditRight(rdata.getLoginUser())){%>
    <div class = contentTop>
        <a class="btn btn-outline-primary" href="/ctrl/defect/openCreateFrontendContent?parentId=<%=unit.getId()%>"><%=$SH("_createDefect")%>
        </a>
    </div>
    <%}%>
    <table id="defectTable" class="defect-table">
        <thead class="tableHead">
            <tr>
                <th style="width:5%"><%=$SH("_id")%>
                </th>
                <th style="width:18%"><%=$SH("_description")%>
                    <a class="fa fa-sort" onclick=linkTo("/ctrl/unit/sort/<%=id%>?sortType=<%=DefectComparator.TYPE_DESCRIPTION%>");>&nbsp;</a>
                </th>
                <th style="width:9%"><%=$SH("_creationDate")%>
                    <a class="fa fa-sort" onclick=linkTo("/ctrl/unit/sort/<%=id%>?sortType=<%=DefectComparator.TYPE_CREATION%>");>&nbsp;</a>
                </th>
                <th style="width:9%"><%=$SH("_editedBy")%>
                    <a class="fa fa-sort" onclick=linkTo("/ctrl/project/sort/<%=id%>?sortType=<%=DefectComparator.TYPE_CHANGER%>");>&nbsp;</a>
                </th>
                <th style="width:9%"><%=$SH("_changeDate")%>
                    <a class="fa fa-sort" onclick=linkTo("/ctrl/project/sort/<%=id%>?sortType=<%=DefectComparator.TYPE_CHANGE%>");>&nbsp;</a>
                </th>
                <th style="width:9%"><%=$SH("_due")%>
                    <a class="fa fa-sort" onclick=linkTo("/ctrl/unit/sort/<%=id%>?sortType=<%=DefectComparator.TYPE_DUE_DATE%>");>&nbsp;</a>
                </th>
                <th style="width:9%"><%=$SH("_closed")%>
                    <a class="fa fa-sort" onclick=linkTo("/ctrl/unit/sort/<%=id%>?sortType=<%=DefectComparator.TYPE_CLOSE_DATE%>");>&nbsp;</a>
                </th>
                <th style="width:9%"><%=$SH("_status")%>
                    <a class="fa fa-sort" onclick=linkTo("/ctrl/unit/sort/<%=id%>?sortType=<%=DefectComparator.TYPE_STATUS%>");>&nbsp;</a>
                </th>
                <th style="width:9%"><%=$SH("_assigned")%>
                    <a class="fa fa-sort" onclick=linkTo("/ctrl/unit/sort/<%=id%>?sortType=<%=DefectComparator.TYPE_ASSIGNED%>");>&nbsp;</a>
                </th>
                <th style="width:9%"><%=$SH("_defectType")%>
                    <a class="fa fa-sort" onclick=linkTo("/ctrl/unit/sort/<%=id%>?sortType=<%=DefectComparator.TYPE_DEFECTTYPE%>");>&nbsp;</a>
                </th>
                <th style="width:9%"><%=$SH("_notified")%>
                    <a class="fa fa-sort" onclick=linkTo("/ctrl/unit/sort/<%=id%>?sortType=<%=DefectComparator.TYPE_NOTIFIED%>");>&nbsp;</a>
                </th>
                <th style="width:5%"></th>
            </tr>
        </thead>
        <tbody class="tableBody">
        <% for (DefectData defect : defects){%>
            <tr class="tableRow">
                <td><%=defect.getDisplayId()%></td>
                <td><%=StringHelper.toHtml(defect.getDescription())%></td>
                <td><%=DateHelper.toHtmlDate(defect.getCreationDate())%></td>
                <td><%=StringHelper.toHtml(defect.getChangerName())%></td>
                <td><%=DateHelper.toHtmlDate(defect.getChangeDate())%></td>
                <td><%=DateHelper.toHtmlDate(defect.getDueDate())%></td>
                <td><%=DateHelper.toHtmlDate(defect.getCloseDate())%></td>
                <td><%=$SH(defect.getStatus().toString())%></td>
                <td><%=$H(defect.getAssignedName())%></td>
                <td><%=$SH(defect.getProjectPhaseString())%></td>
                <td><%=$SH(defect.isNotified() ? "_yes" : "_no")%></td>
                <td>
                    <a href="" class="fa fa-eye" title="<%=$SH("_show")%>" onclick="return linkTo('/ctrl/content/show/<%=defect.getId()%>',null);"></a>
                </td>
            </tr>
        <%
            }%>
        </tbody>
    </table>
    <% if (unit.getPlan()!=null){%>
    <div class="imageBox">
        <img src="/ctrl/unit/showDefectPlan/<%=unit.getId()%>?planId=<%=unit.getPlan().getId()%>" alt="" />
    </div>
    <%}%>
    <div class=buttonLine>
        <button type="button" class="btn btn-outline-secondary" onclick="return linkTo('/ctrl/unit/getReport/<%=unit.getId()%>');"><%=$SH("_downloadPdf")%>
        </button>
        <button type="button" class="btn btn-outline-secondary" onclick="return linkTo('/ctrl/unit/getReport/<%=unit.getId()%>?includeComments=true');"><%=$SH("_downloadPdfWithComments")%>
        </button>
    </div>
</section>
<script type="text/javascript">
    let defectTable=new FlexTable($('#defectTable'),{
        tableHeight: '20rem'
    });
    defectTable.init();
    $(window).resize(function(){
        defectTable.resize();
    });
</script>
