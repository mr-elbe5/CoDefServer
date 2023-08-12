/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.codef.unit;

import de.elbe5.base.BinaryFile;
import de.elbe5.base.DateHelper;
import de.elbe5.base.LocalizedStrings;
import de.elbe5.codef.DefectFopBean;
import de.elbe5.codef.ViewFilter;
import de.elbe5.codef.defect.DefectData;
import de.elbe5.codef.project.ProjectData;
import de.elbe5.content.ContentCache;
import de.elbe5.file.ImageBean;
import de.elbe5.request.RequestData;

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

    public BinaryFile getLocationReport(int locationId, RequestData rdata, boolean includeComments){
        LocalDateTime now= LocalDateTime.now();
        UnitData location= ContentCache.getContent(locationId,UnitData.class);
        if (location==null)
            return null;
        StringBuilder sb=new StringBuilder();
        sb.append("<root>");
        addLocationHeaderXml(sb,location);
        sb.append("<location>");
        List<DefectData> defects = ViewFilter.getFilter(rdata).getLocationDefects(location.getId());
        addLocationDefectsXml(sb,location, defects, includeComments);
        PlanImageData plan = location.getPlan();
        if (plan!=null) {
            PlanImageData fullplan = ImageBean.getInstance().getFile(plan.getId(), true, PlanImageData.class);
            byte[] arrowBytes = UnitBean.getInstance().getImageBytes("redarrow.png");
            defects = ViewFilter.getFilter(rdata).getLocationDefects(location.getId());
            BinaryFile file = fullplan.createLocationDefectPlan(arrowBytes, defects, 1);
            addLocationPlanXml(sb, location, plan, file);
        }
        sb.append("</location>");
        addLocationFooterXml(sb,location,now);
        sb.append("</root>");
        //System.out.println(sb.toString());
        String fileName="report-of-location-defects-" + location.getId() + "-" + DateHelper.toHtmlDateTime(now).replace(' ','-')+".pdf";
        return getPdf(sb.toString(), "pdf.xsl", fileName);
    }

    private void addLocationHeaderXml(StringBuilder sb, UnitData location) {
        ProjectData project=ContentCache.getContent(location.getProjectId(),ProjectData.class);
        assert(project!=null);
        sb.append("<locationheader><title>");
        sb.append(LocalizedStrings.xml("_reports"));
        sb.append(": ");
        sb.append(xml(project.getDisplayName()));
        sb.append(", ");
        sb.append(xml(location.getDisplayName()));
        sb.append("</title></locationheader>");
    }

    private void addLocationFooterXml(StringBuilder sb, UnitData location, LocalDateTime now) {
        ProjectData project=ContentCache.getContent(location.getProjectId(),ProjectData.class);
        assert(project!=null);
        sb.append("<footer><docAndDate>");
        sb.append(LocalizedStrings.xml("_project"))
                .append(" ")
                .append(xml(project.getDisplayName()))
                .append(", ").append(LocalizedStrings.xml("_location"))
                .append(" ").append(xml(location.getDisplayName()))
                .append(" - ")
                .append(DateHelper.toHtmlDateTime(now));
        sb.append("</docAndDate></footer>");
    }

}
