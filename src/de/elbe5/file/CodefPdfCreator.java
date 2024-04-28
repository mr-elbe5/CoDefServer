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

public abstract class CodefPdfCreator extends PdfCreator {

    public void addTopHeader(String text){
        add("<topheader><text>").add(text).add("</text></topheader>");
    }

    public void addSubHeader(String text){
        add("<subheader><text>").add(text).add("</text></subheader>");
    }

    public void addTextLine(String text){
        add("<textline><text>").add(text).add("</text></textline>");
    }

    public void addImage(String src){
        add("<image><src>").add(src).add("</src></image>");
    }

    public void addTableCell(String text){
        add("<tablecell><text>").add(text).add("</text></tablecell>");
    }

    public void addTableCell(int i){
        add("<tablecell><text>").add(String.valueOf(i)).add("</text></tablecell>");
    }

    public void addTableCellBold(String text){
        add("<tablecellbold><text>").add(text).add("</text></tablecellbold>");
    }

    public void addTableCellImage(String src){
        add("<tablecellimage><src>").add(src).add("</src></tablecellimage>");
    }

    public void addTableCellImage(String src, String height){
        add("<tablecellimage><src>").add(src).add("</src><height>")
                .add(height).add("</height></tablecellimage>");
    }

    public void startTableRow(){
        add("<tablerow>");
    }

    public void endTableRow(){
        add("</tablerow>");
    }

    public void startTable2Col(){
        add("<table2col>");
    }

    public void endTable2Col(){
        add("</table2col>");
    }

    public void startTable4Col(){
        add("<table4col>");
    }

    public void endTable4Col(){
        add("</table4col>");
    }

    public void startTable5Col(){
        add("<table5col>");
    }

    public void endTable5Col(){
        add("</table5col>");
    }

    public void addLabeledContent(String label,String content){
        startTableRow();
        addTableCellBold(label);
        addTableCell(content);
        endTableRow();
    }

    public void addLabeledImage(String label,BinaryFile file){
        startTableRow();
        addTableCellBold(label);
        addTableCellImage(getBase64SrcString(file));
        endTableRow();
    }

    public void addLabeledImage(String label,BinaryFile file, String height){
        startTableRow();
        addTableCellBold(label);
        addTableCellImage(getBase64SrcString(file), height);
        endTableRow();
    }

    public void addImage(BinaryFile file, String height){
        add("<image>");
        add("<src>").add(getBase64SrcString(file)).add("</src>");
        add("<height>").add(height).add("</height>");
        add("</image>");
    }

    public void addFooter(String text){
        add("<footer><docAndDate>");
        add(text);
        add("</docAndDate></footer>");
    }

    //todo
    protected void addUnitDefectsXml(UnitData data, List<DefectData> defects, boolean includeStatusChanges) {
        for (DefectData defect : defects){
            addTextLine(sxml(defect.isRemainingWork() ? "_remainingWork" : "_defect") + ": "+ xml(defect.getDescription()));
            startTable4Col();
            startTableRow();
            addTableCellBold(sxml("_id"));
            addTableCell(defect.getId());
            addTableCellBold(sxml("_defectType"));
            addTableCell(sxml(defect.isRemainingWork() ? "_remainingWork" : "_defect"));
            endTableRow();
            UserData user= UserCache.getUser(defect.getCreatorId());
            startTableRow();
            addTableCellBold(sxml("_createdBy"));
            addTableCell(xml(user.getName()));
            addTableCellBold(sxml("_on"));
            addTableCell(html(defect.getCreationDate()));
            endTableRow();
            startTableRow();
            addTableCellBold(sxml("_assigned"));
            addTableCell(xml(defect.getLastAssignedName()));
            addTableCellBold(sxml("_projectPhase"));
            addTableCell(LocalizedSystemStrings.getInstance().xml(defect.getProjectPhaseString()));
            endTableRow();
            startTableRow();
            addTableCellBold(sxml("_dueDate1"));
            addTableCell(html(defect.getDueDate1()));
            addTableCellBold(sxml("_dueDate2"));
            addTableCell(html(defect.getDueDate2()));
            endTableRow();
            startTableRow();
            addTableCellBold(sxml("_status"));
            addTableCell(LocalizedSystemStrings.getInstance().xml(defect.getLastStatus().toString()));
            addTableCellBold(sxml("_closeDate"));
            addTableCell(html(defect.getCloseDate()));
            endTableRow();
            endTable4Col();

            BinaryFile file;
            startTable2Col();
            if (defect.getPositionX()>0 || defect.getPositionY()>0) {
                ImageData plan = FileBean.getInstance().getFile(data.getPlan().getId(), true, ImageData.class);
                byte[] arrowBytes = FileBean.getInstance().getImageBytes(defect.getIconName());
                file = defect.createCroppedDefectPlan(plan, arrowBytes, data.getId(), defect.getPositionX(), defect.getPositionY());
                addLabeledImage(sxml("_defectPosition"), file, "5.0cm");
            }
            if (!defect.getPositionComment().isEmpty()) {
                addLabeledContent(sxml("_positionComment"), xml(defect.getPositionComment()));
            }
            List<ImageData> files = defect.getFiles(ImageData.class);
            if (!files.isEmpty()) {
                addLabeledContent(sxml("_images"), "");
                for (ImageData image : files) {
                    file = FileBean.getInstance().getBinaryFile(image.getId());
                    addLabeledImage("", file, "8.0cm");
                }
            }
            endTable2Col();
            if (includeStatusChanges) {
                startTable4Col();
                for (DefectStatusData changeData : defect.getStatusChanges()) {
                    startTableRow();
                    addTableCellBold(sxml("_statusChange"));
                    endTableRow();
                    startTableRow();
                    addTableCellBold(sxml("_by"));
                    addTableCell(xml(changeData.getCreatorName()));
                    addTableCellBold(sxml("_on"));
                    addTableCell(html(changeData.getCreationDate()));
                    endTableRow();
                    startTableRow();
                    addTableCellBold(sxml("_status"));
                    addTableCell(LocalizedSystemStrings.getInstance().xml(changeData.getStatusString()));
                    addTableCellBold(sxml("_assigned"));
                    addTableCell(xml(changeData.getAssignedName()));
                    endTableRow();
                    startTableRow();
                    addTableCellBold(sxml("_description"));
                    addTableCell(xml(changeData.getDescription()));
                    endTableRow();
                    files = changeData.getFiles(ImageData.class);
                    if (!files.isEmpty()) {
                        startTableRow();
                        addTableCellBold(sxml("_images"));
                        addTableCell(xml(changeData.getDescription()));
                        endTableRow();
                        List<ImageData> statusChangeImages = changeData.getFiles(ImageData.class);
                        for (ImageData image : statusChangeImages) {
                            file = FileBean.getInstance().getBinaryFile(image.getId());
                            addLabeledImage("", file, "8.0cm");
                        }
                    }
                }
                endTable4Col();
            }
        }
    }

}
