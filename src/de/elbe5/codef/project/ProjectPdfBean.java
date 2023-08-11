/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.codef.project;

import de.elbe5.application.Configuration;
import de.elbe5.base.BinaryFile;
import de.elbe5.base.DateHelper;
import de.elbe5.base.LocalizedStrings;
import de.elbe5.content.ContentCache;
import de.elbe5.codef.DefectFopBean;
import de.elbe5.codef.ViewFilter;
import de.elbe5.codef.defect.DefectData;
import de.elbe5.codef.unit.UnitBean;
import de.elbe5.codef.unit.UnitData;
import de.elbe5.codef.unit.PlanImageData;
import de.elbe5.file.ImageBean;
import de.elbe5.request.RequestData;

import java.time.LocalDateTime;
import java.util.List;

public class ProjectPdfBean extends DefectFopBean {

    private static ProjectPdfBean instance = null;

    public static ProjectPdfBean getInstance() {
        if (instance == null) {
            instance = new ProjectPdfBean();
        }
        return instance;
    }

    public BinaryFile getProjectReport(int projectId, RequestData rdata, boolean includeComments){
        LocalDateTime now = LocalDateTime.now();
        ProjectData project= ContentCache.getContent(projectId,ProjectData.class);
        if (project==null)
            return null;
        StringBuilder sb=new StringBuilder();
        sb.append("<root>");
        addProjectHeaderXml(sb,project);
        for (UnitData location : project.getChildren(UnitData.class)) {
            List<DefectData> defects = ViewFilter.getFilter(rdata).getLocationDefects(location.getId());
            if (!defects.isEmpty()) {
                sb.append("<location>");
                sb.append("<locationheader><title>");
                sb.append(xml("_location"));
                sb.append(": ");
                sb.append(xml(location.getDisplayName()));
                sb.append("</title></locationheader>");
                addLocationDefectsXml(sb, location, defects, includeComments);
                PlanImageData plan = location.getPlan();
                if (plan != null) {
                    PlanImageData fullplan = ImageBean.getInstance().getFile(plan.getId(), true, PlanImageData.class);
                    //todo
                    /*byte[] arrowBytes = UnitBean.getInstance().getImageBytes(Configuration.getArrowPng());
                    defects = ViewFilter.getFilter(rdata).getLocationDefects(location.getId());
                    BinaryFile file = fullplan.createLocationDefectPlan(arrowBytes, defects, 1);
                    addLocationPlanXml(sb, location, plan, file);*/
                }
                sb.append("</location>");
            }
        }
        addProjectFooterXml(sb,project,now);
        sb.append("</root>");
        //System.out.println(sb.toString());
        String fileName="report-of-project-defects-" + project.getId() + "-" + DateHelper.toHtmlDateTime(now).replace(' ','-')+".pdf";
        return getPdf(sb.toString(), "pdf.xsl", fileName);
    }

    private void addProjectHeaderXml(StringBuilder sb, ProjectData project) {
        sb.append("<projectheader><title>");
        sb.append(LocalizedStrings.xml("_reports"));
        sb.append(": ");
        sb.append(xml(project.getDisplayName()));
        sb.append("</title></projectheader>");
    }

    private void addProjectFooterXml(StringBuilder sb, ProjectData project, LocalDateTime now) {
        sb.append("<footer><docAndDate>");
        sb.append(LocalizedStrings.xml("_project")).append(" ").append(xml(project.getDisplayName())).append(" - ").append(DateHelper.toHtmlDateTime(now));
        sb.append("</docAndDate></footer>");
    }

}
