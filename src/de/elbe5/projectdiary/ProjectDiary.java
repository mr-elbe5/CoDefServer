package de.elbe5.projectdiary;

import de.elbe5.application.MeteostatClient;
import de.elbe5.base.JsonObject;
import de.elbe5.base.Log;
import de.elbe5.base.StringHelper;
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
    int weatherCoco = 0;
    int weatherWspd = 0;
    int weatherWdir = 0;
    int weatherTemp = 0;
    int weatherRhum = 0;
    int weatherPrcp = 0;
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

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
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

    public int getWeatherPrcp() {
        return weatherPrcp;
    }

    public void setWeatherPrcp(int weatherPrcp) {
        this.weatherPrcp = weatherPrcp;
    }

    public int getWeatherRhum() {
        return weatherRhum;
    }

    public void setWeatherRhum(int weatherRhum) {
        this.weatherRhum = weatherRhum;
    }

    public int getWeatherTemp() {
        return weatherTemp;
    }

    public void setWeatherTemp(int weatherTemp) {
        this.weatherTemp = weatherTemp;
    }

    public int getWeatherWdir() {
        return weatherWdir;
    }

    public void setWeatherWdir(int weatherWdir) {
        this.weatherWdir = weatherWdir;
    }

    public int getWeatherWspd() {
        return weatherWspd;
    }

    public void setWeatherWspd(int weatherWspd) {
        this.weatherWspd = weatherWspd;
    }

    public int getWeatherCoco() {
        return weatherCoco;
    }

    public void setWeatherCoco(int weatherCoco) {
        this.weatherCoco = weatherCoco;
    }

    public boolean getWeather() {
        MeteostatClient.WeatherData weatherData = MeteostatClient.getWeatherData(getProject().getWeatherStation(), LocalDateTime.now());
        if (weatherData != null) {
            setWeatherCoco(weatherData.weatherCoco);
            setWeatherWspd(weatherData.weatherWspd);
            setWeatherWdir(weatherData.weatherWdir);
            setWeatherRhum(weatherData.weatherRhum);
            setWeatherTemp(weatherData.weatherTemp);
            setWeatherPrcp(weatherData.weatherPrcp);
            return true;
        }
        return false;
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
                int i = rdata.getAttributes().getInt("weatherCoco");
                if (i>0) {
                    setWeatherCoco(i);
                    setWeatherPrcp(rdata.getAttributes().getInt("weatherPrcp"));
                    setWeatherRhum(rdata.getAttributes().getInt("weatherRhum"));
                    setWeatherTemp(rdata.getAttributes().getInt("weatherTemp"));
                    setWeatherWdir(rdata.getAttributes().getInt("weatherWdir"));
                    setWeatherWspd(rdata.getAttributes().getInt("weatherWspd"));
                }
                setActivity(rdata.getAttributes().getString("activity"));
                setBriefing(rdata.getAttributes().getString("briefing"));
            }
            case backend ->{
                setDescription(rdata.getAttributes().getString("description"));
                int i = rdata.getAttributes().getInt("weatherCoco");
                if (i>0) {
                    setWeatherCoco(i);
                    setWeatherPrcp(rdata.getAttributes().getInt("weatherPrcp"));
                    setWeatherRhum(rdata.getAttributes().getInt("weatherRhum"));
                    setWeatherTemp(rdata.getAttributes().getInt("weatherTemp"));
                    setWeatherWdir(rdata.getAttributes().getInt("weatherWdir"));
                    setWeatherWspd(rdata.getAttributes().getInt("weatherWspd"));
                }
                setActivity(rdata.getAttributes().getString("activity"));
                setBriefing(rdata.getAttributes().getString("briefing"));
            }
        }
    }

    @Override
    public JsonObject getJson(){
        return super.getJson()
                .add("weatherCoco", getWeatherCoco())
                .add("weatherPrcp", getWeatherPrcp())
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
        int i = getInt(json, "weatherCoco");
        if (i>0) {
            setWeatherCoco(i);
            setWeatherPrcp(getInt(json, "weatherPrcp"));
            setWeatherRhum(getInt(json, "weatherRhum"));
            setWeatherTemp(getInt(json, "weatherTemp"));
            setWeatherWdir(getInt(json, "weatherWdir"));
            setWeatherWspd(getInt(json, "weatherWspd"));
        }
        String s = getString(json, "activity");
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
