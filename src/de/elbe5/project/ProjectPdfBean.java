/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.project;

import de.elbe5.base.BinaryFile;
import de.elbe5.base.DateHelper;
import de.elbe5.file.ImageData;
import de.elbe5.unit.UnitBean;
import de.elbe5.content.ContentCache;
import de.elbe5.file.CodefFopBean;
import de.elbe5.defect.DefectData;
import de.elbe5.unit.UnitData;
import de.elbe5.file.ImageBean;
import de.elbe5.request.RequestData;
import de.elbe5.user.CodefUserData;

import java.time.LocalDateTime;
import java.util.List;

public class ProjectPdfBean extends CodefFopBean {

    private static ProjectPdfBean instance = null;

    public static ProjectPdfBean getInstance() {
        if (instance == null) {
            instance = new ProjectPdfBean();
        }
        return instance;
    }

    public BinaryFile getProjectReport(int projectId, RequestData rdata, boolean includeStatusChanges){
        CodefUserData user = rdata.getLoginUser(CodefUserData.class);
        if (user==null)
            return null;
        LocalDateTime now = LocalDateTime.now();
        ProjectData project= ContentCache.getContent(projectId,ProjectData.class);
        if (project==null)
            return null;
        StringBuilder sb=new StringBuilder();
        sb.append("<root>");
        addProjectHeaderXml(sb,project);
        for (UnitData unit : project.getChildren(UnitData.class)) {
            List<DefectData> defects = user.getUnitDefects(unit.getId());
            if (!defects.isEmpty()) {
                sb.append("<unit>");
                sb.append("<unitheader><title>");
                sb.append(sxml("_unit"));
                sb.append(": ");
                sb.append(xml(unit.getDisplayName()));
                sb.append("</title></unitheader>");
                addLabeledContent(sb,sxml("_approveDate"),html(unit.getApproveDate()));
                ImageData plan = unit.getPlan();
                if (plan != null) {
                    ImageData fullplan = ImageBean.getInstance().getFile(plan.getId(), true, ImageData.class);
                    defects = user.getUnitDefects(unit.getId());
                    BinaryFile file = unit.createUnitDefectPlan(fullplan, defects, 1);
                    addUnitPlanXml(sb, unit, plan, file);
                }

                addUnitDefectsXml(sb, unit, defects, includeStatusChanges);
                sb.append("</unit>");
            }
        }
        addProjectFooterXml(sb,project,now);
        sb.append("</root>");
        //System.out.println(sb.toString());
        String fileName="report-of-project-defects-" + project.getId() + "-" + DateHelper.toHtml(now).replace(' ','-')+".pdf";
        return getPdf(sb.toString(), "_templates/pdf.xsl", fileName);
    }

    private void addProjectHeaderXml(StringBuilder sb, ProjectData project) {
        sb.append("<projectheader><title>");
        sb.append(sxml("_reports"));
        sb.append(": ");
        sb.append(xml(project.getDisplayName()));
        sb.append("</title></projectheader>");
    }

    private void addProjectFooterXml(StringBuilder sb, ProjectData project, LocalDateTime now) {
        sb.append("<footer><docAndDate>");
        sb.append(sxml("_project")).append(" ").append(xml(project.getDisplayName())).append(" - ").append(DateHelper.toHtml(now));
        sb.append("</docAndDate></footer>");
    }

}
