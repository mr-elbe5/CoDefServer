package de.elbe5.projectdailyreport;

import de.elbe5.content.ContentBean;
import de.elbe5.content.ContentData;
import de.elbe5.file.FileBean;
import de.elbe5.file.FileData;

import java.sql.*;

public class ProjectDailyReportBean extends ContentBean {

    private static ProjectDailyReportBean instance = null;

    public static ProjectDailyReportBean getInstance() {
        if (instance == null) {
            instance = new ProjectDailyReportBean();
        }
        return instance;
    }

    private static final String GET_CONTENT_EXTRAS_SQL = " SELECT idx, weather_coco, weather_wspd, weather_wdir, " +
            "weather_temp, weather_rhum FROM t_project_daily_report where id = ?";

    @Override
    public void readContentExtras(Connection con, ContentData contentData) throws SQLException {
        if (!(contentData instanceof ProjectDailyReport data))
            return;
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(GET_CONTENT_EXTRAS_SQL);
            pst.setInt(1, data.getId());
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    int i=1;
                    data.setIdx(rs.getInt(i++));
                    data.setWeatherCoco(rs.getString(i++));
                    data.setWeatherWspd(rs.getString(i++));
                    data.setWeatherWdir(rs.getString(i++));
                    data.setWeatherTemp(rs.getString(i++));
                    data.setWeatherRhum(rs.getString(i));
                }
            }
        } finally {
            closeStatement(pst);
        }
        readProjectDiaryCompanies(con, data);
    }

    private static final String READ_PROJECT_DAILY_REPORTS_COMPANIES_SQL = "SELECT company_id, activity, briefing FROM t_company_briefing WHERE project_daily_report_id=?";

    protected void readProjectDiaryCompanies(Connection con, ProjectDailyReport data) throws SQLException {
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(READ_PROJECT_DAILY_REPORTS_COMPANIES_SQL);
            pst.setInt(1, data.getId());
            try (ResultSet rs = pst.executeQuery()) {
                data.getCompanyBriefings().clear();
                while (rs.next()) {
                    CompanyBriefing briefing = new CompanyBriefing();
                    briefing.setCompanyId(rs.getInt(1));
                    briefing.setActivity(rs.getString(2));
                    briefing.setBriefing(rs.getString(3));
                    data.getCompanyBriefings().add(briefing);
                }
            }
        } finally {
            closeStatement(pst);
        }
    }

    private static final String INSERT_CONTENT_EXTRAS_SQL = "insert into t_project_daily_report (id, idx, weather_coco, weather_wspd, weather_wdir, " +
            "weather_temp, weather_rhum) " +
            "values(?,?,?,?,?,?,?)";

    @Override
    public void createContentExtras(Connection con, ContentData contentData) throws SQLException {
        if (!(contentData instanceof ProjectDailyReport data))
            return;
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(INSERT_CONTENT_EXTRAS_SQL);
            int i=1;
            pst.setInt(i++, data.getId());
            pst.setInt(i++, data.getIdx());
            pst.setString(i++, data.getWeatherCoco());
            pst.setString(i++, data.getWeatherWspd());
            pst.setString(i++, data.getWeatherWdir());
            pst.setString(i++, data.getWeatherTemp());
            pst.setString(i, data.getWeatherRhum());
            pst.executeUpdate();
            pst.close();
            writeProjectDiaryCompanies(con, data);
        } finally {
            closeStatement(pst);
        }
        for (FileData file : data.getFiles()){
            FileBean.getInstance().saveFile(con, file, true);
        }
        writeProjectDiaryCompanies(con, data);
    }

    private static final String UPDATE_CONTENT_EXTRAS_SQL = "update t_project_daily_report " +
            "set idx=?, weather_coco=?, weather_wspd=?, weather_wdir=?, weather_temp=?, weather_rhum=? where id=? ";


    @Override
    public void updateContentExtras(Connection con, ContentData contentData) throws SQLException {
        if (!(contentData instanceof ProjectDailyReport data))
            return;
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(UPDATE_CONTENT_EXTRAS_SQL);
            int i = 1;
            pst.setInt(i++, data.getIdx());
            pst.setString(i++, data.getWeatherCoco());
            pst.setString(i++, data.getWeatherWspd());
            pst.setString(i++, data.getWeatherWdir());
            pst.setString(i++, data.getWeatherTemp());
            pst.setString(i++, data.getWeatherRhum());
            pst.setInt(i, data.getId());
            pst.executeUpdate();
            pst.close();
        } finally {
            closeStatement(pst);
        }
        for (FileData file : data.getFiles()){
            if (file.isNew()) {
                FileBean.getInstance().saveFile(con, file, true);
            }
        }
        writeProjectDiaryCompanies(con, data);
    }

    private static final String DELETE_PROJECT_DAILY_REPORTS_COMPANIES_SQL = "DELETE FROM t_company_briefing WHERE project_daily_report_id=?";
    private static final String INSERT_PROJECT_DAILY_REPORTS_COMPANIES_SQL = "INSERT INTO t_company_briefing (project_daily_report_id,company_id, activity, briefing) VALUES(?,?,?,?)";

    protected void writeProjectDiaryCompanies(Connection con, ProjectDailyReport data) throws SQLException {
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(DELETE_PROJECT_DAILY_REPORTS_COMPANIES_SQL);
            pst.setInt(1, data.getId());
            pst.execute();
            if (data.getCompanyBriefings() != null) {
                pst.close();
                pst = con.prepareStatement(INSERT_PROJECT_DAILY_REPORTS_COMPANIES_SQL);
                pst.setInt(1, data.getId());
                for (CompanyBriefing briefing : data.getCompanyBriefings()) {
                    pst.setInt(2, briefing.getCompanyId());
                    pst.setString(3, briefing.getActivity());
                    pst.setString(4, briefing.getBriefing());
                    pst.executeUpdate();
                }
            }
        } finally {
            closeStatement(pst);
        }
    }

}