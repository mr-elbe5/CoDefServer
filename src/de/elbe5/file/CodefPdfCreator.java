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
import de.elbe5.user.CodefUserData;
import de.elbe5.user.UserCache;
import de.elbe5.user.UserData;

import java.util.List;

public abstract class CodefPdfCreator extends PdfCreator {

    public static int maxImageSize = 1024;

    public void addTopHeader(String text){
        add("<topheader><text>").add(text).add("</text></topheader>");
    }

    public void addSubHeader(String text){
        add("<subheader><text>").add(text).add("</text></subheader>");
    }

    public void addHeaderComment(String text){
        add("<headercomment><text>").add(text).add("</text></headercomment>");
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

    public void startDefect(){
        add("<defect>");
    }

    public void endDefect(){
        add("</defect>");
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

    public void startTable3Col(){
        add("<table3col>");
    }

    public void endTable3Col(){
        add("</table3col>");
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

    protected void addUnit(UnitData unit, CodefUserData user, boolean includeStatusChanges){
        List<DefectData> defects = user.getUnitDefects(unit.getId());
        if (!defects.isEmpty()) {
            addSubHeader(sxml("_unit") + ": " + xml(unit.getDisplayName()));

            if (unit.getApproveDate()!=null) {
                addTextLine(sxml("_approveDate") + ": " + xml(unit.getApproveDate()));
            }

            ImageData plan = unit.getPlan();
            if (plan != null) {
                ImageData fullplan = ImageBean.getInstance().getFile(plan.getId(), true, ImageData.class);
                defects = user.getUnitDefects(unit.getId());
                BinaryFile file = unit.createUnitDefectPlan(fullplan, defects, 1);
                addImage(getBase64SrcString(file));
            }
            addDefectList(unit, defects, includeStatusChanges);
        }
    }

    protected void addDefectList(UnitData unit, List<DefectData> defects, boolean includeStatusChanges) {
        for (DefectData defect : defects){
            startDefect();
            addTextLine(xml(defect.getDisplayName()));
            startTable2Col();
            startTableRow();
            addTableCellBold(sxml("_defect"));
            addTableCell(defect.getDescription());
            endTableRow();
            if (!defect.getComment().isEmpty()) {
                startTableRow();
                addTableCellBold(sxml("_commentOrDescription"));
                addTableCell(defect.getComment());
                endTableRow();
            }
            if (!defect.getLocation().isEmpty()) {
                startTableRow();
                addTableCellBold(sxml("_unitOrLocation"));
                addTableCell(defect.getLocation());
                endTableRow();
            }
            endTable2Col();
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
            addTableCell(xml(defect.getCreationDate()));
            endTableRow();
            startTableRow();
            addTableCellBold(sxml("_assigned"));
            addTableCell(xml(defect.getLastAssignedName()));
            addTableCellBold(sxml("_projectPhase"));
            addTableCell(LocalizedSystemStrings.getInstance().xml(defect.getProjectPhaseString()));
            endTableRow();
            startTableRow();
            addTableCellBold(sxml("_dueDate1"));
            addTableCell(xml(defect.getDueDate1()));
            addTableCellBold(sxml("_dueDate2"));
            addTableCell(xml(defect.getDueDate2()));
            endTableRow();
            startTableRow();
            addTableCellBold(sxml("_status"));
            addTableCell(LocalizedSystemStrings.getInstance().xml(defect.getLastStatus().toString()));
            addTableCellBold(sxml("_closeDate"));
            addTableCell(xml(defect.getCloseDate()));
            endTableRow();
            endTable4Col();

            List<ImageData> files = defect.getFiles(ImageData.class);
            if (defect.getPositionX()>0 || defect.getPositionY()>0 || !files.isEmpty()) {
                BinaryFile file;
                startTable2Col();
                if (defect.getPositionX() > 0 || defect.getPositionY() > 0) {
                    ImageData plan = FileBean.getInstance().getFile(unit.getPlan().getId(), true, ImageData.class);
                    byte[] arrowBytes = FileBean.getInstance().getImageBytes(defect.getIconName());
                    file = defect.createCroppedDefectPlan(plan, arrowBytes, defect.getId(), defect.getPositionX(), defect.getPositionY());
                    addLabeledImage(sxml("_defectPosition"), file, "5.0cm");
                }
                if (!files.isEmpty()) {
                    addLabeledContent(sxml("_images"), "");
                    for (ImageData image : files) {
                        file = FileBean.getInstance().getBinaryFile(image.getId());
                        file.resizeImage(maxImageSize);
                        addLabeledImage("", file, "8.0cm");
                    }
                }
                endTable2Col();
            }
            if (includeStatusChanges) {
                addStatusList(defect);
            }
            endDefect();
        }
    }

    protected void addStatusList(DefectData defect) {
        List<DefectStatusData> statusChanges = defect.getStatusChanges();
        if (!statusChanges.isEmpty()) {
            startTable4Col();
            for (DefectStatusData changeData : defect.getStatusChanges()) {
                startTableRow();
                addTableCellBold(sxml("_statusChange"));
                endTableRow();
                startTableRow();
                addTableCellBold(sxml("_by"));
                addTableCell(xml(changeData.getCreatorName()));
                addTableCellBold(sxml("_on"));
                addTableCell(xml(changeData.getCreationDate()));
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
                List<ImageData> files = changeData.getFiles(ImageData.class);
                if (!files.isEmpty()) {
                    startTableRow();
                    addTableCellBold(sxml("_images"));
                    addTableCell(xml(changeData.getDescription()));
                    endTableRow();
                    List<ImageData> statusChangeImages = changeData.getFiles(ImageData.class);
                    for (ImageData image : statusChangeImages) {
                        BinaryFile file = FileBean.getInstance().getBinaryFile(image.getId());
                        file.resizeImage(maxImageSize);
                        addLabeledImage("", file, "8.0cm");
                    }
                }
            }
            endTable4Col();
        }
    }

}
