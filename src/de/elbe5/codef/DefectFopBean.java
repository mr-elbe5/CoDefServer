/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.codef;

import de.elbe5.base.BinaryFile;
import de.elbe5.base.DateHelper;
import de.elbe5.base.LocalizedStrings;
import de.elbe5.codef.defectstatus.DefectStatusData;
import de.elbe5.codef.defect.DefectData;
import de.elbe5.codef.defect.DefectImageData;
import de.elbe5.codef.unit.UnitData;
import de.elbe5.codef.unit.PlanImageData;
import de.elbe5.file.FileBean;
import de.elbe5.file.PdfCreator;
import de.elbe5.user.UserCache;
import de.elbe5.user.UserData;

import java.util.ArrayList;
import java.util.List;

public abstract class DefectFopBean extends PdfCreator {

    public void addLabeledContent(StringBuilder sb, String label,String content){
        sb.append("<labeledcontent>");
        sb.append("<label>").append(xml(label)).append("</label>");
        sb.append("<content>").append(xml(content)).append("</content>");
        sb.append("</labeledcontent>");
    }

    public void addLabeledImage(StringBuilder sb, String label, BinaryFile file, String height){
        sb.append("<labeledimage>");
        sb.append("<label>").append(xml(label)).append("</label>");
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

    protected void addLocationDefectsXml(StringBuilder sb, UnitData data, List<DefectData> defects, boolean includeComments) {
        for (DefectData defect : defects){
            sb.append("<locationdefect>");
            sb.append("<description>").append(LocalizedStrings.xml("_defect")).append(": ").append(xml(defect.getDescription())).append("</description>");
            sb.append("<defectrow>");
            sb.append("<label1>").append(LocalizedStrings.xml("_id")).append("</label1><content1>").append(defect.getDisplayId()).append("</content1>");
            sb.append("</defectrow>");
            sb.append("<defectrow>");
            UserData user= UserCache.getUser(defect.getCreatorId());
            sb.append("<label1>").append(LocalizedStrings.xml("_createdBy")).append("</label1><content1>").append(xml(user.getName())).append("</content1>");
            sb.append("<label2>").append(LocalizedStrings.xml("_on")).append("</label2><content2>").append(DateHelper.toHtmlDateTime(defect.getCreationDate())).append("</content2>");
            sb.append("</defectrow>");
            sb.append("<defectrow>");
            sb.append("<label1>").append(LocalizedStrings.xml("_assigned")).append("</label1><content1>").append(xml(defect.getAssignedName())).append("</content1>");
            sb.append("<label2>").append(LocalizedStrings.xml("_lot")).append("</label2><content2>").append(xml(defect.getLot())).append("</content2>");
            sb.append("</defectrow>");
            sb.append("<defectrow>");
            sb.append("<label1>").append(LocalizedStrings.xml("_dueDate1")).append("</label1><content1>").append(DateHelper.toHtmlDate(defect.getDueDate1())).append("</content1>");
            sb.append("<label2>").append(LocalizedStrings.xml("_dueDate2")).append("</label2><content2>").append(DateHelper.toHtmlDate(defect.getDueDate2())).append("</content2>");
            sb.append("</defectrow>");
            sb.append("<defectrow>");
            sb.append("<label1>").append(LocalizedStrings.xml("_state")).append("</label1><content1>").append(LocalizedStrings.xml(defect.getState())).append("</content1>");
            sb.append("<label2>").append(LocalizedStrings.xml("_closeDate")).append("</label2><content2>").append(DateHelper.toHtmlDate(defect.getCloseDate())).append("</content2>");
            sb.append("</defectrow>");
            if (!defect.getPositionComment().isEmpty()) {
                sb.append("<defectrow>");
                sb.append("<label1>").append(LocalizedStrings.xml("_positionComment")).append("</label1><content1>").append(xml(defect.getPositionComment())).append("</content1>");
                sb.append("</defectrow>");
            }
            for (DefectImageData image : defect.getFiles(DefectImageData.class)){
                BinaryFile file = FileBean.getInstance().getBinaryFile(image.getId());
                addIndentedImage(sb, file, "8.0cm");
            }
            if (includeComments) {
                List<DefectImageData> files = new ArrayList<>();
                for (DefectStatusData comment : defect.getComments()) {
                    sb.append("<defectrow>");
                    sb.append("<label1>")
                            .append(LocalizedStrings.xml("_comment"))
                            .append("</label1><content1>")
                            .append(xml(comment.getComment()))
                            .append("</content1>");
                    sb.append("<label2>")
                            .append(LocalizedStrings.xml("_by"))
                            .append("</label2><content2>")
                            .append(xml(comment.getCreatorName()))
                            .append("</content2>");
                    sb.append("</defectrow>");
                    sb.append("<defectrow>");
                    sb.append("<label2>")
                            .append(LocalizedStrings.xml("_on"))
                            .append("</label2><content2>")
                            .append(DateHelper.toHtmlDateTime(comment.getCreationDate()))
                            .append("</content2>");
                    sb.append("</defectrow>");
                    List<DefectImageData> defectCommentImages=defect.getFiles(DefectImageData.class);
                    files.clear();
                    for (DefectImageData file : defectCommentImages) {
                        //todo
                        files.add(file);
                    }
                    for (DefectImageData image : files){
                        BinaryFile file = FileBean.getInstance().getBinaryFile(image.getId());
                        addIndentedImage(sb, file, "8.0cm");
                    }
                }
            }
            sb.append("</locationdefect>");
        }
    }

    protected void addLocationPlanXml(StringBuilder sb, UnitData data, PlanImageData plan, BinaryFile file) {
        sb.append("<locationplan>");
        sb.append("<name>").append(xml(plan.getDisplayName())).append("</name>");
        sb.append("<src>").append(getBase64SrcString(file)).append("</src>");
        sb.append("</locationplan>");
    }

}
