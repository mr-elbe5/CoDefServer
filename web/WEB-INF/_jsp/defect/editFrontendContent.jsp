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
<%@ page import="de.elbe5.configuration.CodefConfiguration" %>
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
        <form:line label="_id" padded="true"><%=Integer.toString(defect.getId())%></form:line>
        <% if (!defect.isNew()){%>
        <form:line label="_editedBy" padded="true"><%=$H(defect.getChangerName())%> (<%=$H(defect.getChangeDate())%>)</form:line>
        <%}%>
        <form:textarea name="description" label="_description" height="5em" required="true"><%=$H(defect.getDescription())%></form:textarea>
        <form:select name="assignedId" label="_assignTo" required="true">
            <option value="0" <%=defect.getAssignedId()==0 ? "selected" : ""%>><%=$SH("_pleaseSelect")%></option>
            <% for (CompanyData company : companies){%>
            <option value="<%=company.getId()%>" <%=defect.getAssignedId()==company.getId() ? "selected" : ""%>><%=$H(company.getName())%></option>
            <%}%>
        </form:select>
        <form:select name="projectPhase" label="_projectPhase">
            <option value="<%=ProjectPhase.PREAPPROVAL.toString()%>" <%=ProjectPhase.PREAPPROVAL.equals(defect.getProjectPhase()) ? "selected" : ""%>><%=$SH(ProjectPhase.PREAPPROVAL.name())%></option>
            <option value="<%=ProjectPhase.APPROVAL.toString()%>" <%=ProjectPhase.APPROVAL.equals(defect.getProjectPhase()) ? "selected" : ""%>><%=$SH(ProjectPhase.APPROVAL.name())%></option>
            <option value="<%=ProjectPhase.LIABILITY.toString()%>" <%=ProjectPhase.LIABILITY.equals(defect.getProjectPhase()) ? "selected" : ""%>><%=$SH(ProjectPhase.LIABILITY.name())%></option>
        </form:select>
        <% if (CodefConfiguration.showNotified()){%>
        <form:line label="_notified" padded = "true"><form:check name="notified" value="true" checked="<%=defect.isNotified()%>"/></form:line>
        <%}%>
        <% if (defect.isNew()){%>
        <form:date name="dueDate1" label="_dueDate" value="<%=DateHelper.toHtmlDate(defect.getDueDate1())%>" required="true"/>
        <%}else{%>
        <form:date name="dueDate2" label="_dueDate2" value="<%=$D(defect.getDueDate2())%>" required="true"/>
        <%}%>
        <% if (defect.getPlan()!=null){
            if (defect.isNew()){%>
        <form:line label="_position"> </form:line>
        <div id="planContainer">
            <img id="plan" src="/files/<%=defect.getPlanId()%>" alt="" style="border:1px solid red"/>
            <div id="planPositioner">
                <img id="arrow" src="/static-content/img/redarrow.png" alt=""/>
                <span><%=defect.getId()%></span>
            </div>
        </div>
        <input type="hidden" name="positionX" id="positionX" value="<%=defect.getPositionX()%>"/>
        <input type="hidden" name="positionY" id="positionY" value="<%=defect.getPositionY()%>"/>
        <%}else if (defect.hasValidPosition()){%>
        <form:line label="_position">
            <img src="/ctrl/defect/showCroppedDefectPlan/<%=defect.getId()%>" alt="" />
        </form:line>
        <%}
        }%>
        <form:textarea name="positionComment" label="_positionComment" height="5em"><%=$H(defect.getPositionComment())%></form:textarea>
        <form:file name="files" label="_addImages" required="false" multiple="true"/>
        <div>
            <% if (defect.isNew()){%>
            <button type="button" class="btn btn-outline-secondary" onclick="linkTo('/ctrl/unit/show/<%=unit.getId()%>');"><%=$SH("_cancel")%>
            </button>
            <%}else{%>
            <button type="button" class="btn btn-outline-secondary" onclick="linkTo('/ctrl/defect/show/<%=defect.getId()%>');"><%=$SH("_cancel")%>
            </button>
            <%}%>
            <button type="submit" class="btn btn-primary"><%=$SH("_save")%>
            </button>
        </div>
    </form:form>
</section>
<% if (unit.getPlan() != null && defect.isNew()) {%>
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
<%}}%>





