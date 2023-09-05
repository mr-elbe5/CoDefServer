/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.unit;

import de.elbe5.base.BinaryFile;
import de.elbe5.base.DateHelper;
import de.elbe5.base.LocalizedStrings;
import de.elbe5.file.DefectFopBean;
import de.elbe5.defect.DefectData;
import de.elbe5.file.ImageData;
import de.elbe5.project.ProjectData;
import de.elbe5.content.ContentCache;
import de.elbe5.file.ImageBean;
import de.elbe5.request.RequestData;
import de.elbe5.user.CodefUserData;

import java.time.LocalDateTime;
import java.util.List;

public class UnitPdfBean extends DefectFopBean {

    private static UnitPdfBean instance = null;

    public static UnitPdfBean getInstance() {
        if (instance == null) {
            instance = new UnitPdfBean();
        }
        return instance;
    }

    public BinaryFile getUnitReport(int unitId, RequestData rdata, boolean includeStatusChanges){
        CodefUserData user = rdata.getLoginUser(CodefUserData.class);
        if (user==null)
            return null;
        LocalDateTime now= LocalDateTime.now();
        UnitData unit= ContentCache.getContent(unitId,UnitData.class);
        if (unit==null)
            return null;
        StringBuilder sb=new StringBuilder();
        sb.append("<root>");
        addUnitHeaderXml(sb,unit);
        sb.append("<unit>");
        addLabeledContent(sb,LocalizedStrings.string("_approveDate"),DateHelper.toHtmlDate(unit.getApproveDate()));
        List<DefectData> defects = user.getUnitDefects(unit.getId());
        ImageData plan = unit.getPlan();
        if (plan!=null) {
            ImageData fullplan = ImageBean.getInstance().getFile(plan.getId(), true, ImageData.class);
            byte[] arrowBytes = UnitBean.getInstance().getImageBytes("redarrow.png");
            defects = user.getUnitDefects(unit.getId());
            BinaryFile file = unit.createUnitDefectPlan(fullplan,arrowBytes, defects, 1);
            addUnitPlanXml(sb, unit, plan, file);
        }
        addUnitDefectsXml(sb,unit, defects, includeStatusChanges);
        sb.append("</unit>");
        addUnitFooterXml(sb,unit,now);
        sb.append("</root>");
        //System.out.println(sb.toString());
        String fileName="report-of-unit-defects-" + unit.getId() + "-" + DateHelper.toHtmlDateTime(now).replace(' ','-')+".pdf";
        return getPdf(sb.toString(), "_templates/pdf.xsl", fileName);
    }

    private void addUnitHeaderXml(StringBuilder sb, UnitData unit) {
        ProjectData project=unit.getProject();
        assert(project!=null);
        sb.append("<unitheader><title>");
        sb.append(LocalizedStrings.xml("_reports"));
        sb.append(": ");
        sb.append(xml(project.getDisplayName()));
        sb.append(", ");
        sb.append(xml(unit.getDisplayName()));
        sb.append("</title></unitheader>");
    }

    private void addUnitFooterXml(StringBuilder sb, UnitData unit, LocalDateTime now) {
        ProjectData project=unit.getProject();
        assert(project!=null);
        sb.append("<footer><docAndDate>");
        sb.append(LocalizedStrings.xml("_project"))
                .append(" ")
                .append(xml(project.getDisplayName()))
                .append(", ").append(LocalizedStrings.xml("_unit"))
                .append(" ").append(xml(unit.getDisplayName()))
                .append(" - ")
                .append(DateHelper.toHtmlDateTime(now));
        sb.append("</docAndDate></footer>");
    }

}
