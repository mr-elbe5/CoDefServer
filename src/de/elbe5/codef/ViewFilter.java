/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.codef;

import de.elbe5.codef.defect.DefectComparator;
import de.elbe5.content.ContentCache;
import de.elbe5.codef.defect.DefectBean;
import de.elbe5.codef.defect.DefectData;
import de.elbe5.codef.project.ProjectData;
import de.elbe5.group.GroupBean;
import de.elbe5.group.GroupData;
import de.elbe5.request.RequestData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
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

    private DefectComparator comparator = new DefectComparator();

    private final List<Integer> ownProjectIds=new ArrayList<>();

    public List<Integer> getOwnProjectIds() {
        return ownProjectIds;
    }

    public int getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(int currentUserId) {
        this.currentUserId = currentUserId;
        comparator.setUserId(currentUserId);
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

    public DefectComparator getComparator() {
        return comparator;
    }

    public void initWatchedUsers(){
        watchedIds.clear();
        if (isEditor){
            ProjectData project=ContentCache.getContent(getProjectId(), ProjectData.class);
            if (project!=null) {
                GroupData group = GroupBean.getInstance().getGroup(project.getGroupId());
                if (group != null)
                    watchedIds.addAll(group.getUserIds());
            }

        }
        if (watchedIds.isEmpty()){
            watchedIds.add(currentUserId);
        }
    }

    public boolean isShowClosed() {
        return showClosed;
    }

    public void setShowClosed(boolean showClosed) {
        this.showClosed = showClosed;
    }

    public List<DefectData> getLocationDefects(int locationId){
        List<Integer> ids= DefectBean.getInstance().getUnitDefectIds(locationId);
        List<DefectData> list = ContentCache.getContents(DefectData.class);
        for (int i=list.size()-1;i>=0;i--){
            DefectData data=list.get(i);
            if (!ids.contains(data.getId())){
                list.remove(i);
                continue;
            }
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
        list.sort(comparator);
        return list;
    }

    public List<DefectData> getProjectDefects(){
        List<Integer> ids= DefectBean.getInstance().getProjectDefectIds(projectId);
        List<DefectData> list = ContentCache.getContents(DefectData.class);
        for (int i=list.size()-1;i>=0;i--){
            DefectData data=list.get(i);
            if (!ids.contains(data.getId())){
                list.remove(i);
                continue;
            }
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
        list.sort(comparator);
        return list;
    }
}
