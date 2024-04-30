package de.elbe5.projectdailyreport;

import de.elbe5.base.IJsonData;
import de.elbe5.base.JsonObject;

public class CompanyBriefing implements IJsonData {

    protected int companyId = 0;
    protected String activity = "";
    protected String briefing = "";

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getBriefing() {
        return briefing;
    }

    public void setBriefing(String briefing) {
        this.briefing = briefing;
    }

    @Override
    public JsonObject getJson(){
        return new JsonObject()
                .add("companyId", getCompanyId())
                .add("activity", getActivity())
                .add("briefing", getBriefing())
                ;
    }

}
