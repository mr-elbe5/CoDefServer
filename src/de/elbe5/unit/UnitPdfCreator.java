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
import de.elbe5.file.CodefPdfCreator;
import de.elbe5.defect.DefectData;
import de.elbe5.file.ImageData;
import de.elbe5.project.ProjectData;
import de.elbe5.content.ContentCache;
import de.elbe5.file.ImageBean;
import de.elbe5.request.RequestData;
import de.elbe5.user.CodefUserData;

import java.time.LocalDateTime;
import java.util.List;

public class UnitPdfCreator extends CodefPdfCreator {

    public BinaryFile getUnitReport(int unitId, RequestData rdata, boolean includeStatusChanges){
        CodefUserData user = rdata.getLoginUser(CodefUserData.class);
        if (user==null)
            return null;
        LocalDateTime now= LocalDateTime.now();
        UnitData unit= ContentCache.getContent(unitId,UnitData.class);
        if (unit==null)
            return null;
        ProjectData project=unit.getProject();
        assert(project!=null);
        StringBuilder sb=new StringBuilder();
        addTopHeader(sxml("_reports") + ": " + xml(project.getDisplayName()) + ", "
                + xml(unit.getDisplayName()));
        addLabeledContent(unit.getApproveDate()==null ? "" : sxml("_approveDate"), html(unit.getApproveDate()));
        List<DefectData> defects = user.getUnitDefects(unit.getId());
        ImageData plan = unit.getPlan();
        if (plan!=null) {
            ImageData fullplan = ImageBean.getInstance().getFile(plan.getId(), true, ImageData.class);
            defects = user.getUnitDefects(unit.getId());
            BinaryFile file = unit.createUnitDefectPlan(fullplan,defects, 1);
            addUnitPlanXml(sb, unit, plan, file);
        }
        addUnitDefectsXml(unit, defects, includeStatusChanges);
        addFooter(sxml("_project") + " " + xml(project.getDisplayName()) +
                ", " + sxml("_unit") + " " + xml(unit.getDisplayName()) +
                " - " + DateHelper.toHtml(now));
        String fileName="report-of-unit-defects-" + unit.getId() + "-" + html(now).replace(' ','-')+".pdf";
        return getPdf(finishXml(), "_templates/pdf.xsl", fileName);
    }

}
