/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.user;

import de.elbe5.base.Log;

import java.sql.*;

public class CodefUserBean extends UserBean {

    private static CodefUserBean instance = null;

    public static CodefUserBean getInstance() {
        if (instance == null) {
            instance = new CodefUserBean();
        }
        return instance;
    }

    private static final String GET_USER_EXTRAS_SQL = "SELECT project_id,company_ids,show_closed,show_preapprove,show_liability FROM t_codef_user WHERE id=?";

    @Override
    public void readUserExtras(Connection con, UserData userData) throws SQLException{
        if (!(userData instanceof CodefUserData data))
            return;
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(GET_USER_EXTRAS_SQL);
            pst.setInt(1, data.getId());
            boolean passed;
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    int i = 1;
                    data.setProjectId(rs.getInt(i++));
                    data.setCompanyIds(rs.getString(i++));
                    data.setShowClosed(rs.getBoolean(i++));
                    data.setShowPreapprove(rs.getBoolean(i++));
                    data.setShowLiability(rs.getBoolean(i));
                }
            }
        } catch (SQLException se) {
            Log.error("sql error", se);
        } finally {
            closeStatement(pst);
        }
    }

    private static final String INSERT_EXTENDED_SQL = "insert into t_codef_user (project_id,company_ids,show_closed,show_preapprove,show_liability,id) values(?,?,?,?,?,?)";

    @Override
    public void createUserExtras(Connection con, UserData userData) throws SQLException{
        if (!userData.isNew() || !(userData instanceof CodefUserData data))
            return;
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(INSERT_EXTENDED_SQL);
            int i = 1;
            if (data.getProjectId()==0){
                pst.setNull(i++, Types.INTEGER);
            }
            else{
                pst.setInt(i++, data.getProjectId());
            }
            pst.setString(i++, data.getCompanyIdsString());
            pst.setBoolean(i++, data.isShowClosed());
            pst.setBoolean(i++, data.isShowPreapprove());
            pst.setBoolean(i++, data.isShowLiability());
            pst.setInt(i, data.getId());
            pst.executeUpdate();
            pst.close();
            writeUserGroups(con, data);
        } finally {
            closeStatement(pst);
        }
    }

    private static final String UPDATE_EXTENDED_SQL = "update t_codef_user set project_id=?,company_ids=?,show_closed=?,show_preapprove=?,show_liability=? where id=?";

    @Override
    public void updateUserExtras(Connection con, UserData userData) throws SQLException{
        if (userData.isNew() || !(userData instanceof CodefUserData data))
            return;
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(UPDATE_EXTENDED_SQL);
            int i = 1;
            if (data.getProjectId()==0){
                pst.setNull(i++, Types.INTEGER);
            }
            else{
                pst.setInt(i++, data.getProjectId());
            }
            pst.setString(i++, data.getCompanyIdsString());
            pst.setBoolean(i++, data.isShowClosed());
            pst.setBoolean(i++, data.isShowPreapprove());
            pst.setBoolean(i++, data.isShowLiability());
            pst.setInt(i, data.getId());
            pst.executeUpdate();
            pst.close();
            writeUserGroups(con, data);
        } finally {
            closeStatement(pst);
        }
    }

    public boolean updateViewSettings(UserData data){
        Connection con = startTransaction();
        try {
            if (!data.isNew() && changedUser(con, data)) {
                return rollbackTransaction(con);
            }
            updateUserExtras(con, data);
            return commitTransaction(con);
        } catch (Exception se) {
            return rollbackTransaction(con, se);
        }
    }

}