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
import de.elbe5.defectstatus.DefectStatusData;
import de.elbe5.file.CodefFopBean;
import de.elbe5.file.FileBean;
import de.elbe5.file.ImageData;
import de.elbe5.request.RequestData;
import de.elbe5.user.UserCache;
import de.elbe5.user.UserData;

import java.time.LocalDateTime;
import java.util.List;

public class DefectPdfBean extends CodefFopBean {

    private static DefectPdfBean instance = null;

    public static DefectPdfBean getInstance() {
        if (instance == null) {
            instance = new DefectPdfBean();
        }
        return instance;
    }

    // defect

    public BinaryFile getDefectPdfFile(DefectData data, RequestData rdata){
        LocalDateTime now= LocalDateTime.now();
        StringBuilder sb=new StringBuilder();
        sb.append("<root>");
        addDefectHeaderXml(sb,data);
        addDefectXml(sb,data,rdata.getSessionHost());
        for (DefectStatusData statusChange : data.getStatusChanges()){
            addDefectStatusChangeXml(sb, data, statusChange, rdata.getSessionHost());
        }
        addDefectFooterXml(sb,data,now);
        sb.append("</root>");
        //System.out.println(sb.toString());
        String fileName="report-of-defect-" + data.getId() + "-" + html(now).replace(' ','-')+".pdf";
        return getPdf(sb.toString(), "_templates/pdf.xsl", fileName);
    }

    private void addDefectHeaderXml(StringBuilder sb, DefectData data) {
        sb.append("<defectheader><title>");
        sb.append(sxml("_report"));
        sb.append(": ");
        sb.append(xml(data.getProject().getDisplayName()));
        sb.append(", ");
        sb.append(xml(data.getUnit().getDisplayName()));
        sb.append(", ");
        sb.append(xml(data.getDisplayName()));
        sb.append("</title></defectheader>");
    }

    private void addDefectFooterXml(StringBuilder sb, DefectData data, LocalDateTime now) {
        sb.append("<footer><docAndDate>")
                .append(" ").append(xml(data.getDisplayName()))
                .append(" - ");
        sb.append(html(now));
        sb.append("</docAndDate></footer>");
    }

    private void addDefectXml(StringBuilder sb, DefectData data, String host) {
        sb.append("<defect>");
        addLabeledContent(sb,sxml("_description"),data.getDescription());
        addLabeledContent(sb,sxml("_id"),Integer.toString(data.getId()));
        addLabeledContent(sb,sxml("_defectType"),data.isRemainingWork() ? sxml("_remainingWork") : sxml("_defect"));
        UserData user= UserCache.getUser(data.getCreatorId());
        addLabeledContent(sb,sxml("_creator"),user.getName());
        addLabeledContent(sb,sxml("_creationDate"),html(data.getCreationDate()));
        addLabeledContent(sb,sxml("_status"),LocalizedSystemStrings.getInstance().xml(data.getLastStatus().toString()));
        addLabeledContent(sb,sxml("_assigned"),data.getLastAssignedName());
        addLabeledContent(sb,sxml("_dueDate1"),html(data.getDueDate1()));
        addLabeledContent(sb,sxml("_dueDate2"),html(data.getDueDate2()));
        addLabeledContent(sb,sxml("_closeDate"),html(data.getCloseDate()));
        BinaryFile file;
        if (data.hasValidPosition()) {
            ImageData plan = FileBean.getInstance().getFile(data.getPlan().getId(), true, ImageData.class);
            byte[] arrowBytes = FileBean.getInstance().getImageBytes(data.getIconName());
            file = data.createCroppedDefectPlan(plan, arrowBytes, data.getId(), data.getPositionX(), data.getPositionY());
            addLabeledImage(sb, sxml("_defectPosition"), file, "5.0cm");
        }
        addLabeledContent(sb,sxml("_positionComment"),data.getPositionComment());
        List<ImageData> files = data.getFiles(ImageData.class);
        if (!files.isEmpty()) {
            addLabeledContent(sb,sxml("_images"),"");
            for (ImageData image : files) {
                file = FileBean.getInstance().getBinaryFile(image.getId());
                addLabeledImage(sb, image.getDisplayName(), file,"5.0cm");
            }
        }
        sb.append("</defect>");
    }

    private void addDefectStatusChangeXml(StringBuilder sb, DefectData defect, DefectStatusData data, String host) {
        sb.append("<statuschange>");
        sb.append("<title>").append(xml(data.geTitle())).append("</title>");
        UserData user= UserCache.getUser(data.getCreatorId());
        addLabeledContent(sb,sxml("_description"),data.getDescription());
        addLabeledContent(sb, LocalizedSystemStrings.getInstance().xml("_status"),sxml(data.getStatusString()));
        addLabeledContent(sb,sxml("_assigned"),xml(data.getAssignedName()));
        for (ImageData image : data.getFiles(ImageData.class)){
            BinaryFile file = FileBean.getInstance().getBinaryFile(image.getId());
            addLabeledImage(sb, sxml("_image"), file, "5.0cm");
        }
        sb.append("</statuschange>");
    }

}
