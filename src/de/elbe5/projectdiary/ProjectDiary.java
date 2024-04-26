package de.elbe5.projectdiary;

import de.elbe5.application.MeteostatClient;
import de.elbe5.base.*;
import de.elbe5.company.CompanyCache;
import de.elbe5.configuration.CodefConfiguration;
import de.elbe5.content.ContentBean;
import de.elbe5.content.ContentData;
import de.elbe5.content.ContentNavType;
import de.elbe5.file.FileData;
import de.elbe5.project.ProjectData;
import de.elbe5.request.RequestData;
import de.elbe5.request.RequestType;
import de.elbe5.unit.UnitData;
import jakarta.servlet.ServletException;
import jakarta.servlet.jsp.PageContext;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.Writer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProjectDiary extends ContentData {

    public static List<Class<? extends ContentData>> childClasses = new ArrayList<>();
    public static List<Class<? extends FileData>> fileClasses = new ArrayList<>();

    static {
        childClasses.add(UnitData.class);
    }

    int idx = 0;
    String weatherCoco = "";
    String weatherWspd = "";
    String weatherWdir = "";
    String weatherTemp = "";
    String weatherRhum = "";
    String activity = "";
    String briefing = "";

    protected Set<Integer> companyIds = new HashSet<>();

    public ProjectDiary() {
    }

    public ContentBean getBean() {
        return ProjectDiaryBean.getInstance();
    }

    public ProjectData getProject(){
        return getParent(ProjectData.class);
    }

    @Override
    public String getDisplayName(){
        return getIdx() + " (" + DateHelper.toHtmlDate(getCreationDate()) + ")";
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public String getWeatherCoco() {
        return weatherCoco;
    }

    public void setWeatherCoco(String weatherCoco) {
        this.weatherCoco = weatherCoco;
    }

    public void setWeatherCoco(int value) {
        setWeatherCoco(MeteostatClient.getWeatherCoco(value));
    }

    public String getWeatherWspd() {
        return weatherWspd;
    }

    public void setWeatherWspd(String weatherWspd) {
        this.weatherWspd = weatherWspd;
    }

    public void setWeatherWspd(double value) {
        this.weatherWspd = value + " km/h";
    }

    public String getWeatherWdir() {
        return weatherWdir;
    }

    public void setWeatherWdir(String weatherWdir) {
        this.weatherWdir = weatherWdir;
    }

    public void setWeatherWdir(double value) {
        this.weatherWdir = MeteostatClient.getWindDirection(value);
    }

    public String getWeatherTemp() {
        return weatherTemp;
    }

    public void setWeatherTemp(String weatherTemp) {
        this.weatherTemp = weatherTemp;
    }

    public void setWeatherTemp(double value) {
        this.weatherTemp = String.format("%.1f °C", value);
    }

    public String getWeatherRhum() {
        return weatherRhum;
    }

    public void setWeatherRhum(String weatherRhum) {
        this.weatherRhum = weatherRhum;
    }

    public void setWeatherRhum(double value) {
        this.weatherRhum = String.format("%d %%", (int) value);
    }

    public String getBriefing() {
        return briefing;
    }

    public void setBriefing(String briefing) {
        this.briefing = briefing;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public boolean getWeather() {
        MeteostatClient.WeatherData weatherData = MeteostatClient.getWeatherData(getProject().getWeatherStation(), LocalDateTime.now(), CodefConfiguration.getTimeZoneName());
        if (weatherData != null) {
            setWeatherCoco(weatherData.weatherCoco);
            setWeatherWspd(weatherData.weatherWspd);
            setWeatherWdir(weatherData.weatherWdir);
            setWeatherRhum(weatherData.weatherRhum);
            setWeatherTemp(weatherData.weatherTemp);
            return true;
        }
        return false;
    }

    public Set<Integer> getCompanyIds() {
        return companyIds;
    }

    public String getCompaniesBoxHtml(){
        StringBuilder sb = new StringBuilder();
        for (int id : companyIds){
            if (!sb.isEmpty())
                sb.append("</br>");
            sb.append(StringHelper.toHtml(CompanyCache.getCompany(id).getName()));
        }
        return sb.toString();
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
        return "/WEB-INF/_jsp/projectdiary/backendTreeContent.inc.jsp";
    }

    @Override
    public String getBackendEditJsp() {
        return "/WEB-INF/_jsp/projectdiary/editBackendContent.ajax.jsp";
    }

    @Override
    public void displayContent(PageContext context, RequestData rdata) throws IOException, ServletException {
        Writer writer = context.getOut();
        writer.write("<div id=\"pageContent\" class=\"viewArea\">");
        context.include("/WEB-INF/_jsp/projectdiary/projectdiary.jsp");
        writer.write("</div>");
    }

    @Override
    public void setCreateValues(RequestData rdata, RequestType type) {
        super.setCreateValues(rdata, type);
        setNavType(ContentNavType.NONE);
        setActive(true);
        setOpenAccess(true);
    }

    @Override
    public void setParentValues(ContentData parent){
        super.setParentValues(parent);
        setIdx(getProject().getNextDiaryIndex());
        if (!getProject().getWeatherStation().isEmpty() && getWeatherCoco().isEmpty()){
            if (getWeather())
                Log.info("got weather for project diary " + getDisplayName() + " of project " + getProject().getName());
        }
    }

    @Override
    public void setNewId(){
        super.setNewId();
        setName(StringHelper.toSafeWebName(getDisplayName()));
    }

    @Override
    public void readRequestData(RequestData rdata, RequestType type) {
        Log.log("ProjectDiary.readRequestData");
        switch (type) {
            case api -> {
                super.readRequestData(rdata,type);
                setDescription(rdata.getAttributes().getString("description"));
                int i = rdata.getAttributes().getInt("weatherCoco");
                if (i>0) {
                    setWeatherCoco(i);
                    setWeatherRhum(rdata.getAttributes().getString("weatherRhum"));
                    setWeatherTemp(rdata.getAttributes().getString("weatherTemp"));
                    setWeatherWdir(rdata.getAttributes().getString("weatherWdir"));
                    setWeatherWspd(rdata.getAttributes().getString("weatherWspd"));
                }
                setCompanyIds(rdata.getAttributes().getIntegerSet("companyIds"));
                setActivity(rdata.getAttributes().getString("activity"));
                setBriefing(rdata.getAttributes().getString("briefing"));
            }
            case backend ->{
                setDescription(rdata.getAttributes().getString("description"));
                int i = rdata.getAttributes().getInt("weatherCoco");
                if (i>0) {
                    setWeatherCoco(i);
                    setWeatherRhum(rdata.getAttributes().getString("weatherRhum"));
                    setWeatherTemp(rdata.getAttributes().getString("weatherTemp"));
                    setWeatherWdir(rdata.getAttributes().getString("weatherWdir"));
                    setWeatherWspd(rdata.getAttributes().getString("weatherWspd"));
                }
                setCompanyIds(rdata.getAttributes().getIntegerSet("companyIds"));
                setActivity(rdata.getAttributes().getString("activity"));
                setBriefing(rdata.getAttributes().getString("briefing"));
            }
        }
    }

    @Override
    public JsonObject getJson(){
        return super.getJson()
                .add("weatherCoco", getWeatherCoco())
                .add("weatherRhum", getWeatherRhum())
                .add("weatherTemp", getWeatherTemp())
                .add("weatherWdir", getWeatherWdir())
                .add("weatherWspd", getWeatherWspd())
                .add("activity", getActivity())
                .add("briefing", getBriefing());
    }

    @Override
    public JsonObject getJsonRecursive(){
        return getJson()
                .add("images", getImagesForJson());
    }

    @Override
    public void fromJson(JSONObject json) {
        super.fromJson(json);
        String s = getString(json, "weatherCoco");
        if (!s.isEmpty()) {
            setWeatherCoco(s);
            setWeatherRhum(getString(json, "weatherRhum"));
            setWeatherTemp(getString(json, "weatherTemp"));
            setWeatherWdir(getString(json, "weatherWdir"));
            setWeatherWspd(getString(json, "weatherWspd"));
        }
        s = getString(json, "activity");
        if (s != null)
            setActivity(s);
        s = getString(json, "briefing");
        if (s != null)
            setBriefing(s);
    }

    @Override
    public void fromJsonRecursive(JSONObject json) {
        fromJson(json);
        addImagesFromJson(json);
    }

}
