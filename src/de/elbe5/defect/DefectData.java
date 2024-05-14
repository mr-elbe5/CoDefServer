/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.defect;

import de.elbe5.base.*;
import de.elbe5.company.CompanyCache;
import de.elbe5.company.CompanyData;
import de.elbe5.content.ContentNavType;
import de.elbe5.defectstatus.DefectStatusData;
import de.elbe5.content.ContentBean;
import de.elbe5.project.ProjectPhase;
import de.elbe5.request.RequestType;
import de.elbe5.unit.UnitData;
import de.elbe5.content.ContentData;
import de.elbe5.project.ProjectData;
import de.elbe5.file.FileData;
import de.elbe5.file.ImageData;
import de.elbe5.request.RequestData;
import jakarta.servlet.ServletException;
import jakarta.servlet.jsp.PageContext;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Writer;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class DefectData extends ContentData {

    public static int PLAN_CROP_WIDTH = 600;
    public static int PLAN_CROP_HEIGHT = 300;

    public static List<Class<? extends ContentData>> childClasses = new ArrayList<>();
    public static List<Class<? extends FileData>> fileClasses = new ArrayList<>();

    static {
        childClasses.add(DefectStatusData.class);
        fileClasses.add(ImageData.class);
    }

    protected String comment = "";
    protected String location = "";
    protected boolean remainingWork = false;
    protected int assignedId = 0;

    protected ProjectPhase projectPhase = ProjectPhase.PREAPPROVAL;
    protected boolean notified = false;
    protected double positionX = 0; //width fraction
    protected double positionY = 0; //height fraction
    protected LocalDate dueDate1 = null;
    protected LocalDate dueDate2 = null;
    protected LocalDate closeDate = null;

    // base data

    public ContentBean getBean() {
        return DefectBean.getInstance();
    }

    @Override
    public String getName(){
        return remainingWork ? "remainingwork-" : "defect-"+getId();
    }

    @Override
    public String getDisplayName(){
        return LocalizedStrings.getInstance().string(remainingWork ? "_remainingWork" : "_defect") + " " +getId();
    }

    public UnitData getUnit() {
        return getParent(UnitData.class);
    }

    public ProjectData getProject() {
        return getUnit().getProject();
    }

    public ImageData getPlan(){
        return getUnit().getPlan();
    }

    public int getPlanId(){
        return (getPlan() == null ? 0 : getPlan().getId());
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isRemainingWork() {
        return remainingWork;
    }

    public void setRemainingWork(boolean remainingWork) {
        this.remainingWork = remainingWork;
    }

    public String getIconName(){
        return isRemainingWork() ? "bluearrow.png" : "redarrow.png";
    }

    public int getAssignedId() {
        return assignedId;
    }

    public void setAssignedId(int assignedId) {
        this.assignedId = assignedId;
    }

    public ProjectPhase getProjectPhase() {
        return projectPhase;
    }

    public String getProjectPhaseString() {
        return projectPhase.name();
    }

    public void setProjectPhase(ProjectPhase projectPhase) {
        this.projectPhase = projectPhase;
    }

    public void setProjectPhase(String name) {
        try{
            projectPhase = ProjectPhase.valueOf(name);
        }
        catch(IllegalArgumentException e){
            projectPhase = ProjectPhase.PREAPPROVAL;
        }
    }

    public void setProjectPhaseFromApproveDate(UnitData unit){
        if (unit.isAfterApproveDate(getCreationDate().toLocalDate())){
            setProjectPhase(ProjectPhase.LIABILITY);
        }
        else{
            setProjectPhase(ProjectPhase.PREAPPROVAL);
        }
    }

    public boolean isNotified() {
        return notified;
    }

    public void setNotified(boolean notified) {
        this.notified = notified;
    }

    public DefectStatus getStatus() {
        DefectStatusData statusChange = getLastStatusChange();
        return statusChange == null ? DefectStatus.OPEN : statusChange.getStatus();
    }

    public String getDefectStatusString(){
        return getStatus().toString();
    }

    public boolean isClosed(){
        return getCloseDate()!=null;
    }

    public double getPositionX() {
        return positionX;
    }

    public void setPositionX(double positionX) {
        this.positionX = positionX;
    }

    public double getPositionY() {
        return positionY;
    }

    public void setPositionY(double positionY) {
        this.positionY = positionY;
    }

    public boolean hasValidPosition(){
        return positionX != 0 || positionY != 0;
    }

    public LocalDate getDueDate1() {
        return dueDate1;
    }

    public void setDueDate1(LocalDate dueDate1) {
        this.dueDate1 = dueDate1;
    }

    public LocalDate getDueDate2() {
        return dueDate2;
    }

    public void setDueDate2(LocalDate dueDate2) {
        this.dueDate2 = dueDate2;
    }

    public LocalDate getDueDate() {
        return dueDate2 != null ? dueDate2 : dueDate1;
    }

    public LocalDate getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(LocalDate closeDate) {
        this.closeDate = closeDate;
    }

    public List<DefectStatusData> getStatusChanges() {
        return new ArrayList<>(getChildren(DefectStatusData.class));
    }

    public DefectStatusData getLastStatusChange(){
        List<DefectStatusData> statusChanges = getStatusChanges();
        if (statusChanges.isEmpty()){
            return null;
        }
        return statusChanges.get(statusChanges.size()-1);
    }

    public int getLastAssignedId(){
        DefectStatusData statusChange = getLastStatusChange();
        return statusChange != null ? statusChange.getAssignedId() : getAssignedId();
    }

    public DefectStatus getLastStatus(){
        DefectStatusData statusChange = getLastStatusChange();
        return statusChange != null ? statusChange.getStatus() : getStatus();
    }

    public String getAssignedName() {
        if (assignedId==0)
            return "";
        CompanyData data= CompanyCache.getCompany(assignedId);
        if (data!=null)
            return data.getName();
        return "";
    }

    public String getLastAssignedName() {
        int id = getLastAssignedId();
        if (id==0)
            return "";
        CompanyData data= CompanyCache.getCompany(id);
        if (data!=null)
            return data.getName();
        return "";
    }

    @Override
    public List<Class<? extends ContentData>> getChildClasses(){
        return DefectData.childClasses;
    }


    public List<Class<? extends FileData>> getFileClasses(){
        return DefectData.fileClasses;
    }

    // view

    public String getBackendContentTreeJsp() {
        return "/WEB-INF/_jsp/defect/backendTreeContent.inc.jsp";
    }

    @Override
    public String getBackendEditJsp() {
        return "/WEB-INF/_jsp/defect/editBackendContent.ajax.jsp";
    }

    @Override
    public String getFrontendContentTreeJsp() {
        return "/WEB-INF/_jsp/defect/frontendTreeContent.inc.jsp";
    }

    @Override
    public void displayContent(PageContext context, RequestData rdata) throws IOException, ServletException {
        Writer writer = context.getOut();
        writer.write("<div id=\"pageContent\" class=\"viewArea\">");
        if (isEditMode()) {
            context.include("/WEB-INF/_jsp/defect/editFrontendContent.jsp");
        } else {
            context.include("/WEB-INF/_jsp/defect/defect.jsp");
        }
        writer.write("</div>");
    }

    // multiple data

    @Override
    public void setCreateValues(RequestData rdata, RequestType type) {
        super.setCreateValues(rdata, type);
        setNavType(ContentNavType.NONE);
        setActive(true);
        setOpenAccess(true);
    }

    @Override
    public void setNewId(){
        super.setNewId();
        setName(StringHelper.toSafeWebName(getDisplayName()));
    }

    @Override
    public void readRequestData(RequestData rdata, RequestType type) {
        Log.log("DefectData.readRequestData");
        switch (type) {
            case api -> {
                super.readRequestData(rdata,type);
                setDescription(rdata.getAttributes().getString("description"));
                setComment(rdata.getAttributes().getString("positionComment"));
                setLocation(rdata.getAttributes().getString("location"));
                setRemainingWork(rdata.getAttributes().getBoolean("remainingWork"));
                setAssignedId(rdata.getAttributes().getInt("assignedId"));
                setProjectPhase(rdata.getAttributes().getString("projectPhase"));
                setNotified(rdata.getAttributes().getBoolean("notified"));
                setDueDate1(rdata.getAttributes().getIsoDate("dueDate1"));
                setPositionX(rdata.getAttributes().getDouble("positionX"));
                setPositionY(rdata.getAttributes().getDouble("positionY"));
            }
            case frontend -> {
                setDescription(rdata.getAttributes().getString("description"));
                setComment(rdata.getAttributes().getString("comment"));
                setLocation(rdata.getAttributes().getString("location"));
                setRemainingWork(rdata.getAttributes().getBoolean("remainingWork"));
                setAssignedId(rdata.getAttributes().getInt("assignedId"));
                setProjectPhase(rdata.getAttributes().getString("projectPhase"));
                setNotified(rdata.getAttributes().getBoolean("notified"));
                if (isNew()) {
                    setDueDate1(rdata.getAttributes().getDate("dueDate1"));
                    setPositionX(rdata.getAttributes().getDouble("positionX"));
                    setPositionY(rdata.getAttributes().getDouble("positionY"));
                    if (getDueDate() == null) {
                        rdata.addIncompleteField("dueDate1");
                    }
                }
                else{
                    setDueDate2(rdata.getAttributes().getDate("dueDate2"));
                }
                List<BinaryFile> newFiles = rdata.getAttributes().getFileList("files");
                for (BinaryFile file : newFiles) {
                    if (file.isImage()) {
                        ImageData image = new ImageData();
                        image.setCreateValues(rdata, RequestType.frontend);
                        image.setParentValues(this);
                        if (!image.createFromBinaryFile(file))
                            continue;
                        image.setChangerId(rdata.getUserId());
                        getFiles().add(image);
                    }
                }
                if (getDescription().isEmpty()) {
                    rdata.addIncompleteField("description");
                }
                if (getAssignedId() == 0) {
                    rdata.addIncompleteField("assignedId");
                }
            }
            case backend ->{
                setDescription(rdata.getAttributes().getString("description"));
                setComment(rdata.getAttributes().getString("comment"));
                setLocation(rdata.getAttributes().getString("location"));
                setRemainingWork(rdata.getAttributes().getBoolean("remainingWork"));
                setAssignedId(rdata.getAttributes().getInt("assignedId"));
                setProjectPhase(rdata.getAttributes().getString("projectPhase"));
                setNotified(rdata.getAttributes().getBoolean("notified"));
                setDueDate1(rdata.getAttributes().getDate("dueDate1"));
                setDueDate2(rdata.getAttributes().getDate("dueDate2"));
                setPositionX(rdata.getAttributes().getDouble("positionX"));
                setPositionY(rdata.getAttributes().getDouble("positionY"));
                if (getDescription().isEmpty()) {
                    rdata.addIncompleteField("description");
                }
                if (getAssignedId() == 0) {
                    rdata.addIncompleteField("assignedId");
                }
                if (getDueDate() == null) {
                    rdata.addIncompleteField("dueDate1");
                }
            }
        }
    }

    @Override
    public JsonObject getJson(){
        return super.getJson()
                .add("projectPhase", getProjectPhaseString())
                .add("notified", isNotified())
                .add("location", getLocation())
                .add("positionComment", getComment())
                .add("remainingWork",isRemainingWork())
                .add("assignedId",getLastAssignedId())
                .add("assignedName",getLastAssignedName())
                .add("state", getStatus().toString())
                .add("dueDate1", getDueDate1())
                .add("dueDate2", getDueDate2())
                .add("positionX",getPositionX())
                .add("positionY",getPositionY());

    }

    @Override
    @SuppressWarnings("unchecked")
    public JsonObject getJsonRecursive(){
        JSONArray jsStatusChanges = new JSONArray();
        for (DefectStatusData statusChange : getStatusChanges()) {
            JsonObject jsStatusChange = statusChange.getJsonRecursive();
            jsStatusChanges.add(jsStatusChange);
        }
        return getJson()
                .add("statusChanges", jsStatusChanges)
                .add("images", getImagesForJson())
                .add("documents", getDocumentsForJson());
    }

    @Override
    public void fromJson(JSONObject json) {
        super.fromJson(json);
        String s = getString(json, "projectPhase");
        if (s != null)
            setProjectPhase(s);
        s = getString(json, "comment");
        if (s != null)
            setLocation(s);
        s = getString(json, "positionComment");
        if (s != null)
            setComment(s);
        s = getString(json, "remainingWork");
        if (s != null)
            setRemainingWork(s.equalsIgnoreCase(Boolean.TRUE.toString()));
        int i = getInt(json, "assignedId");
        if (i!=0)
            setAssignedId(i);
        i = getInt(json, "positionX");
        if (i!=0)
            setPositionX(i);
        i = getInt(json, "positionY");
        if (i!=0)
            setPositionY(i);
        LocalDate date = getLocalDate(json, "dueDate");
        if (date != null)
            setDueDate1(date);
    }

    @Override
    public void fromJsonRecursive(JSONObject json) {
        fromJson(json);
        addImagesFromJson(json);
        addDocumentsFromJson(json);
        addStatusChangesFromJson(json);
    }

    public void addStatusChangesFromJson(JSONObject json) {
        JSONArray jsStatusChanges = getJSONArray(json, "statusChanges");
        if (jsStatusChanges != null){
            for (Object obj : jsStatusChanges){
                if (obj instanceof JSONObject jsObj){
                    DefectStatusData statusData = new DefectStatusData();
                    statusData.fromJsonRecursive(jsObj);
                    if (statusData.hasValidData())
                        getChildren().add(statusData);
                }
            }
        }
    }

    public BinaryFile createCroppedDefectPlan(ImageData plan, byte[] primaryArrawBytes, int defectDisplayId, double positionX, double positionY) {
        BinaryFile file = null;

        try {
            BufferedImage source = ImageHelper.createImage(plan.getBytes(), "image/jpeg");
            assert (source != null);
            int srcWidth=source.getWidth();
            int srcHeight=source.getHeight();
            assert(srcWidth>=PLAN_CROP_WIDTH && srcHeight>=PLAN_CROP_HEIGHT);
            int posX=(int)(srcWidth*positionX);
            int posY=(int)(srcHeight*positionY);
            int x = posX - PLAN_CROP_WIDTH / 2;
            int y = posY - PLAN_CROP_HEIGHT / 2;
            int dx = 0;
            int dy = 0;
            if (x < 0) {
                dx = x;
                x = 0;
            }
            else if (x+PLAN_CROP_WIDTH>srcWidth){
                dx = x+PLAN_CROP_WIDTH-srcWidth;
                x = srcWidth-PLAN_CROP_WIDTH;
            }
            if (y < 0) {
                dy = y;
                y = 0;
            }
            else if (y+PLAN_CROP_HEIGHT>srcHeight){
                dy=y+PLAN_CROP_HEIGHT-srcHeight;
                y= srcHeight-PLAN_CROP_HEIGHT;
            }
            BufferedImage bi = source.getSubimage(x, y, PLAN_CROP_WIDTH, PLAN_CROP_HEIGHT);
            BufferedImage redbi = ImageHelper.createImage(primaryArrawBytes, "image/png");
            assert (bi != null);
            Graphics2D g = bi.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setFont(new Font("Monospaced", Font.PLAIN, 12));
            g.setColor(Color.RED);
            posX = PLAN_CROP_WIDTH/2 + dx;
            posY = PLAN_CROP_HEIGHT/2 + dy;
            g.drawImage(redbi, null, posX - 9, posY - 2);
            g.drawString(Integer.toString(defectDisplayId), posX + 5, posY + 16);
            file = new BinaryFile();
            file.setFileName("defectCrop" + defectDisplayId + ".jpg");
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType("image/jpeg");
            file.setContentType("image/jpeg");
            ImageWriter writer = writers.next();
            file.setBytes(ImageHelper.writeImage(writer, bi));
            file.setFileSize(file.getBytes().length);
        } catch (IOException e) {
            Log.error("could not create defect plan", e);
        }
        return file;
    }

    public BinaryFile createFullDefectPlan(ImageData plan, byte[] primaryArrowBytes, int defectDisplayId, double positionX, double positionY) {
        BinaryFile file = null;
        try {
            BufferedImage bi = ImageHelper.createImage(plan.getBytes(), "image/jpeg");
            BufferedImage arrowbi = ImageHelper.createImage(primaryArrowBytes, "image/png");
            assert (bi != null);
            Graphics2D g = bi.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setFont(new Font("Monospaced", Font.PLAIN, 12));
            g.setColor(Color.RED);
            int posX=(int)(bi.getWidth()*positionX);
            int posY=(int)(bi.getHeight()*positionY);
            g.drawImage(arrowbi, null, posX -9 , posY- 2);
            g.drawString(Integer.toString(defectDisplayId), posX + 5, posY + 16);
            file = new BinaryFile();
            file.setFileName("defectPlan" + defectDisplayId + ".jpg");
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType("image/jpeg");
            file.setContentType("image/jpeg");
            ImageWriter writer = writers.next();
            file.setBytes(ImageHelper.writeImage(writer, bi));
            file.setFileSize(file.getBytes().length);
        } catch (IOException e) {
            Log.error("could not create defect plan", e);
        }
        return file;
    }

}
