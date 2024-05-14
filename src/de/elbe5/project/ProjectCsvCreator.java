package de.elbe5.project;

import com.opencsv.CSVWriter;
import de.elbe5.base.BinaryFile;
import de.elbe5.base.DateHelper;
import de.elbe5.base.LocalizedSystemStrings;
import de.elbe5.content.ContentCache;
import de.elbe5.defect.DefectData;
import de.elbe5.defectstatus.DefectStatusData;
import de.elbe5.file.CsvCreator;
import de.elbe5.unit.UnitData;

import java.util.ArrayList;
import java.util.List;

public class ProjectCsvCreator extends CsvCreator {

    ProjectData project;

    public BinaryFile getCsvFile(int projectId){
        project= ContentCache.getContent(projectId,ProjectData.class);
        assert project!= null;
        String fileName="project-spreadsheet-" + project.getId() + "-" + DateHelper.toHtml(DateHelper.getCurrentTime()).replace(' ','-')+".csv";
        String csv = createCSV();
        return getCsv(csv, fileName);
    }

    static String[] projectFields = {""};
    static String[] unitFields = {"_unit","_approveDate"};
    static String[] defectFields = {"_id","_defect","_commentOrDescription","_unitOrLocation","_defectType","_assigned","_status",
            "_dueDate","_dueDate2","_closed",
            "_projectPhase","_creationDate","_editedBy","_changeDate"};
    static String[] statusFields = {"_statusChangeBy","_on","_status","_assigned","_description"};

    @Override
    public void writeContent(CSVWriter writer) {
        writeHeader(writer);
        for (UnitData unit : project.getChildren(UnitData.class)) {
            writeUnit(writer, unit);
        }
    }

    private void writeHeader(CSVWriter writer){
        List<String> list = new ArrayList<>();
        list.add(csv(project.getDisplayName()));
        for (String s : unitFields)
            list.add(scsv(s));
        for (String s : defectFields)
            list.add(scsv(s));
        for (String s : statusFields)
            list.add(scsv(s));
        writeLine(writer,list);
    }

    private void writeUnit(CSVWriter writer, UnitData unit){
        List<String> list = new ArrayList<>();
        for (int i = 0; i < projectFields.length; i++)
            list.add("");
        list.add(csv(unit.getDisplayName()));
        list.add(csv(unit.getApproveDate()));
        writeLine(writer,list);
        for (DefectData defect : unit.getChildren(DefectData.class)) {
            writeDefect(writer, defect);
        }
    }

    private void writeDefect(CSVWriter writer, DefectData defect){
        List<String> list = new ArrayList<>();
        for (int i = 0; i < projectFields.length + unitFields.length; i++)
            list.add("");
        list.add(String.valueOf(defect.getId()));
        list.add(csv(defect.getDescription()));
        list.add(csv(defect.getComment()));
        list.add(csv(defect.getLocation()));
        list.add(csv(defect.isRemainingWork() ? scsv("_remainingWork") : scsv("_defect") ));
        list.add(csv(defect.getLastAssignedName()));
        list.add(csv(LocalizedSystemStrings.getInstance().csv(defect.getLastStatus().toString())));
        list.add(csv(defect.getDueDate()));
        list.add(csv(defect.getDueDate2()));
        list.add(csv(defect.getCloseDate()));
        list.add(csv(LocalizedSystemStrings.getInstance().csv(defect.getProjectPhaseString())));
        list.add(csv(defect.getCreationDate()));
        list.add(csv(defect.getChangerName()));
        list.add(csv(defect.getChangeDate()));
        writeLine(writer,list);
        for (DefectStatusData status : defect.getChildren(DefectStatusData.class)) {
            writeDefectStatus(writer, status);
        }
    }

    private void writeDefectStatus(CSVWriter writer, DefectStatusData status){
        List<String> list = new ArrayList<>();
        for (int i = 0; i < projectFields.length + unitFields.length + defectFields.length; i++)
            list.add("");
        list.add(csv(status.getCreatorName()));
        list.add(csv(status.getCreationDate()));
        list.add(LocalizedSystemStrings.getInstance().csv(status.getStatusString()));
        list.add(csv(status.getAssignedName()));
        list.add(csv(status.getDescription()));
        writeLine(writer,list);
    }
}
