/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.defect;

import de.elbe5.base.BinaryFile;
import de.elbe5.base.DateHelper;
import de.elbe5.base.LocalizedSystemStrings;
import de.elbe5.content.ContentCache;
import de.elbe5.file.CodefPdfCreator;
import de.elbe5.file.FileBean;
import de.elbe5.file.ImageData;
import de.elbe5.request.RequestData;
import de.elbe5.unit.UnitData;
import de.elbe5.user.UserCache;
import de.elbe5.user.UserData;

import java.time.LocalDateTime;
import java.util.List;

public class DefectPdfCreator extends CodefPdfCreator {
    
    public BinaryFile getDefectPdfFile(DefectData defect, RequestData rdata){
        LocalDateTime now = DateHelper.getCurrentTime();
        UnitData unit= ContentCache.getContent(defect.getParentId(),UnitData.class);
        if (unit==null || unit.getProject()==null)
            return null;
        startXml();

        addTopHeader(sxml("_defect") + ": " + xml(defect.getDisplayName()));
        addHeaderComment(sxml("_project") +", " + sxml("_unit") + ": "
                + xml(unit.getProject().getDisplayName()) + ", " + xml(unit.getDisplayName()));

        startTable2Col();
        addLabeledContent(sxml("_defect"),xml(defect.getDescription()));
        addLabeledContent(sxml("_id"),Integer.toString(defect.getId()));
        addLabeledContent(sxml("_commentOrDescription"),xml(defect.getComment()));
        addLabeledContent(sxml("_unitOrLocation"),xml(defect.getLocation()));
        addLabeledContent(sxml("_defectType"),defect.isRemainingWork() ? sxml("_remainingWork") : sxml("_defect"));
        UserData user= UserCache.getUser(defect.getCreatorId());
        addLabeledContent(sxml("_creator"),xml(user.getName()));
        addLabeledContent(sxml("_creationDate"),xml(defect.getCreationDate()));
        addLabeledContent(sxml("_status"),LocalizedSystemStrings.getInstance().xml(defect.getLastStatus().toString()));
        addLabeledContent(sxml("_assigned"),xml(defect.getLastAssignedName()));
        addLabeledContent(sxml("_dueDate1"),xml(defect.getDueDate1()));
        addLabeledContent(sxml("_dueDate2"),xml(defect.getDueDate2()));
        addLabeledContent(sxml("_closeDate"),xml(defect.getCloseDate()));

        BinaryFile file;
        if (defect.hasValidPosition()) {
            ImageData plan = FileBean.getInstance().getFile(defect.getPlan().getId(), true, ImageData.class);
            byte[] arrowBytes = FileBean.getInstance().getImageBytes(defect.getIconName());
            file = defect.createCroppedDefectPlan(plan, arrowBytes, defect.getId(), defect.getPositionX(), defect.getPositionY());
            addLabeledImage( sxml("_defectPosition"), file, "5.0cm");
        }
        List<ImageData> files = defect.getFiles(ImageData.class);
        if (!files.isEmpty()) {
            addLabeledContent(sxml("_images"),"");
            for (ImageData image : files) {
                file = FileBean.getInstance().getBinaryFile(image.getId());
                addLabeledImage( image.getDisplayName(), file,"5.0cm");
            }
        }
        endTable2Col();

        addStatusList(defect);

        addFooter(xml(defect.getDisplayName()) + " - " + xml(now));

        finishXml();
        String xml = getXml();
        //Log.log(xml);
        String fileName="report-of-defect-" + defect.getId() + "-" + xml(now).replace(' ','-')+".pdf";
        return getPdf(xml, "_templates/pdf.xsl", fileName);
    }

}
