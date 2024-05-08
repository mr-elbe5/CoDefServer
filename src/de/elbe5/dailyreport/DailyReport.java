package de.elbe5.dailyreport;

import de.elbe5.application.MeteostatClient;
import de.elbe5.base.*;
import de.elbe5.configuration.CodefConfiguration;
import de.elbe5.content.ContentBean;
import de.elbe5.content.ContentData;
import de.elbe5.content.ContentNavType;
import de.elbe5.file.FileData;
import de.elbe5.file.ImageData;
import de.elbe5.project.ProjectData;
import de.elbe5.request.RequestData;
import de.elbe5.request.RequestType;
import de.elbe5.unit.UnitData;
import jakarta.servlet.ServletException;
import jakarta.servlet.jsp.PageContext;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.Writer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DailyReport extends ContentData {

    public static List<Class<? extends ContentData>> childClasses = new ArrayList<>();
    public static List<Class<? extends FileData>> fileClasses = new ArrayList<>();

    static {
        childClasses.add(UnitData.class);
    }

    protected int idx = 0;
    protected LocalDateTime reportDate = LocalDateTime.now();
    protected String weatherCoco = "";
    protected String weatherWspd = "";
    protected String weatherWdir = "";
    protected String weatherTemp = "";
    protected String weatherRhum = "";

    protected List<CompanyBriefing> companyBriefings = new ArrayList<>();

    public DailyReport() {
    }

    public ContentBean getBean() {
        return DailyReportBean.getInstance();
    }

    public ProjectData getProject(){
        return getParent(ProjectData.class);
    }

    @Override
    public String getDisplayName(){
        return getIdx() + " (" + DateHelper.toHtmlDate(getReportDate()) + ")";
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public LocalDateTime getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDateTime reportDate) {
        this.reportDate = reportDate;
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

    public boolean getWeather(LocalDateTime date) {
        MeteostatClient.WeatherData weatherData = MeteostatClient.getWeatherData(getProject().getWeatherStation(), date, CodefConfiguration.getTimeZoneName());
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

    public boolean getWeather() {
        return getWeather(getReportDate());
    }

    public List<CompanyBriefing> getCompanyBriefings() {
        return companyBriefings;
    }

    public CompanyBriefing getCompanyBriefing(int companyId) {
        for (CompanyBriefing companyBriefing : companyBriefings) {
            if (companyBriefing.getCompanyId() == companyId) {
                return companyBriefing;
            }
        }
        return null;
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
        return "/WEB-INF/_jsp/dailyreport/backendTreeContent.inc.jsp";
    }

    @Override
    public String getFrontendEditJsp() {
        return "/WEB-INF/_jsp/dailyreport/editFrontendContent.jsp";
    }

    @Override
    public String getBackendEditJsp() {
        return "/WEB-INF/_jsp/dailyreport/editBackendContent.ajax.jsp";
    }

    @Override
    public void displayContent(PageContext context, RequestData rdata) throws IOException, ServletException {
        Writer writer = context.getOut();
        writer.write("<div id=\"pageContent\" class=\"viewArea\">");
        if (isEditMode()) {
            context.include("/WEB-INF/_jsp/dailyreport/editFrontendContent.jsp");
        } else {
            context.include("/WEB-INF/_jsp/dailyreport/dailyreport.jsp");
        }
        writer.write("</div>");
    }

    @Override
    public void setCreateValues(RequestData rdata, RequestType type) {
        super.setCreateValues(rdata, type);
        setNavType(ContentNavType.NONE);
        setActive(true);
        setOpenAccess(true);
        setReportDate(getCreationDate());
    }

    @Override
    public void setParentValues(ContentData parent){
        super.setParentValues(parent);
        setIdx(getProject().getNextDiaryIndex());
        if (!getProject().getWeatherStation().isEmpty() && getWeatherCoco().isEmpty()){
            if (getWeather())
                Log.info("got weather for daily report " + getDisplayName() + " of project " + getProject().getName());
        }
    }

    @Override
    public void setNewId(){
        super.setNewId();
        setName(StringHelper.toSafeWebName(getDisplayName()));
    }

    @Override
    public void readRequestData(RequestData rdata, RequestType type) {
        //Log.log("ProjectDailyReport.readRequestData");
        switch (type) {
            case api -> {
                super.readRequestData(rdata,type);
                setDescription(rdata.getAttributes().getString("description"));
                int i = rdata.getAttributes().getInt("idx");
                if (i>0)
                    setIdx(i);
                setReportDate(getCreationDate());
                String s = rdata.getAttributes().getString("weatherCoco");
                if (!s.isEmpty()) {
                    setWeatherCoco(s);
                    setWeatherRhum(rdata.getAttributes().getString("weatherRhum"));
                    setWeatherTemp(rdata.getAttributes().getString("weatherTemp"));
                    setWeatherWdir(rdata.getAttributes().getString("weatherWdir"));
                    setWeatherWspd(rdata.getAttributes().getString("weatherWspd"));
                }
                getCompanyBriefings().clear();
                for (int companyId : getProject().getCompanyIds()){
                    if (rdata.getAttributes().getBoolean("company_" + companyId + "_present")){
                        CompanyBriefing briefing = new CompanyBriefing();
                        briefing.setCompanyId(companyId);
                        briefing.setActivity(rdata.getAttributes().getString("company_" + companyId + "_activity"));
                        briefing.setBriefing(rdata.getAttributes().getString("company_" + companyId + "_briefing"));
                        getCompanyBriefings().add(briefing);
                    }
                }
            }
            case frontend -> {
                setDescription(rdata.getAttributes().getString("description"));
                setReportDate(rdata.getAttributes().getDateTime("reportDate"));
                String s = rdata.getAttributes().getString("weatherCoco");
                if (!s.isEmpty()) {
                    setWeatherCoco(s);
                    setWeatherRhum(rdata.getAttributes().getString("weatherRhum"));
                    setWeatherTemp(rdata.getAttributes().getString("weatherTemp"));
                    setWeatherWdir(rdata.getAttributes().getString("weatherWdir"));
                    setWeatherWspd(rdata.getAttributes().getString("weatherWspd"));
                }
                getCompanyBriefings().clear();
                for (int companyId : getProject().getCompanyIds()){
                    if (rdata.getAttributes().getBoolean("company_" + companyId + "_present")){
                        CompanyBriefing briefing = new CompanyBriefing();
                        briefing.setCompanyId(companyId);
                        briefing.setActivity(rdata.getAttributes().getString("company_" + companyId + "_activity"));
                        briefing.setBriefing(rdata.getAttributes().getString("company_" + companyId + "_briefing"));
                        getCompanyBriefings().add(briefing);
                    }
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
            }
            case backend ->{
                setDescription(rdata.getAttributes().getString("description"));
                setReportDate(rdata.getAttributes().getDateTime("reportDate"));
                String s = rdata.getAttributes().getString("weatherCoco");
                if (!s.isEmpty()) {
                    setWeatherCoco(s);
                    setWeatherRhum(rdata.getAttributes().getString("weatherRhum"));
                    setWeatherTemp(rdata.getAttributes().getString("weatherTemp"));
                    setWeatherWdir(rdata.getAttributes().getString("weatherWdir"));
                    setWeatherWspd(rdata.getAttributes().getString("weatherWspd"));
                }
                getCompanyBriefings().clear();
                for (int companyId : getProject().getCompanyIds()){
                    if (rdata.getAttributes().getBoolean("company_" + companyId + "_present")){
                        CompanyBriefing briefing = new CompanyBriefing();
                        briefing.setCompanyId(companyId);
                        briefing.setActivity(rdata.getAttributes().getString("company_" + companyId + "_activity"));
                        briefing.setBriefing(rdata.getAttributes().getString("company_" + companyId + "_briefing"));
                        getCompanyBriefings().add(briefing);
                    }
                }
            }
        }
    }

    @Override
    public JsonObject getJson(){
        return super.getJson()
                .add("idx", getIdx())
                .add("weatherCoco", getWeatherCoco())
                .add("weatherRhum", getWeatherRhum())
                .add("weatherTemp", getWeatherTemp())
                .add("weatherWdir", getWeatherWdir())
                .add("weatherWspd", getWeatherWspd())
                ;
    }

    @SuppressWarnings("unchecked")
    public JSONArray getCompanyBriefingsForJson() {
        JSONArray jsCompanyBriefings = new JSONArray();
        for (CompanyBriefing companyBriefing : getCompanyBriefings()) {
            JsonObject jsCompanyBriefing = companyBriefing.getJson();
            jsCompanyBriefings.add(jsCompanyBriefing);
        }
        return jsCompanyBriefings;
    }

    @Override
    public JsonObject getJsonRecursive(){
        return getJson()
                .add("images", getImagesForJson())
                .add("companyBriefings", getCompanyBriefingsForJson());
    }

    @Override
    public void fromJson(JSONObject json) {
        super.fromJson(json);
        setIdx(getInt(json,"idx"));
        String s = getString(json, "weatherCoco");
        if (!s.isEmpty()) {
            setWeatherCoco(s);
            setWeatherRhum(getString(json, "weatherRhum"));
            setWeatherTemp(getString(json, "weatherTemp"));
            setWeatherWdir(getString(json, "weatherWdir"));
            setWeatherWspd(getString(json, "weatherWspd"));
        }

    }

    @Override
    public void fromJsonRecursive(JSONObject json) {
        fromJson(json);
        addImagesFromJson(json);
    }

}
