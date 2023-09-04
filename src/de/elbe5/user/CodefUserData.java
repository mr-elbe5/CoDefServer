/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.user;

import de.elbe5.base.StringHelper;
import de.elbe5.content.ContentCache;
import de.elbe5.defect.DefectComparator;
import de.elbe5.defect.DefectData;
import de.elbe5.project.ProjectData;
import de.elbe5.rights.GlobalRight;
import de.elbe5.unit.UnitData;

import java.util.ArrayList;
import java.util.List;

public class CodefUserData extends UserData{

    private int projectId = 0;
    private List<Integer> companyIds = new ArrayList<>();
    private boolean showClosed = true;
    private boolean showPreapprove = true;
    private boolean showLiability = true;

    //runtime

    private final List<Integer> ownProjectIds=new ArrayList<>();
    private int sortType= DefectComparator.TYPE_CREATION;
    private boolean ascending = false;

    public CodefUserData(){
    }

    public UserBean getBean() {
        return CodefUserBean.getInstance();
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getCompanyIdsString() {
        return StringHelper.getIntString(companyIds);
    }

    public List<Integer> getCompanyIds() {
        return companyIds;
    }

    public void setCompanyIds(String companyIdsString) {
        companyIds = StringHelper.toIntList(companyIdsString);
    }

    public void setCompanyIds(List<Integer> ids) {
        companyIds = ids;
    }

    public boolean isShowClosed() {
        return showClosed;
    }

    public void setShowClosed(boolean showClosed) {
        this.showClosed = showClosed;
    }

    public boolean isShowPreapprove() {
        return showPreapprove;
    }

    public void setShowPreapprove(boolean showPreapprove) {
        this.showPreapprove = showPreapprove;
    }

    public boolean isShowLiability() {
        return showLiability;
    }

    public void setShowLiability(boolean showLiability) {
        this.showLiability = showLiability;
    }

    public int getSortType() {
        return sortType;
    }

    public void setSortType(int sortType) {
        if (this.sortType==sortType)
            ascending=!ascending;
        else {
            this.sortType = sortType;
            ascending = true;
        }
    }

    public boolean isAscending() {
        return ascending;
    }

    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    public List<Integer> getOwnProjectIds() {
        return ownProjectIds;
    }

    public List<DefectData> getUnitDefects(int unitId){
        UnitData unit = ContentCache.getContent(unitId, UnitData.class);
        assert unit != null;
        List<DefectData> list = unit.getChildren(DefectData.class);
        for (int i=list.size()-1;i>=0;i--){
            DefectData data=list.get(i);
            if (!showClosed && data.isClosed()){
                list.remove(i);
                continue;
            }
            if (!getCompanyIds().contains(data.getAssignedId())){
                list.remove(i);
            }
        }
        DefectComparator.instance.
                sort(list, sortType, ascending);
        return list;
    }

    public List<DefectData> getProjectDefects(int projectId){
        ProjectData project = ContentCache.getContent(projectId, ProjectData.class);
        assert project != null;
        List<DefectData> defects = new ArrayList<>();
        List<UnitData> units = project.getChildren(UnitData.class);
        for (UnitData unit : units){
            List<DefectData> list = unit.getChildren(DefectData.class);
            for (int i=list.size()-1;i>=0;i--){
                DefectData data=list.get(i);
                if (!showClosed && data.isClosed()){
                    list.remove(i);
                    continue;
                }
                if (!getCompanyIds().contains(data.getAssignedId())){
                    list.remove(i);
                }
            }
            defects.addAll(list);
        }
        DefectComparator.instance.sort(defects, sortType, ascending);
        return defects;
    }

}
