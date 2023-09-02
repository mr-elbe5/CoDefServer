/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.application;

import de.elbe5.defect.DefectComparator;
import de.elbe5.content.ContentCache;
import de.elbe5.defect.DefectData;
import de.elbe5.project.ProjectData;
import de.elbe5.request.RequestData;
import de.elbe5.unit.UnitData;

import java.util.ArrayList;
import java.util.List;

public class ViewFilter {

    public static ViewFilter getFilter(RequestData rdata){
        ViewFilter filter=rdata.getSessionObject("$filterData", ViewFilter.class);
        if (filter==null){
            filter=new ViewFilter();
            rdata.setSessionObject("$filterData",filter);
        }
        return filter;
    }

    private int currentUserId = 0;
    private boolean isEditor= false;
    private int projectId = 0;
    private List<Integer> watchedIds = new ArrayList<>();
    private boolean showClosed =false;

    private int sortType=DefectComparator.TYPE_CREATION;
    private boolean ascending = false;

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
    private final List<Integer> ownProjectIds=new ArrayList<>();

    public List<Integer> getOwnProjectIds() {
        return ownProjectIds;
    }

    public int getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(int currentUserId) {
        this.currentUserId = currentUserId;
    }

    public boolean isEditor() {
        return isEditor;
    }

    public void setEditor(boolean editor) {
        isEditor = editor;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public boolean hasProjectReadRight(int projectId){
        return isEditor || ownProjectIds.contains(projectId);
    }

    public List<Integer> getWatchedIds() {
        return watchedIds;
    }

    public void setWatchedIds(List<Integer> watchedIds) {
        this.watchedIds = watchedIds;
    }

    public void initWatchedCompanies(){
        watchedIds.clear();
        if (isEditor){
            ProjectData project=ContentCache.getContent(getProjectId(), ProjectData.class);
            if (project!=null) {
                watchedIds.addAll(project.getCompanyIds());
            }
        }
    }

    public boolean isShowClosed() {
        return showClosed;
    }

    public void setShowClosed(boolean showClosed) {
        this.showClosed = showClosed;
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
            if (!isEditor && data.getAssignedId()!=currentUserId){
                list.remove(i);
                continue;
            }
            if (!getWatchedIds().contains(data.getAssignedId())){
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
                if (!isEditor && data.getAssignedId()!=currentUserId){
                    list.remove(i);
                    continue;
                }
                if (!getWatchedIds().contains(data.getAssignedId())){
                    list.remove(i);
                }
            }
            defects.addAll(list);
        }
        DefectComparator.instance.sort(defects, sortType, ascending);
        return defects;
    }
}
