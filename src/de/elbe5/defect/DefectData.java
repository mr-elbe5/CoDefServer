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
import de.elbe5.content.ContentViewType;
import de.elbe5.defectstatuschange.DefectStatusChangeData;
import de.elbe5.content.ContentBean;
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
        childClasses.add(DefectStatusChangeData.class);
        fileClasses.add(ImageData.class);
    }

    protected int displayId = 0;
    protected int assignedId = 0;

    protected boolean notified = false;
    protected String lot = "";
    protected int costs = 0;
    protected int positionX = 0; // Percent * 100
    protected int positionY = 0; // Percent * 100
    protected String positionComment = "";
    protected LocalDate dueDate1 = null;
    protected LocalDate dueDate2 = null;
    protected LocalDate closeDate = null;

    // base data

    public ContentBean getBean() {
        return DefectBean.getInstance();
    }

    @Override
    public String getName(){
        return "defect-"+getDisplayId();
    }

    @Override
    public String getDisplayName(){
        return LocalizedStrings.string("_defect") + " " +getDisplayId();
    }

    public int getDisplayId() {
        return displayId;
    }

    public void setDisplayId(int displayId) {
        this.displayId = displayId;
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

    public int getAssignedId() {
        return assignedId;
    }

    public void setAssignedId(int assignedId) {
        this.assignedId = assignedId;
    }

    public boolean isNotified() {
        return notified;
    }

    public void setNotified(boolean notified) {
        this.notified = notified;
    }

    public String getLot() {
        return lot;
    }

    public void setLot(String lot) {
        this.lot = lot;
    }

    public DefectStatus getStatus() {
        DefectStatusChangeData statusChange = getLastStatusChange();
        return statusChange == null ? DefectStatus.OPEN : statusChange.getStatus();
    }

    public String getDefectStatusString(){
        return getStatus().toString();
    }

    public boolean isClosed(){
        return getCloseDate()!=null;
    }

    public int getCosts() {
        return costs;
    }

    public String getCostsString() {
        return costs==0 ? "" : Integer.toString(costs);
    }

    public void setCosts(int costs) {
        this.costs = costs;
    }

    public int getPositionX() {
        return positionX;
    }

    public void setPositionX(int positionX) {
        this.positionX = positionX;
    }

    public int getPositionY() {
        return positionY;
    }

    public void setPositionY(int positionY) {
        this.positionY = positionY;
    }

    public String getPositionComment() {
        return positionComment;
    }

    public void setPositionComment(String positionComment) {
        this.positionComment = positionComment;
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

    public List<DefectStatusChangeData> getStatusChanges() {
        return new ArrayList<>(getChildren(DefectStatusChangeData.class));
    }

    public DefectStatusChangeData getLastStatusChange(){
        List<DefectStatusChangeData> statusChanges = getStatusChanges();
        if (statusChanges.isEmpty()){
            return null;
        }
        return statusChanges.get(statusChanges.size()-1);
    }

    public String getAssignedName() {
        if (assignedId==0)
            return "";
        CompanyData data= CompanyCache.getCompany(assignedId);
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
        if (ContentViewType.EDIT.equals(getViewType())) {
            if (isNew())
                context.include("/WEB-INF/_jsp/defect/createFrontendContent.jsp");
            else
                context.include("/WEB-INF/_jsp/defect/editFrontendContent.jsp");
        } else {
            context.include("/WEB-INF/_jsp/defect/defect.jsp");
        }
        writer.write("</div>");
    }

    // multiple data

    @Override
    public void setCreateValues(ContentData parent, RequestData rdata) {
        super.setCreateValues(parent, rdata);
        if (!(this.parent instanceof UnitData unit)) {
            Log.error("parent of defect page should be unit page");
            return;
        }
        setDisplayId(DefectBean.getInstance().getNextDisplayId());
        ProjectData project = (ProjectData) unit.getParent();
        setNavType(ContentNavType.NONE);
    }

    public void readBackendRequestData(RequestData rdata) {
        Log.log("DefectData.readBackendRequestData");
        setDescription(rdata.getAttributes().getString("description"));
        setAssignedId(rdata.getAttributes().getInt("assignedId"));
        setNotified(rdata.getAttributes().getBoolean("notified"));
        setLot(rdata.getAttributes().getString("lot"));
        setCosts(rdata.getAttributes().getInt("costs"));
        setDueDate1(rdata.getAttributes().getDate("dueDate1"));
        setDueDate2(rdata.getAttributes().getDate("dueDate2"));
        setPositionX(rdata.getAttributes().getInt("positionX"));
        setPositionY(rdata.getAttributes().getInt("positionY"));
        setPositionComment(rdata.getAttributes().getString("positionComment"));
        if (getDescription().isEmpty()) {
            rdata.addIncompleteField("description");
        }
        if (getAssignedId()==0) {
            rdata.addIncompleteField("assigned");
        }
        if (getDueDate()==null) {
            rdata.addIncompleteField("dueDate1");
        }
    }

    @Override
    public void readFrontendCreateRequestData(RequestData rdata) {
        Log.log("DefectData.readFrontendCreateRequestData");
        readFrontendRequestData(rdata);
        setDescription(rdata.getAttributes().getString("description").trim());
        setDueDate1(rdata.getAttributes().getDate("dueDate1"));
        setPositionX(rdata.getAttributes().getInt("positionX"));
        setPositionY(rdata.getAttributes().getInt("positionY"));
        setPositionComment(rdata.getAttributes().getString("positionComment"));
        if (getDescription().isEmpty()) {
            rdata.addIncompleteField("description");
        }
        if (getAssignedId()==0) {
            rdata.addIncompleteField("assignedId");
        }
        if (getDueDate()==null) {
            rdata.addIncompleteField("dueDate1");
        }
    }

    @Override
    public void readFrontendUpdateRequestData(RequestData rdata) {
        Log.log("DefectData.readFrontendUpdateRequestData");
        readFrontendRequestData(rdata);
        setDueDate2(rdata.getAttributes().getDate("dueDate2"));
        if (getAssignedId()==0) {
            rdata.addIncompleteField("assigned");
        }
    }

    public void readFrontendRequestData(RequestData rdata) {
        Log.log("DefectData.readFrontendRequestData");
        setAssignedId(rdata.getAttributes().getInt("assignedId"));
        setNotified(rdata.getAttributes().getBoolean("notified"));
        setLot(rdata.getAttributes().getString("lot"));
        setCosts(rdata.getAttributes().getInt("costs"));
        List<BinaryFile> newFiles = rdata.getAttributes().getFileList("files");
        for (BinaryFile f : newFiles) {
            if (f.isImage()){
                ImageData image = new ImageData();
                image.setCreateValues(this, rdata);
                if (!image.createFromBinaryFile(f, image.getMaxWidth(), image.getMaxHeight(), image.getMaxPreviewWidth(),image.getMaxPreviewHeight(), false))
                    continue;
                image.setChangerId(rdata.getUserId());
                getFiles().add(image);
            }
        }
    }



    @Override
    public JsonObject getJson(){
        return super.getJson()
                .add("displayId",getDisplayId())
                .add("assignedId",getAssignedId())
                .add("assignedName",getAssignedName())
                .add("positionX",getPositionX())
                .add("positionY",getPositionY())
                .add("positionComment",getPositionComment())
                .add("state", getStatus().toString())
                .add("dueDate", getDueDate())
                .add("phase", "DEFAULT");
    }

    @Override
    public JsonObject getJsonRecursive(){
        JsonArray jsStatusChanges = new JsonArray();
        for (DefectStatusChangeData statusChange : getStatusChanges()) {
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
        int i = getInt(json, "displayId");
        if (i!=0)
            setDisplayId(i);
        i = getInt(json, "assignedId");
        if (i!=0)
            setAssignedId(i);
        i = getInt(json, "positionX");
        if (i!=0)
            setPositionX(i);
        i = getInt(json, "positionY");
        if (i!=0)
            setPositionY(i);
        String s = getString(json, "positionComment");
        if (s != null)
            setPositionComment(s);
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
                    DefectStatusChangeData statusChange = new DefectStatusChangeData();
                    statusChange.fromJsonRecursive(jsObj);
                    if (statusChange.hasValidData())
                        getChildren().add(statusChange);
                }
            }
        }
    }

    public BinaryFile createCroppedDefectPlan(ImageData plan, byte[] primaryArrawBytes, int defectDisplayId, int positionX, int positionY) {
        BinaryFile file = null;

        try {
            BufferedImage source = ImageHelper.createImage(plan.getBytes(), "image/jpeg");
            assert (source != null);
            int srcWidth=source.getWidth();
            int srcHeight=source.getHeight();
            assert(srcWidth>=PLAN_CROP_WIDTH && srcHeight>=PLAN_CROP_HEIGHT);
            int posX=srcWidth*positionX/100/100;
            int posY=srcHeight*positionY/100/100;
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
            g.drawImage(redbi, null, PLAN_CROP_WIDTH / 2 - 9 + dx, PLAN_CROP_HEIGHT / 2 - 2 + dy);
            g.drawString(Integer.toString(defectDisplayId), posX + 3, posY + 16);
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

    public BinaryFile createFullDefectPlan(ImageData plan, byte[] primaryArrawBytes, int defectDisplayId, int positionX, int positionY) {
        BinaryFile file = null;
        try {
            BufferedImage bi = ImageHelper.createImage(plan.getBytes(), "image/jpeg");
            BufferedImage redbi = ImageHelper.createImage(primaryArrawBytes, "image/png");
            assert (bi != null);
            Graphics2D g = bi.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setFont(new Font("Monospaced", Font.PLAIN, 12));
            g.setColor(Color.RED);
            int posX=bi.getWidth()*positionX/100/100;
            int posY=bi.getHeight()*positionY/100/100;
            g.drawImage(redbi, null, posX -9 , posY- 2);
            g.drawString(Integer.toString(defectDisplayId), posX + 3, posY + 16);
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
