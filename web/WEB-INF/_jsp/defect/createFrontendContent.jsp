<%--
  Elbe 5 CMS - A Java based modular Content Management System
  Copyright (C) 2009-2020 Michael Roennau

  This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
--%><%response.setContentType("text/html;charset=UTF-8");%>
<%@ page trimDirectiveWhitespaces="true" %>
<%@include file="/WEB-INF/_jsp/_include/_functions.inc.jsp" %>
<%@ page import="de.elbe5.request.RequestData" %>
<%@ page import="de.elbe5.defect.DefectData" %>
<%@ page import="de.elbe5.project.ProjectData" %>
<%@ page import="de.elbe5.unit.UnitData" %>
<%@ page import="de.elbe5.content.ContentData" %>
<%@ page import="de.elbe5.company.CompanyData" %>
<%@ page import="de.elbe5.company.CompanyCache" %>
<%@ page import="de.elbe5.project.ProjectPhase" %>
<%@ taglib uri="/WEB-INF/formtags.tld" prefix="form" %>
<%
    RequestData rdata = RequestData.getRequestData(request);

    DefectData defect = ContentData.getCurrentContent(rdata, DefectData.class);
    assert (defect != null);
    UnitData unit = defect.getUnit();
    assert (unit != null);
    ProjectData project = defect.getProject();
    assert (project != null);
    String url = "/ctrl/defect/saveFrontendContent/" + defect.getId();

    if (defect.hasUserEditRight(rdata.getLoginUser())) {%>
<form:message/>
<section class="contentTop">
    <h1>
        <%=$H(defect.getDescription())%>
    </h1>
</section>
<section class="contentSection" id="content">
    <form:form url="<%=url%>" name="pageform" multi="true">
        <form:formerror/>
        <form:line label="_id" padded="true"><%=Integer.toString(defect.getDisplayId())%>
        </form:line>
        <form:textarea name="description" label="_description" height="5em" required="true"><%=StringHelper.toHtmlMultiline(defect.getDescription())%>
        </form:textarea>
        <form:select name="assignedId" label="_assignTo" required="true">
            <option value="0" <%=defect.getAssignedId() == 0 ? "selected" : ""%>><%=$SH("_pleaseSelect")%>
            </option>
            <% for (int companyId : project.getCompanyIds()) {
                CompanyData company = CompanyCache.getCompany(companyId);%>
            <option value="<%=companyId%>" <%=defect.getAssignedId() == company.getId() ? "selected" : ""%>><%=$H(company.getName())%>
            </option>
            <%}%>
        </form:select>
        <form:select name="defectType" label="_defectType">
            <option value="<%=ProjectPhase.PREAPPROVE.toString()%>" <%=ProjectPhase.PREAPPROVE.equals(defect.getProjectPhase()) ? "selected" : ""%>><%=$SH(ProjectPhase.PREAPPROVE.name())%></option>
            <option value="<%=ProjectPhase.LIABILITY.toString()%>" <%=ProjectPhase.LIABILITY.equals(defect.getProjectPhase()) ? "selected" : ""%>><%=$SH(ProjectPhase.LIABILITY.name())%></option>
        </form:select>
        <input type="hidden" name="notified" value="false" />
        <form:date name="dueDate1" label="_dueDate" value="<%=DateHelper.toHtmlDate(defect.getDueDate1())%>" required="true"/>
        <input type="hidden" name="dueDate2" value="" />
        <% if (unit.getPlan() != null) {%>
        <form:line label="_position"> </form:line>
        <div id="planContainer">
            <img id="plan" src="/files/<%=defect.getPlanId()%>" alt="" style="border:1px solid red"/>
            <div id="planPositioner">
                <img id="arrow" src="/static-content/img/redarrow.png" alt=""/>
                <span><%=defect.getDisplayId()%></span>
            </div>
        </div>
        <input type="hidden" name="positionX" id="positionX" value="<%=defect.getPositionX()%>"/>
        <input type="hidden" name="positionY" id="positionY" value="<%=defect.getPositionY()%>"/>
        <%}%>
        <form:textarea name="positionComment" label="_positionComment" height="5em"><%=StringHelper.toHtmlMultiline(defect.getPositionComment())%>
        </form:textarea>
        <form:file name="files" label="_addDocumentsAndImages" required="false" multiple="true"/>
        <form:line><%=$SH("_uploadHint")%></form:line>
        <div>
            <button type="button" class="btn btn-outline-secondary" onclick="linkTo('/ctrl/unit/show/<%=unit.getId()%>');"><%=$SH("_cancel")%>
            </button>
            <button type="submit" class="btn btn-primary"><%=$SH("_save")%>
            </button>
        </div>
    </form:form>
</section>
<% if (unit.getPlan() != null) {%>
<script type="text/javascript">
    let posX = 0;
    let posY = 0;
    let $container = $('#planContainer');
    console.log('posX,posY=' + posX + ',' + posY);
    console.log('container=' + $container);
    let $positioner = $('#planPositioner');
    console.log('positioner=' + $positioner);
    let $plan = $('#plan');
    console.log('plan=' + $plan);

    $plan.on('click', function (event) {
        let offset = $container.offset();
        console.log('container offset=' + offset.left + ',' + offset.top);
        posX = Math.round(event.pageX - offset.left );
        posY = Math.round(event.pageY - offset.top);
        //console.log('posX,posY=' + posX + ',' + posY);
        setPositioner();
    });

    function setPositioner() {
        $positioner.css('left', posX - 11);
        //relative, so go top
        $positioner.css('top', posY - 5 - $plan.height());
        let positionX=posX/$plan.width();
        let positionY=posY/$plan.height();
        //console.log('positionX,positionY=' + positionX + ',' + positionY);
        $('#positionX').val(positionX);
        $('#positionY').val(positionY);
    }

    $('#arrow').load(function () {
        setPositioner($container.position());
    });

    $plan.load(function () {
        posX = (<%=defect.getPositionX()%>)*$plan.width();
        posY = (<%=defect.getPositionY()%>)*$plan.height();
        //console.log('posX,posY=' + posX + ',' + posY);
        setPositioner();
    });

</script>
<%}
}%>





