/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.project;

import de.elbe5.application.Coordinate;
import de.elbe5.application.MeteostatClient;
import de.elbe5.application.NominatimClient;
import de.elbe5.base.*;
import de.elbe5.configuration.CodefConfiguration;
import de.elbe5.configuration.StaticConfiguration;
import de.elbe5.content.ContentNavType;
import de.elbe5.projectdiary.ProjectDiary;
import de.elbe5.request.RequestType;
import de.elbe5.unit.UnitData;
import de.elbe5.content.ContentBean;
import de.elbe5.content.ContentData;
import de.elbe5.file.FileData;
import de.elbe5.request.RequestData;

import jakarta.servlet.ServletException;
import jakarta.servlet.jsp.PageContext;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.Writer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProjectData extends ContentData {

    public static List<Class<? extends ContentData>> childClasses = new ArrayList<>();
    public static List<Class<? extends FileData>> fileClasses = new ArrayList<>();

    static {
        childClasses.add(UnitData.class);
    }

    protected String zipCode = "";
    protected String city = "";
    protected String street = "";
    protected String weatherStation = "";

    protected Set<Integer> companyIds = new HashSet<>();

    public ProjectData() {
    }

    public ContentBean getBean() {
        return ProjectBean.getInstance();
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getWeatherStation() {
        return weatherStation;
    }

    public void setWeatherStation(String weatherStation) {
        this.weatherStation = weatherStation;
    }

    public int getNextDiaryIndex(){
        int idx = 0;
        for (ProjectDiary diary : getChildren(ProjectDiary.class)){
            if (diary.getIdx() > idx){
                idx = diary.getIdx();
            }
        }
        return idx + 1;
    }

    public Set<Integer> getCompanyIds() {
        return companyIds;
    }

    public void setCompanyIds(Set<Integer> companyIds) {
        this.companyIds = companyIds;
    }

    @Override
    public List<Class<? extends ContentData>> getChildClasses(){
        return ProjectData.childClasses;
    }

    public List<Class<? extends FileData>> getFileClasses(){
        return ProjectData.fileClasses;
    }

    @Override
    public String getBackendContentTreeJsp() {
        return "/WEB-INF/_jsp/project/backendTreeContent.inc.jsp";
    }

    @Override
    public String getBackendEditJsp() {
        return "/WEB-INF/_jsp/project/editBackendContent.ajax.jsp";
    }

    @Override
    public String getFrontendContentTreeJsp() {
        return "/WEB-INF/_jsp/project/frontendTreeContent.inc.jsp";
    }

    @Override
    public void displayContent(PageContext context, RequestData rdata) throws IOException, ServletException {
        Writer writer = context.getOut();
        writer.write("<div id=\"pageContent\" class=\"viewArea\">");
        context.include("/WEB-INF/_jsp/project/project.jsp");
        writer.write("</div>");
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
        Log.log("ProjectData.readRequestData");
        switch (type){
            case api -> {
                super.readRequestData(rdata, type);
                setZipCode(rdata.getAttributes().getString("zipCode"));
                setCity(rdata.getAttributes().getString("city"));
                setStreet(rdata.getAttributes().getString("street"));
                setWeatherStation(rdata.getAttributes().getString("weatherStation"));
                if (getWeatherStation().isEmpty()){
                    findWeatherStation();
                }
                setCompanyIds(rdata.getAttributes().getIntegerSet("companyIds"));
            }
            case backend -> {
                setDisplayName(rdata.getAttributes().getString("displayName").trim());
                setName(StringHelper.toSafeWebName(getDisplayName()));
                setDescription(rdata.getAttributes().getString("description"));
                setZipCode(rdata.getAttributes().getString("zipCode"));
                setCity(rdata.getAttributes().getString("city"));
                setStreet(rdata.getAttributes().getString("street"));
                setWeatherStation(rdata.getAttributes().getString("weatherStation"));
                if (getWeatherStation().isEmpty()){
                    findWeatherStation();
                }
                if (StaticConfiguration.useReadRights()) {
                    setOpenAccess(rdata.getAttributes().getBoolean("openAccess"));
                }
                if (StaticConfiguration.useReadRights() && StaticConfiguration.useReadGroup()) {
                    setReaderGroupId(rdata.getAttributes().getInt("readerGroupId"));
                }
                if (StaticConfiguration.useEditorGroup()) {
                    setEditorGroupId(rdata.getAttributes().getInt("editorGroupId"));
                }
                setActive(rdata.getAttributes().getBoolean("active"));
                setCompanyIds(rdata.getAttributes().getIntegerSet("companyIds"));
                if (getDisplayName().isEmpty()) {
                    rdata.addIncompleteField("displayName");
                }
                if (StaticConfiguration.useEditorGroup() && getEditorGroupId() == 0){
                    rdata.addIncompleteField("editorGroupId");
                }
                if (getCompanyIds().isEmpty()){
                    rdata.addIncompleteField("companyIds");
                }
            }
        }

    }

    public void findWeatherStation(){
        Coordinate coordinate = NominatimClient.getCoordinate(CodefConfiguration.getDefaultCountry(), getCity(), getStreet());
        if (coordinate == null)
            return;
        String s = MeteostatClient.findWeatherStation(coordinate);
        if (!s.isEmpty()){
            setWeatherStation(s);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public JsonObject getJson(){
        JSONArray jsCompanyIds = new JSONArray();

        jsCompanyIds.addAll(getCompanyIds());
        return super.getJson()
                .add("zipCode", getZipCode())
                .add("city", getCity())
                .add("street", getStreet())
                .add("weatherStation", getWeatherStation())
                .add("companyIds", jsCompanyIds);
    }

    @Override
    @SuppressWarnings("unchecked")
    public JsonObject getJsonRecursive(){
        JSONArray jsUnits = new JSONArray();
        for (UnitData unit : getChildren(UnitData.class)) {
            if (!unit.isActive())
                continue;
            jsUnits.add(unit.getJsonRecursive());
        }
        JSONArray jsDiaries = new JSONArray();
        for (ProjectDiary diary : getChildren(ProjectDiary.class)) {
            if (!diary.isActive())
                continue;
            jsDiaries.add(diary.getJsonRecursive());
        }
        return getJson()
                .add("units", jsUnits)
                .add("diaries", jsDiaries);
    }

    @Override
    public void fromJson(JSONObject json) {
        super.fromJson(json);
        String s = getString(json, "zipCode");
        if (s!=null) {
            setZipCode(s);
        }
        s = getString(json, "city");
        if (s!=null)
            setCity(s);
        s = getString(json, "street");
        if (s!=null)
            setStreet(s);
        s = getString(json, "weatherStation");
        if (s!=null)
            setWeatherStation(s);
    }

    @Override
    public void fromJsonRecursive(JSONObject json) {
        fromJson(json);
        addUnitsFromJson(json);
        addDiariesFromJson(json);
    }

    public void addUnitsFromJson(JSONObject json) {
        JSONArray jsUnits = getJSONArray(json, "units");
        if (jsUnits != null){
            for (Object obj : jsUnits){
                if (obj instanceof JSONObject jsObj){
                    UnitData unit = new UnitData();
                    unit.fromJsonRecursive(jsObj);
                    if (unit.hasValidData())
                        getChildren().add(unit);
                }
            }
        }
    }

    public void addDiariesFromJson(JSONObject json) {
        JSONArray jsDiaries = getJSONArray(json, "diaries");
        if (jsDiaries != null){
            for (Object obj : jsDiaries){
                if (obj instanceof JSONObject jsObj){
                    ProjectDiary diary = new ProjectDiary();
                    diary.fromJsonRecursive(jsObj);
                    if (diary.hasValidData())
                        getChildren().add(diary);
                }
            }
        }
    }

}
