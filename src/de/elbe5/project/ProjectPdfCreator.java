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
import de.elbe5.base.Log;
import de.elbe5.file.ImageData;
import de.elbe5.content.ContentCache;
import de.elbe5.file.CodefPdfCreator;
import de.elbe5.defect.DefectData;
import de.elbe5.unit.UnitData;
import de.elbe5.file.ImageBean;
import de.elbe5.request.RequestData;
import de.elbe5.user.CodefUserData;

import java.time.LocalDateTime;
import java.util.List;

public class ProjectPdfCreator extends CodefPdfCreator {

    public BinaryFile getProjectReport(int projectId, RequestData rdata, boolean includeStatusChanges){
        CodefUserData user = rdata.getLoginUser(CodefUserData.class);
        if (user==null)
            return null;
        LocalDateTime now = LocalDateTime.now();
        ProjectData project= ContentCache.getContent(projectId,ProjectData.class);
        if (project==null)
            return null;
        startXml();
        addTopHeader(sxml("_reports") + ": " + xml(project.getDisplayName()));
        for (UnitData unit : project.getChildren(UnitData.class)) {
            List<DefectData> defects = user.getUnitDefects(unit.getId());
            if (!defects.isEmpty()) {
                addSubHeader(sxml("_unit") + ": " + xml(unit.getDisplayName()));
                startTable2Col();
                addLabeledContent(unit.getApproveDate()==null ? "" : sxml("_approveDate"), xml(unit.getApproveDate()));
                endTable2Col();
                ImageData plan = unit.getPlan();
                if (plan != null) {
                    ImageData fullplan = ImageBean.getInstance().getFile(plan.getId(), true, ImageData.class);
                    defects = user.getUnitDefects(unit.getId());
                    BinaryFile file = unit.createUnitDefectPlan(fullplan, defects, 1);
                    addImage(getBase64SrcString(file));
                }
                addUnitDefectsXml(unit, defects, includeStatusChanges);
            }
        }
        addFooter(sxml("_project") + " " + xml(project.getDisplayName()) + " - " + (xml(now)));
        finishXml();
        String xml = getXml();
        //Log.log(xml);
        String fileName="report-of-project-defects-" + project.getId() + "-" + xml(now).replace(' ','-')+".pdf";
        return getPdf(xml, "_templates/pdf.xsl", fileName);
    }

}
