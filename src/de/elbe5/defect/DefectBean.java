/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.defect;

import de.elbe5.content.ContentBean;
import de.elbe5.content.ContentData;
import de.elbe5.file.FileBean;
import de.elbe5.file.FileData;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class DefectBean extends ContentBean {

    private static DefectBean instance = null;

    public static DefectBean getInstance() {
        if (instance == null) {
            instance = new DefectBean();
        }
        return instance;
    }

    public int getNextDisplayId() {
        return getNextId("s_defect_id");
    }

    private static final String GET_CONTENT_EXTRAS_SQL = "SELECT display_id,unit_id,project_id,plan_id, assigned_id, notified, lot, status, " +
            "costs, position_x, position_y, position_comment, " +
            "due_date1, due_date2, close_date  " +
            "FROM t_defect  WHERE id=?";

    @Override
    public void readContentExtras(Connection con, ContentData contentData) throws SQLException {
        if (!(contentData instanceof DefectData data))
            return;
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(GET_CONTENT_EXTRAS_SQL);
            pst.setInt(1, data.getId());
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    int i=1;
                    data.setDisplayId(rs.getInt(i++));
                    data.setUnitId(rs.getInt(i++));
                    data.setProjectId(rs.getInt(i++));
                    data.setPlanId(rs.getInt(i++));
                    data.setAssignedId(rs.getInt(i++));
                    data.setNotified(rs.getBoolean(i++));
                    data.setLot(rs.getString(i++));
                    data.setStatus(rs.getString(i++));
                    data.setCosts(rs.getInt(i++));
                    data.setPositionX(rs.getInt(i++));
                    data.setPositionY(rs.getInt(i++));
                    data.setPositionComment(rs.getString(i++));
                    Timestamp ts = rs.getTimestamp(i++);
                    data.setDueDate1(ts == null ? null : ts.toLocalDateTime().toLocalDate());
                    ts = rs.getTimestamp(i++);
                    data.setDueDate2(ts == null ? null : ts.toLocalDateTime().toLocalDate());
                    ts = rs.getTimestamp(i);
                    data.setCloseDate(ts == null ? null : ts.toLocalDateTime().toLocalDate());
                }
            }
        } finally {
            closeStatement(pst);
        }
    }

    private static final String INSERT_CONTENT_EXTRAS_SQL = "insert into t_defect (" +
            "display_id,unit_id,project_id,plan_id, assigned_id, notified, lot, " +
            "due_date1, status, costs, " +
            "position_x, position_y,position_comment,id) " +
            "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

    @Override
    public void createContentExtras(Connection con, ContentData contentData) throws SQLException {
        if (!contentData.isNew() || !(contentData instanceof DefectData data))
            return;
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(INSERT_CONTENT_EXTRAS_SQL);
            int i = 1;
            pst.setInt(i++,data.getDisplayId());
            pst.setInt(i++,data.getUnitId());
            pst.setInt(i++,data.getProjectId());
            pst.setInt(i++, data.getPlanId());
            pst.setInt(i++, data.getAssignedId());
            pst.setBoolean(i++, data.isNotified());
            pst.setString(i++, data.getLot());
            LocalDate date=data.getDueDate1();
            if (date==null)
                pst.setNull(i++,Types.TIMESTAMP);
            else
                pst.setTimestamp(i++, Timestamp.valueOf(LocalDateTime.of(date,LocalTime.MIDNIGHT)));
            pst.setString(i++, data.getStatus());
            pst.setInt(i++, data.getCosts());
            pst.setInt(i++, data.getPositionX());
            pst.setInt(i++, data.getPositionY());
            pst.setString(i++, data.getPositionComment());
            pst.setInt(i, data.getId());
            pst.executeUpdate();
            pst.close();
        } finally {
            closeStatement(pst);
        }
        for (FileData file : data.getFiles()){
            FileBean.getInstance().saveFile(con, file, true);
        }
    }

    private static final String UPDATE_CONTENT_EXTRAS_SQL = "update t_defect " +
            "set assigned_id=?, notified=?, lot=?, due_date2=?, status =?, costs=? where id=? ";

    @Override
    public void updateContentExtras(Connection con, ContentData contentData) throws SQLException {
        if (!(contentData instanceof DefectData data))
            return;
        PreparedStatement pst = null;
        try {
            pst = con.prepareStatement(UPDATE_CONTENT_EXTRAS_SQL);
            int i = 1;
            pst.setInt(i++, data.getAssignedId());
            pst.setBoolean(i++, data.isNotified());
            pst.setString(i++, data.getLot());
            LocalDate date=data.getDueDate2();
            if (date==null)
                pst.setNull(i++,Types.TIMESTAMP);
            else
                pst.setTimestamp(i++, Timestamp.valueOf(LocalDateTime.of(date,LocalTime.MIDNIGHT)));
            pst.setString(i++, data.getStatus());
            pst.setInt(i++, data.getCosts());
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
    }

    private static final String UPDATE_CHANGE_SQL = "update t_content set change_date=?, changer_id=? where id=?";
    private static final String CLOSE_DEFECT_SQL = "update t_defect set close_date=? where id=?";

    public boolean closeDefect(DefectData data) {
        Connection con = startTransaction();
        PreparedStatement pst = null;
        try {
            data.setChangeDate(getServerTime(con));
            pst = con.prepareStatement(UPDATE_CHANGE_SQL);
            Timestamp now=getTimestamp(con);
            pst.setTimestamp(1,now);
            pst.setInt(2, data.getChangerId());
            pst.setInt(3,data.getId());
            pst.executeUpdate();
            pst.close();
            pst = con.prepareStatement(CLOSE_DEFECT_SQL);
            int i = 1;
            LocalDate date=data.getCloseDate();
            pst.setTimestamp(i++, Timestamp.valueOf(LocalDateTime.of(date,LocalTime.MIDNIGHT)));
            pst.setInt(i, data.getId());
            pst.executeUpdate();
            pst.close();
            return commitTransaction(con);
        } catch (SQLException e){
            return rollbackTransaction(con);
        } finally {
            closeStatement(pst);
            closeConnection(con);
        }
    }

    // project Ids

    private static final String GET_PROJECT_DEFECT_IDS_SQL = "SELECT id FROM t_defect  WHERE project_id=?";

    public List<Integer> getProjectDefectIds(int projectId) {
        Connection con = startTransaction();
        PreparedStatement pst = null;
        List<Integer> ids=new ArrayList<>();
        try {
            pst = con.prepareStatement(GET_PROJECT_DEFECT_IDS_SQL);
            pst.setInt(1,projectId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                ids.add(rs.getInt(1));
            }
            pst.close();
        } catch (SQLException e){
            e.printStackTrace();
        } finally {
            closeStatement(pst);
            closeConnection(con);
        }
        return ids;
    }

    private static final String GET_UNIT_DEFECT_IDS_SQL = "SELECT id FROM t_defect  WHERE unit_id=?";

    public List<Integer> getUnitDefectIds(int unitId) {
        Connection con = startTransaction();
        PreparedStatement pst = null;
        List<Integer> ids=new ArrayList<>();
        try {
            pst = con.prepareStatement(GET_UNIT_DEFECT_IDS_SQL);
            pst.setInt(1,unitId);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                ids.add(rs.getInt(1));
            }
            pst.close();
        } catch (SQLException e){
            e.printStackTrace();
        } finally {
            closeStatement(pst);
            closeConnection(con);
        }
        return ids;
    }

}
