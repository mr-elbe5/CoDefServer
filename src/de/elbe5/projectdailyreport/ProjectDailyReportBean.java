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
            "weather_temp, weather_rhum, activity, briefing FROM t_project_daily_report where id = ?";

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
                    data.setWeatherRhum(rs.getString(i++));
                    data.setActivity(rs.getString(i++));
                    data.setBriefing(rs.getString(i));
                }
            }
        } finally {
            closeStatement(pst);
        }
        readProjectDiaryCompanies(con, data);
    }

    private static final String READ_PROJECT_DAILY_REPORTS_COMPANIES_SQL = "SELECT company_id FROM t_company2project_daily_report WHERE project_daily_report_id=?";

    protected void readProjectDiaryCompanies(Connection con, ProjectDailyReport data) throws SQLException {
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(READ_PROJECT_DAILY_REPORTS_COMPANIES_SQL);
            pst.setInt(1, data.getId());
            try (ResultSet rs = pst.executeQuery()) {
                data.getCompanyIds().clear();
                while (rs.next()) {
                    data.getCompanyIds().add(rs.getInt(1));
                }
            }
        } finally {
            closeStatement(pst);
        }
    }

    private static final String INSERT_CONTENT_EXTRAS_SQL = "insert into t_project_daily_report (id, idx, weather_coco, weather_wspd, weather_wdir, " +
            "weather_temp, weather_rhum, activity, briefing) " +
            "values(?,?,?,?,?,?,?,?,?)";

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
            pst.setString(i++, data.getWeatherRhum());
            pst.setString(i++, data.getActivity());
            pst.setString(i, data.getBriefing());
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
            "set idx=?, weather_coco=?, weather_wspd=?, weather_wdir=?, weather_temp=?, weather_rhum=?, activity=?, briefing=? where id=? ";


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
            pst.setString(i++, data.getActivity());
            pst.setString(i++, data.getBriefing());
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

    private static final String DELETE_PROJECT_DAILY_REPORTS_COMPANIES_SQL = "DELETE FROM t_company2project_daily_report WHERE project_daily_report_id=?";
    private static final String INSERT_PROJECT_DAILY_REPORTS_COMPANIES_SQL = "INSERT INTO t_company2project_daily_report (project_daily_report_id,company_id) VALUES(?,?)";

    protected void writeProjectDiaryCompanies(Connection con, ProjectDailyReport data) throws SQLException {
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(DELETE_PROJECT_DAILY_REPORTS_COMPANIES_SQL);
            pst.setInt(1, data.getId());
            pst.execute();
            if (data.getCompanyIds() != null) {
                pst.close();
                pst = con.prepareStatement(INSERT_PROJECT_DAILY_REPORTS_COMPANIES_SQL);
                pst.setInt(1, data.getId());
                for (int companyId : data.getCompanyIds()) {
                    pst.setInt(2, companyId);
                    pst.executeUpdate();
                }
            }
        } finally {
            closeStatement(pst);
        }
    }

}