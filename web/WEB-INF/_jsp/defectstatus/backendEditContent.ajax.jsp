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
<%@ page import="de.elbe5.defectstatus.StatusChangeData" %>
<%@ page import="de.elbe5.defect.DefectData" %>
<%@ taglib uri="/WEB-INF/formtags.tld" prefix="form" %>
<%
    RequestData rdata = RequestData.getRequestData(request);

    StatusChangeData contentData = ContentData.getCurrentContent(rdata, StatusChangeData.class);
    assert (contentData != null);
    String url = "/ctrl/statuschange/saveBackendContent/" + contentData.getId();%>
<div class="modal-dialog modal-lg" role="document">
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

                <form:select name="status" label="_status">
                    <option value="<%=DefectData.STATUS_OPEN%>" <%=DefectData.STATUS_OPEN.equals(contentData.getStatus()) ? "selected" : ""%>><%=$SH(DefectData.STATUS_OPEN)%></option>
                    <option value="<%=DefectData.STATUS_DISPUTED%>" <%=DefectData.STATUS_DISPUTED.equals(contentData.getStatus()) ? "selected" : ""%>><%=$SH(DefectData.STATUS_DISPUTED)%></option>
                    <option value="<%=DefectData.STATUS_REJECTED%>" <%=DefectData.STATUS_REJECTED.equals(contentData.getStatus()) ? "selected" : ""%>><%=$SH(DefectData.STATUS_REJECTED)%></option>
                    <option value="<%=DefectData.STATUS_DONE%>" <%=DefectData.STATUS_DONE.equals(contentData.getStatus()) ? "selected" : ""%>><%=$SH(DefectData.STATUS_DONE)%></option>
                </form:select>
                <form:textarea name="description" label="_notes" height="5em"><%=$H(contentData.getDescription())%></form:textarea>
                <form:line label="_active" padded="true">
                    <form:check name="active" value="true" checked="<%=contentData.isActive()%>"/>
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


