/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.company;

import de.elbe5.base.Log;
import de.elbe5.database.DbBean;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CompanyBean extends DbBean {

    private static CompanyBean instance = null;

    public static CompanyBean getInstance() {
        if (instance == null) {
            instance = new CompanyBean();
        }
        return instance;
    }

    public int getNextId() {
        return getNextId("s_company_id");
    }

    private static final String CHANGED_SQL = "SELECT change_date FROM t_company WHERE id=?";

    protected boolean changedCompany(Connection con, CompanyData data) {
        return changedItem(con, CHANGED_SQL, data);
    }

    private static final String SELECT_COMPANY_SQL = "SELECT id,change_date,name,street,zipcode,city,country,email,phone,fax FROM t_company";

    private static final String GET_ALL_COMPANIES_SQL = SELECT_COMPANY_SQL + " ORDER BY name";

    public List<CompanyData> getAllCompanies() {
        List<CompanyData> list = new ArrayList<>();
        Connection con = getConnection();
        PreparedStatement pst = null;
        CompanyData data;
        try {
            pst = con.prepareStatement(GET_ALL_COMPANIES_SQL);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    data = new CompanyData();
                    readCompanyData(data, rs);
                    list.add(data);
                }
            }
        } catch (SQLException se) {
            Log.error("sql error", se);
        } finally {
            closeStatement(pst);
            closeConnection(con);
        }
        return list;
    }

    private static final String GET_COMPANY_SQL = SELECT_COMPANY_SQL + " WHERE id=?";

    public CompanyData getCompany(int id) {
        Connection con = getConnection();
        PreparedStatement pst = null;
        CompanyData data = null;
        try {
            pst = con.prepareStatement(GET_COMPANY_SQL);
            pst.setInt(1, id);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                data = new CompanyData();
                readCompanyData(data, rs);
            }
        } catch (SQLException se) {
            Log.error("sql error", se);
        } finally {
            closeStatement(pst);
            closeConnection(con);
        }
        return data;
    }

    private void readCompanyData(CompanyData data, ResultSet rs) throws SQLException {
        int i = 1;
        data.setId(rs.getInt(i++));
        data.setChangeDate(rs.getTimestamp(i++).toLocalDateTime());
        data.setName(rs.getString(i++));
        data.setStreet(rs.getString(i++));
        data.setZipCode(rs.getString(i++));
        data.setCity(rs.getString(i++));
        data.setCountry(rs.getString(i++));
        data.setEmail(rs.getString(i++));
        data.setPhone(rs.getString(i++));
        data.setFax(rs.getString(i));
    }

    public boolean saveCompany(CompanyData data) {
        Connection con = startTransaction();
        try {
            if (!data.isNew() && changedCompany(con, data)) {
                return rollbackTransaction(con);
            }
            data.setChangeDate(getServerTime(con));
            writeCompany(con, data);
            return commitTransaction(con);
        } catch (Exception se) {
            return rollbackTransaction(con, se);
        }
    }

    private static final String INSERT_COMPANY_SQL = "insert into t_company (change_date,name,street,zipcode,city,country,email,phone,fax,id) values(?,?,?,?,?,?,?,?,?,?)";
    private static final String UPDATE_COMPANY_SQL = "update t_company set change_date=?,name=?,street=?,zipcode=?,city=?,country=?,email=?,phone=?,fax=? where id=?";

    protected void writeCompany(Connection con, CompanyData data) throws SQLException {
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(data.isNew() ? INSERT_COMPANY_SQL : UPDATE_COMPANY_SQL);
            int i = 1;
            pst.setTimestamp(i++, Timestamp.valueOf(data.getChangeDate()));
            pst.setString(i++, data.getName());
            pst.setString(i++, data.getStreet());
            pst.setString(i++, data.getZipCode());
            pst.setString(i++, data.getCity());
            pst.setString(i++, data.getCountry());
            pst.setString(i++, data.getEmail());
            pst.setString(i++, data.getPhone());
            pst.setString(i++, data.getFax());
            pst.setInt(i, data.getId());
            pst.executeUpdate();
            pst.close();
        } finally {
            closeStatement(pst);
        }
    }

    private static final String DELETE_COMPANY_SQL = "DELETE FROM t_company WHERE id=?";

    public void deleteCompany(int id) {
        Connection con = getConnection();
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(DELETE_COMPANY_SQL);
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException se) {
            Log.error("sql error", se);
        } finally {
            closeStatement(pst);
            closeConnection(con);
        }
    }

}
