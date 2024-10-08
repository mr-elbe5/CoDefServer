/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.project;

import de.elbe5.content.ContentBean;
import de.elbe5.content.ContentData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProjectBean extends ContentBean {

    private static ProjectBean instance = null;

    public static ProjectBean getInstance() {
        if (instance == null) {
            instance = new ProjectBean();
        }
        return instance;
    }

    private static final String GET_CONTENT_EXTRAS_SQL = " SELECT zip_code, city, street, weather_station FROM t_project where id = ?";

    @Override
    public void readContentExtras(Connection con, ContentData contentData) throws SQLException {
        if (!(contentData instanceof ProjectData data))
            return;
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(GET_CONTENT_EXTRAS_SQL);
            pst.setInt(1, data.getId());
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    int i=1;
                    data.setZipCode(rs.getString(i++));
                    data.setCity(rs.getString(i++));
                    data.setStreet(rs.getString(i++));
                    data.setWeatherStation(rs.getString(i));
                }
            }
        } finally {
            closeStatement(pst);
        }
        readProjectCompanies(con, data);
    }

    private static final String READ_PROJECT_COMPANIES_SQL = "SELECT company_id FROM t_company2project WHERE project_id=?";

    protected void readProjectCompanies(Connection con, ProjectData data) throws SQLException {
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(READ_PROJECT_COMPANIES_SQL);
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

    private static final String INSERT_CONTENT_EXTRAS_SQL = "insert into t_project (id, zip_code, city, street, weather_station) values(?,?,?,?,?)";

    @Override
    public void createContentExtras(Connection con, ContentData contentData) throws SQLException {
        if (!(contentData instanceof ProjectData data))
            return;
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(INSERT_CONTENT_EXTRAS_SQL);
            int i=1;
            pst.setInt(i++, data.getId());
            pst.setString(i++, data.getZipCode());
            pst.setString(i++, data.getCity());
            pst.setString(i++, data.getStreet());
            pst.setString(i, data.getWeatherStation());
            pst.executeUpdate();
            pst.close();
            writeProjectCompanies(con, data);

        } finally {
            closeStatement(pst);
        }
    }

    private static final String UPDATE_CONTENT_EXTRAS_SQL = "update t_project " +
            "set zip_code=?, city=?, street=?, weather_station=? where id=? ";


    @Override
    public void updateContentExtras(Connection con, ContentData contentData) throws SQLException {
        if (!(contentData instanceof ProjectData data))
            return;
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(UPDATE_CONTENT_EXTRAS_SQL);
            int i = 1;
            pst.setString(i++, data.getZipCode());
            pst.setString(i++, data.getCity());
            pst.setString(i++, data.getStreet());
            pst.setString(i++, data.getWeatherStation());
            pst.setInt(i, data.getId());
            pst.executeUpdate();
            pst.close();
        } finally {
            closeStatement(pst);
        }
        writeProjectCompanies(con, data);
    }

    private static final String DELETE_PROJECTCOMPANIES_SQL = "DELETE FROM t_company2project WHERE project_id=?";
    private static final String INSERT_PROJECTCOMPANIES_SQL = "INSERT INTO t_company2project (project_id,company_id) VALUES(?,?)";

    protected void writeProjectCompanies(Connection con, ProjectData data) throws SQLException {
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(DELETE_PROJECTCOMPANIES_SQL);
            pst.setInt(1, data.getId());
            pst.execute();
            if (data.getCompanyIds() != null) {
                pst.close();
                pst = con.prepareStatement(INSERT_PROJECTCOMPANIES_SQL);
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
