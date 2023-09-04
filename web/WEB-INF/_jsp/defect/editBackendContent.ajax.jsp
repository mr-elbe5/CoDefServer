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
<%@ page import="de.elbe5.defect.DefectData" %>
<%@ page import="de.elbe5.company.CompanyData" %>
<%@ page import="de.elbe5.unit.UnitData" %>
<%@ page import="de.elbe5.project.ProjectData" %>
<%@ page import="java.util.List" %>
<%@ page import="de.elbe5.company.CompanyCache" %>
<%@ page import="de.elbe5.project.ProjectPhase" %>
<%@ taglib uri="/WEB-INF/formtags.tld" prefix="form" %>
<%
    RequestData rdata = RequestData.getRequestData(request);

    DefectData contentData = ContentData.getCurrentContent(rdata, DefectData.class);
    assert (contentData != null);
    UnitData unit = contentData.getUnit();
    assert(unit !=null);
    ProjectData project= contentData.getProject();
    assert(project!=null);
    List<CompanyData> companies = CompanyCache.getCompanies(project.getCompanyIds());
    String url = "/ctrl/defect/saveBackendContent/" + contentData.getId();%>
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
                <form:line label="_idAndUrl"><%=$I(contentData.getId())%> - <%=$H(contentData.getUrl())%>
                </form:line>
                <form:line label="_creation"><%=$DT(contentData.getCreationDate())%> - <%=$H(contentData.getCreatorName())%>
                </form:line>
                <form:line label="_lastChange"><%=$DT(contentData.getChangeDate())%> - <%=$H(contentData.getChangerName())%>
                </form:line>
                <form:line label="_name"><%=$H(contentData.getDisplayName())%>
                </form:line>
                <form:textarea name="description" label="_description" height="5em"><%=$H(contentData.getDescription())%></form:textarea>
                <form:select name="assignedId" label="_assignTo" required="true">
                    <option value="0" <%=contentData.getAssignedId()==0 ? "selected" : ""%>><%=$SH("_pleaseSelect")%></option>
                    <% for (CompanyData company : companies){%>
                    <option value="<%=company.getId()%>" <%=contentData.getAssignedId()==company.getId() ? "selected" : ""%>><%=$H(company.getName())%></option>
                    <%}%>
                </form:select>
                <form:select name="defectType" label="_defectType">
                    <option value="<%=ProjectPhase.PREAPPROVE.toString()%>" <%=ProjectPhase.PREAPPROVE.equals(defect.getDefectType()) ? "selected" : ""%>><%=$SH(ProjectPhase.PREAPPROVE.name())%></option>
                    <option value="<%=ProjectPhase.LIABILITY.toString()%>" <%=ProjectPhase.LIABILITY.equals(defect.getDefectType()) ? "selected" : ""%>><%=$SH(ProjectPhase.LIABILITY.name())%></option>
                </form:select>
                <form:line label="_notified" padded = "true"><form:check name="notified" value="true" checked="<%=contentData.isNotified()%>"/></form:line>
                <form:date name="dueDate1" label="_dueDate1" value="<%=$D(contentData.getDueDate1())%>" required="true"/>
                <form:date name="dueDate2" label="_dueDate2" value="<%=$D(contentData.getDueDate2())%>"/>
                <% if (unit.getPlan() != null) {%>
                <form:line label="_position"> </form:line>
                <div id="planContainer">
                    <img id="plan" src="/files/<%=contentData.getPlanId()%>" alt="" style="border:1px solid red; width:100%"/>
                    <div id="planPositioner">
                        <img id="arrow" src="/static-content/img/redarrow.png" alt=""/>
                        <span><%=contentData.getDisplayId()%></span>
                    </div>
                </div>
                <input type="hidden" name="positionX" id="positionX" value="<%=contentData.getPositionX()%>"/>
                <input type="hidden" name="positionY" id="positionY" value="<%=contentData.getPositionY()%>"/>
                <%}%>
                <form:textarea name="positionComment" label="_positionComment" height="5em"><%=$H(contentData.getPositionComment())%></form:textarea>
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
        posX = (<%=contentData.getPositionX()%>)*$plan.width();
        posY = (<%=contentData.getPositionY()%>)*$plan.height();
        //console.log('posX,posY=' + posX + ',' + posY);
        setPositioner();
    });

</script>
<%}%>