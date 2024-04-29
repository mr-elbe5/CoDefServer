/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.defect;

import de.elbe5.base.BinaryFile;
import de.elbe5.base.LocalizedSystemStrings;
import de.elbe5.base.Log;
import de.elbe5.defectstatus.DefectStatusData;
import de.elbe5.file.CodefPdfCreator;
import de.elbe5.file.FileBean;
import de.elbe5.file.ImageData;
import de.elbe5.request.RequestData;
import de.elbe5.user.UserCache;
import de.elbe5.user.UserData;

import java.time.LocalDateTime;
import java.util.List;

public class DefectPdfCreator extends CodefPdfCreator {
    
    public BinaryFile getDefectPdfFile(DefectData data, RequestData rdata){
        LocalDateTime now= LocalDateTime.now();
        startXml();
        addTopHeader(sxml("_report") + ": " + xml(data.getProject().getDisplayName()) + ", "
                + xml(data.getUnit().getDisplayName()) + ", " + xml(data.getDisplayName()));
        startTable2Col();
        addLabeledContent(sxml("_description"),xml(data.getDescription()));
        addLabeledContent(sxml("_id"),Integer.toString(data.getId()));
        addLabeledContent(sxml("_defectType"),data.isRemainingWork() ? sxml("_remainingWork") : sxml("_defect"));
        UserData user= UserCache.getUser(data.getCreatorId());
        addLabeledContent(sxml("_creator"),xml(user.getName()));
        addLabeledContent(sxml("_creationDate"),xml(data.getCreationDate()));
        addLabeledContent(sxml("_status"),LocalizedSystemStrings.getInstance().xml(data.getLastStatus().toString()));
        addLabeledContent(sxml("_assigned"),xml(data.getLastAssignedName()));
        addLabeledContent(sxml("_dueDate1"),xml(data.getDueDate1()));
        addLabeledContent(sxml("_dueDate2"),xml(data.getDueDate2()));
        addLabeledContent(sxml("_closeDate"),xml(data.getCloseDate()));
        BinaryFile file;
        if (data.hasValidPosition()) {
            ImageData plan = FileBean.getInstance().getFile(data.getPlan().getId(), true, ImageData.class);
            byte[] arrowBytes = FileBean.getInstance().getImageBytes(data.getIconName());
            file = data.createCroppedDefectPlan(plan, arrowBytes, data.getId(), data.getPositionX(), data.getPositionY());
            addLabeledImage( sxml("_defectPosition"), file, "5.0cm");
        }
        addLabeledContent(sxml("_positionComment"),data.getPositionComment());
        List<ImageData> files = data.getFiles(ImageData.class);
        if (!files.isEmpty()) {
            addLabeledContent(sxml("_images"),"");
            for (ImageData image : files) {
                file = FileBean.getInstance().getBinaryFile(image.getId());
                addLabeledImage( image.getDisplayName(), file,"5.0cm");
            }
        }
        endTable2Col();
        for (DefectStatusData statusChange : data.getStatusChanges()){
            addDefectStatusChangeXml( data, statusChange, rdata.getSessionHost());
        }
        addFooter(xml(data.getDisplayName()) + " - " + xml(now));
        finishXml();
        String xml = getXml();
        //Log.log(xml);
        String fileName="report-of-defect-" + data.getId() + "-" + xml(now).replace(' ','-')+".pdf";
        return getPdf(xml, "_templates/pdf.xsl", fileName);
    }

    private void addDefectXml(DefectData data, String host) {

    }

    private void addDefectStatusChangeXml(DefectData defect, DefectStatusData data, String host) {
        addSubHeader(xml(data.geTitle()));
        UserData user= UserCache.getUser(data.getCreatorId());
        startTable2Col();
        addLabeledContent(sxml("_description"),xml(data.getDescription()));
        addLabeledContent( LocalizedSystemStrings.getInstance().xml("_status"),sxml(data.getStatusString()));
        addLabeledContent(sxml("_assigned"),xml(data.getAssignedName()));
        for (ImageData image : data.getFiles(ImageData.class)){
            BinaryFile file = FileBean.getInstance().getBinaryFile(image.getId());
            addLabeledImage( sxml("_image"), file, "5.0cm");
        }
        endTable2Col();
    }

}
