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
import de.elbe5.base.LocalizedStrings;
import de.elbe5.defectstatuschange.DefectStatusChangeData;
import de.elbe5.file.DefectFopBean;
import de.elbe5.file.FileBean;
import de.elbe5.file.ImageData;
import de.elbe5.request.RequestData;
import de.elbe5.user.UserCache;
import de.elbe5.user.UserData;

import java.time.LocalDateTime;

public class DefectPdfBean extends DefectFopBean {

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
        for (DefectStatusChangeData statusChange : data.getStatusChanges()){
            addDefectStatusChangeXml(sb, data, statusChange, rdata.getSessionHost());
        }
        addDefectFooterXml(sb,data,now);
        sb.append("</root>");
        //System.out.println(sb.toString());
        String fileName="report-of-defect-" + data.getDisplayId() + "-" + DateHelper.toHtmlDateTime(now).replace(' ','-')+".pdf";
        return getPdf(sb.toString(), "_templates/pdf.xsl", fileName);
    }

    private void addDefectHeaderXml(StringBuilder sb, DefectData data) {
        sb.append("<defectheader><title>");
        sb.append(LocalizedStrings.xml("_report"));
        sb.append(": ");
        sb.append(LocalizedStrings.xml(data.getProject().getName()));
        sb.append(", ");
        sb.append(LocalizedStrings.xml(data.getUnit().getName()));
        sb.append(", ");
        sb.append(LocalizedStrings.xml(data.getDisplayName()));
        sb.append("</title></defectheader>");
    }

    private void addDefectFooterXml(StringBuilder sb, DefectData data, LocalDateTime now) {
        sb.append("<footer><docAndDate>")
                .append(LocalizedStrings.xml("_defect"))
                .append(" ").append(LocalizedStrings.xml(data.getDisplayName()))
                .append(" - ");
        sb.append(LocalizedStrings.xml(DateHelper.toHtmlDateTime(now)));
        sb.append("</docAndDate></footer>");
    }

    private void addDefectXml(StringBuilder sb, DefectData data, String host) {
        sb.append("<defect>");
        addLabeledContent(sb,LocalizedStrings.string("_description"),data.getDescription());
        addLabeledContent(sb,LocalizedStrings.string("_id"),Integer.toString(data.getDisplayId()));
        UserData user= UserCache.getUser(data.getCreatorId());
        addLabeledContent(sb,LocalizedStrings.string("_creator"),user.getName());
        addLabeledContent(sb,LocalizedStrings.string("_creationDate"),DateHelper.toHtmlDateTime(data.getCreationDate()));
        addLabeledContent(sb,LocalizedStrings.string("_state"),LocalizedStrings.string(data.getStatus().toString()));
        addLabeledContent(sb,LocalizedStrings.string("_assigned"),data.getAssignedName());
        addLabeledContent(sb,LocalizedStrings.string("_lot"),data.getLot());
        addLabeledContent(sb,LocalizedStrings.string("_dueDate1"),DateHelper.toHtmlDate(data.getDueDate1()));
        addLabeledContent(sb,LocalizedStrings.string("_dueDate2"),DateHelper.toHtmlDate(data.getDueDate2()));
        addLabeledContent(sb,LocalizedStrings.string("_closeDate"),DateHelper.toHtmlDate(data.getCloseDate()));
        ImageData plan = FileBean.getInstance().getFile(data.getPlan().getId(),true,ImageData.class);
        byte[] arrowBytes = FileBean.getInstance().getImageBytes("redarrow.png");
        BinaryFile file = data.createCroppedDefectPlan(plan, arrowBytes, data.getId(), data.getPositionX(), data.getPositionY());
        addLabeledImage(sb,LocalizedStrings.string("_position"), file,"5.0cm");
        addLabeledContent(sb,LocalizedStrings.string("_positionComment"),data.getPositionComment());
        for (ImageData image : data.getFiles(ImageData.class)){
            file = FileBean.getInstance().getBinaryFile(image.getId());
            addLabeledImage(sb,LocalizedStrings.string("_image"),file,"5.0cm");
        }
        sb.append("</defect>");
    }

    private void addDefectStatusChangeXml(StringBuilder sb, DefectData defect, DefectStatusChangeData data, String host) {
        sb.append("<statuschange>");
        sb.append("<title>").append(LocalizedStrings.xml(data.geTitle())).append("</title>");
        UserData user= UserCache.getUser(data.getCreatorId());
        addLabeledContent(sb,LocalizedStrings.string("_description"),data.getDescription());
        for (ImageData image : data.getFiles(ImageData.class)){
            BinaryFile file = FileBean.getInstance().getBinaryFile(image.getId());
            addLabeledImage(sb, LocalizedStrings.string("_image"), file, "5.0cm");
        }
        sb.append("</statuschange>");
    }

}
