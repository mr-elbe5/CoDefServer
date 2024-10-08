/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.unit;

import de.elbe5.base.*;
import de.elbe5.content.ContentNavType;
import de.elbe5.defect.DefectData;
import de.elbe5.defect.DefectStatus;
import de.elbe5.file.FileBean;
import de.elbe5.file.ImageData;
import de.elbe5.project.ProjectData;
import de.elbe5.content.ContentBean;
import de.elbe5.content.ContentData;
import de.elbe5.file.FileData;
import de.elbe5.request.RequestData;

import de.elbe5.request.RequestType;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UnitData extends ContentData {

    public static int STD_PLAN_SIZE = 2190;

    public static int PLAN_PREVIEW_SIZE = 600;

    public static List<Class<? extends ContentData>> childClasses = new ArrayList<>();
    public static List<Class<? extends FileData>> fileClasses = new ArrayList<>();

    static {
        childClasses.add(DefectData.class);
        fileClasses.add(ImageData.class);
    }

    protected LocalDate approveDate = null;

    public UnitData() {
    }

    public ContentBean getBean() {
        return UnitBean.getInstance();
    }

    public ProjectData getProject() {
        return getParent(ProjectData.class);
    }

    public LocalDate getApproveDate() {
        return approveDate;
    }

    public LocalDateTime getApproveDateTime() {
        return LocalDateTime.of(approveDate, LocalTime.MIDNIGHT);
    }

    public void setApproveDate(LocalDate approveDate) {
        this.approveDate = approveDate;
    }

    public void setApproveDateTime(LocalDateTime approveDate) {
        this.approveDate = approveDate.toLocalDate();
    }

    public boolean isAfterApproveDate(LocalDate date){
        return getApproveDate().isBefore(date);
    }

    public boolean isAfterApproveDate(){
        return isAfterApproveDate(LocalDate.now());
    }

    public ImageData getPlan() {
        List<ImageData> images = getFiles(ImageData.class);
        if (!images.isEmpty())
            return images.get(0);
        return null;
    }

    @Override
    public String getBackendContentTreeJsp() {
        return "/WEB-INF/_jsp/unit/backendTreeContent.inc.jsp";
    }

    @Override
    public String getBackendEditJsp() {
        return "/WEB-INF/_jsp/unit/editBackendContent.ajax.jsp";
    }

    @Override
    public String getFrontendContentTreeJsp() {
        return "/WEB-INF/_jsp/unit/frontendTreeContent.inc.jsp";
    }

    @Override
    public void displayContent(PageContext context, RequestData rdata) throws IOException, ServletException {
        Writer writer = context.getOut();
        writer.write("<div id=\"pageContent\" class=\"viewArea\">");
        context.include("/WEB-INF/_jsp/unit/unit.jsp");
        writer.write("</div>");
    }

    @Override
    public List<Class<? extends ContentData>> getChildClasses(){
        return UnitData.childClasses;
    }

    public List<Class<? extends FileData>> getFileClasses(){
        return UnitData.fileClasses;
    }

    // multiple data

    @Override
    public void setCreateValues(RequestData rdata, RequestType type) {
        super.setCreateValues(rdata, type);
        setNavType(ContentNavType.HEADER);
        setActive(true);
        setOpenAccess(true);
    }

    @Override
    public void readRequestData(RequestData rdata, RequestType type) {
        Log.log("Unit.readRequestData");
        super.readRequestData(rdata, type);
        switch (type) {
            case api -> {
                setDisplayName(rdata.getAttributes().getString("displayName").trim());
                setName(StringHelper.toSafeWebName(getDisplayName()));
                setDescription(rdata.getAttributes().getString("description"));
                setApproveDate(rdata.getAttributes().getIsoDate("approveDate"));
            }
            case backend -> {
                setDisplayName(rdata.getAttributes().getString("displayName").trim());
                setName(StringHelper.toSafeWebName(getDisplayName()));
                setDescription(rdata.getAttributes().getString("description"));
                setApproveDate(rdata.getAttributes().getDate("approveDate"));
                setActive(rdata.getAttributes().getBoolean("active"));
                if (getDisplayName().isEmpty()) {
                    rdata.addIncompleteField("displayName");
                }
            }
        }
    }

    public ImageData readPlanFile(RequestData rdata, RequestType type){
        BinaryFile file = rdata.getAttributes().getFile("file");
        if (file != null){
            ImageData plan = new ImageData();
            plan.setCreateValues(rdata, type);
            plan.setParentValues(this);
            plan.setMaxSize(STD_PLAN_SIZE);
            plan.setPreviewSize(PLAN_PREVIEW_SIZE);
            plan.createFromBinaryFile(file);
            plan.setDisplayName(LocalizedStrings.getInstance().string("_plan"));
            return plan;
        }
        return null;
    }

    @Override
    public JsonObject getJson(){
        return super.getJson()
                .add("approveDate", getApproveDate());
    }

    @Override
    @SuppressWarnings("unchecked")
    public JsonObject getJsonRecursive(){
        ImageData plan = getPlan();
        JSONArray jsDefects = new JSONArray();
        for (DefectData defect : getChildren(DefectData.class)) {
            if (!defect.isActive() || defect.getStatus() == DefectStatus.DONE || defect.isClosed())
                continue;
            JsonObject jsDefect = defect.getJsonRecursive();
            jsDefects.add(jsDefect);
        }
        return getJson()
                .add("plan", plan != null ? plan.getJson() : null)
                .add("defects", jsDefects);
    }

    @Override
    public void fromJson(JSONObject json) {
        super.fromJson(json);
        LocalDate date = getLocalDate(json, "approveDate");
        if (date != null)
            setApproveDate(date);
    }

    @Override
    public void fromJsonRecursive(JSONObject json) {
        fromJson(json);
        addDefectsFromJson(json);
    }

    public void addDefectsFromJson(JSONObject json) {
        JSONArray jsDefects = getJSONArray(json, "defects");
        if (jsDefects != null){
            for (Object obj : jsDefects){
                if (obj instanceof JSONObject jsObj){
                    DefectData defect = new DefectData();
                    defect.fromJsonRecursive(jsObj);
                    if (defect.hasValidData())
                        getChildren().add(defect);
                }
            }
        }
    }

    public BinaryFile createUnitDefectPlan(ImageData plan, List<DefectData> defects, float scale){
        BinaryFile file=null;
        try {
            BufferedImage bi = ImageHelper.createImage(plan.getBytes(), "image/jpeg");
            assert(bi!=null);
            if (scale!=1){
                bi=ImageHelper.copyImage(bi,scale);
            }
            Graphics2D g = bi.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setFont(new Font("Monospaced", Font.PLAIN, 12));
            int biWidth=bi.getWidth();
            int biHeight=bi.getHeight();
            for (DefectData defect : defects){
                //Log.log("posx " + defect.getPositionX() +" posy " +defect.getPositionY());
                if (defect.hasValidPosition()) {
                    g.setColor(Color.RED);
                    int posX=(int)(biWidth*defect.getPositionX());
                    int posY=(int)(biHeight*defect.getPositionY());
                    BufferedImage arrowbi = ImageHelper.createImage(FileBean.getInstance().getImageBytes(defect.getIconName()),"image/png");
                    g.drawImage(arrowbi, null, posX - 9, posY - 2);
                    g.drawString(Integer.toString(defect.getId()), posX + 5, posY + 16);
                }
            }
            file=new BinaryFile();
            file.setFileName("defectPlan"+getId()+".jpg");
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType("image/jpeg");
            file.setContentType("image/jpeg");
            ImageWriter writer = writers.next();
            file.setBytes(ImageHelper.writeImage(writer, bi));
            file.setFileSize(file.getBytes().length);
        }
        catch (IOException e){
            Log.error("could not create defect plan", e);
        }
        return file;
    }

}
