/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.file;

import de.elbe5.base.BinaryFile;
import de.elbe5.base.LocalizedSystemStrings;
import de.elbe5.defectstatus.DefectStatusData;
import de.elbe5.defect.DefectData;
import de.elbe5.unit.UnitData;
import de.elbe5.user.UserCache;
import de.elbe5.user.UserData;

import java.util.List;

public abstract class CodefFopBean extends PdfCreator {

    public void addLabeledContent(StringBuilder sb, String label,String content){
        sb.append("<labeledcontent>");
        sb.append("<label>").append(label).append("</label>");
        sb.append("<content>").append(xml(content)).append("</content>");
        sb.append("</labeledcontent>");
    }

    public void addLabeledImage(StringBuilder sb, String label, BinaryFile file, String height){
        sb.append("<labeledimage>");
        sb.append("<label>").append(label).append("</label>");
        sb.append("<src>").append(getBase64SrcString(file)).append("</src>");
        sb.append("<height>").append(height).append("</height>");
        sb.append("</labeledimage>");
    }

    public void addIndentedImage(StringBuilder sb, BinaryFile file, String height){
        sb.append("<labeledimage>");
        sb.append("<label></label>");
        sb.append("<src>").append(getBase64SrcString(file)).append("</src>");
        sb.append("<height>").append(height).append("</height>");
        sb.append("</labeledimage>");
    }

    public void addImage(StringBuilder sb, BinaryFile file, String height){
        sb.append("<image>");
        sb.append("<src>").append(getBase64SrcString(file)).append("</src>");
        sb.append("<height>").append(height).append("</height>");
        sb.append("</image>");
    }

    protected void addUnitDefectsXml(StringBuilder sb, UnitData data, List<DefectData> defects, boolean includeStatusChanges) {
        for (DefectData defect : defects){
            sb.append("<unitdefect>");
            sb.append("<description>").append(sxml(defect.isRemainingWork() ? "_remainingWork" : "_defect")).append(": ").append(xml(defect.getDescription())).append("</description>");
            sb.append("<defectrow>");
            sb.append("<label1>").append(sxml("_id")).append("</label1><content1>").append(defect.getId()).append("</content1>");
            sb.append("<label2>").append(sxml("_defectType")).append("</label2><content2>").append(sxml(defect.isRemainingWork() ? "_remainingWork" : "_defect")).append("</content2>");
            sb.append("</defectrow>");
            sb.append("<defectrow>");
            UserData user= UserCache.getUser(defect.getCreatorId());
            sb.append("<label1>").append(sxml("_createdBy")).append("</label1><content1>").append(xml(user.getName())).append("</content1>");
            sb.append("<label2>").append(sxml("_on")).append("</label2><content2>").append(html(defect.getCreationDate())).append("</content2>");
            sb.append("</defectrow>");
            sb.append("<defectrow>");
            sb.append("<label1>").append(sxml("_assigned")).append("</label1><content1>").append(xml(defect.getLastAssignedName())).append("</content1>");
            sb.append("<label2>").append(sxml("_projectPhase")).append("</label2><content2>").append(LocalizedSystemStrings.getInstance().xml(defect.getProjectPhaseString())).append("</content2>");
            sb.append("</defectrow>");
            sb.append("<defectrow>");
            sb.append("<label1>").append(sxml("_dueDate1")).append("</label1><content1>").append(html(defect.getDueDate1())).append("</content1>");
            sb.append("<label2>").append(sxml("_dueDate2")).append("</label2><content2>").append(html(defect.getDueDate2())).append("</content2>");
            sb.append("</defectrow>");
            sb.append("<defectrow>");
            sb.append("<label1>").append(sxml("_status")).append("</label1><content1>").append(LocalizedSystemStrings.getInstance().xml(defect.getLastStatus().toString())).append("</content1>");
            sb.append("<label2>").append(sxml("_closeDate")).append("</label2><content2>").append(html(defect.getCloseDate())).append("</content2>");
            sb.append("</defectrow>");
            BinaryFile file;
            if (defect.getPositionX()>0 || defect.getPositionY()>0) {
                ImageData plan = FileBean.getInstance().getFile(data.getPlan().getId(), true, ImageData.class);
                byte[] arrowBytes = FileBean.getInstance().getImageBytes(defect.getIconName());
                file = defect.createCroppedDefectPlan(plan, arrowBytes, data.getId(), defect.getPositionX(), defect.getPositionY());
                addLabeledImage(sb, sxml("_defectPosition"), file, "5.0cm");
            }
            if (!defect.getPositionComment().isEmpty()) {
                sb.append("<defectrow>");
                sb.append("<label1>").append(sxml("_positionComment")).append("</label1><content1>").append(xml(defect.getPositionComment())).append("</content1>");
                sb.append("</defectrow>");
            }
            List<ImageData> files = defect.getFiles(ImageData.class);
            if (!files.isEmpty()) {
                sb.append("<defectrow>");
                sb.append("<label1>")
                        .append(sxml("_images"))
                        .append("</label1>");
                sb.append("</defectrow>");
                for (ImageData image : files) {
                    file = FileBean.getInstance().getBinaryFile(image.getId());
                    addIndentedImage(sb, file, "8.0cm");
                }
            }
            if (includeStatusChanges) {
                for (DefectStatusData changeData : defect.getStatusChanges()) {
                    sb.append("<defectrow>");
                    sb.append("<label1>")
                            .append(sxml("_statusChange"))
                            .append("</label1>");
                    sb.append("</defectrow>");
                    sb.append("<defectrow>");
                    sb.append("<label1>")
                            .append(sxml("_by"))
                            .append("</label1><content1>")
                            .append(xml(changeData.getCreatorName()))
                            .append("</content1>");
                    sb.append("<label2>")
                            .append(sxml("_on"))
                            .append("</label2><content2>")
                            .append(html(changeData.getCreationDate()))
                            .append("</content2>");
                    sb.append("</defectrow>");
                    sb.append("<defectrow>");
                    sb.append("<label1>")
                            .append(sxml("_status"))
                            .append("</label1><content1>")
                            .append(LocalizedSystemStrings.getInstance().xml(changeData.getStatusString()))
                            .append("</content1>");
                    sb.append("<label2>")
                            .append(sxml("_assigned"))
                            .append("</label2><content2>")
                            .append(xml(changeData.getAssignedName()))
                            .append("</content2>");
                    sb.append("</defectrow>");
                    sb.append("<defectrow>");
                    sb.append("<label1>")
                            .append(sxml("_description"))
                            .append("</label1><content1>")
                            .append(xml(changeData.getDescription()))
                            .append("</content1>");
                    sb.append("</defectrow>");
                    files = changeData.getFiles(ImageData.class);
                    if (!files.isEmpty()) {
                        sb.append("<defectrow>");
                        sb.append("<label1>")
                                .append(sxml("_images"))
                                .append("</label1>");
                        sb.append("</defectrow>");
                        List<ImageData> statusChangeImages = changeData.getFiles(ImageData.class);
                        for (ImageData image : statusChangeImages) {
                            file = FileBean.getInstance().getBinaryFile(image.getId());
                            addIndentedImage(sb, file, "8.0cm");
                        }
                    }
                }
            }
            sb.append("</unitdefect>");
        }
    }

    protected void addUnitPlanXml(StringBuilder sb, UnitData data, ImageData plan, BinaryFile file) {
        sb.append("<unitplan>");
        sb.append("<name>").append(xml(plan.getDisplayName())).append("</name>");
        sb.append("<src>").append(getBase64SrcString(file)).append("</src>");
        sb.append("</unitplan>");
    }

}
