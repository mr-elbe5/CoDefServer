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
import de.elbe5.project.ProjectPhase;
import de.elbe5.project.ProjectData;
import de.elbe5.unit.UnitData;

import java.util.ArrayList;
import java.util.List;

public class CodefUserData extends UserData{

    private int projectId = 0;
    private List<Integer> selectedProjectIds = new ArrayList<>();
    private List<Integer> selectedCompanyIds = new ArrayList<>();
    private boolean showOpen = true;
    private boolean showDisputed = true;
    private boolean showRejected = true;
    private boolean showDone = true;
    private boolean showClosed = true;
    private ProjectPhase projectPhase = null;
    private boolean showOnlyRemainingWork = false;

    //runtime

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

    public String getProjectIdsString() {
        return StringHelper.getIntString(selectedProjectIds);
    }

    public List<Integer> getSelectedProjectIds() {
        return selectedProjectIds;
    }

    public void setSelectedProjectIds(String projectIdsString) {
        selectedProjectIds = StringHelper.toIntList(projectIdsString);
    }

    public void setProjectIds(List<Integer> ids) {
        selectedProjectIds = ids;
    }

    public String getCompanyIdsString() {
        return StringHelper.getIntString(selectedCompanyIds);
    }

    public List<Integer> getSelectedCompanyIds() {
        return selectedCompanyIds;
    }

    public void setSelectedCompanyIds(String companyIdsString) {
        selectedCompanyIds = StringHelper.toIntList(companyIdsString);
    }

    public void setCompanyIds(List<Integer> ids) {
        selectedCompanyIds = ids;
    }

    public boolean isShowOpen() {
        return showOpen;
    }

    public void setShowOpen(boolean showOpen) {
        this.showOpen = showOpen;
    }

    public boolean isShowDisputed() {
        return showDisputed;
    }

    public void setShowDisputed(boolean showDisputed) {
        this.showDisputed = showDisputed;
    }

    public boolean isShowRejected() {
        return showRejected;
    }

    public void setShowRejected(boolean showRejected) {
        this.showRejected = showRejected;
    }

    public boolean isShowDone() {
        return showDone;
    }

    public void setShowDone(boolean showDone) {
        this.showDone = showDone;
    }

    public boolean isShowClosed() {
        return showClosed;
    }

    public void setShowClosed(boolean showClosed) {
        this.showClosed = showClosed;
    }

    public ProjectPhase getProjectPhase() {
        return projectPhase;
    }

    public String getProjectPhaseString() {
        return projectPhase == null ? "" : projectPhase.name();
    }

    public void setProjectPhase(ProjectPhase projectPhase) {
        this.projectPhase = projectPhase;
    }

    public boolean isShowOnlyRemainingWork() {
        return showOnlyRemainingWork;
    }

    public void setShowOnlyRemainingWork(boolean showOnlyRemainingWork) {
        this.showOnlyRemainingWork = showOnlyRemainingWork;
    }

    public void setProjectPhase(String projectPhaseString) {
        if (projectPhaseString.isEmpty()){
            projectPhase = null;
            return;
        }
        try{
            projectPhase = ProjectPhase.valueOf(projectPhaseString);
        }
        catch (IllegalArgumentException e){
            projectPhase = null;
        }
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

    public List<Integer> getAllowedProjectIds(){
        List<Integer> ids = new ArrayList<>();
        for (ProjectData project : ContentCache.getContents(ProjectData.class)){
            if (project.hasUserEditRight(this)){
                ids.add(project.getId());
            }
        }
        return ids;
    }

    public List<DefectData> getUnitDefects(int unitId){
        UnitData unit = ContentCache.getContent(unitId, UnitData.class);
        assert unit != null;
        List<DefectData> list = unit.getChildren(DefectData.class);
        for (int i=list.size()-1;i>=0;i--){
            DefectData data=list.get(i);
            switch (data.getStatus()){
                case OPEN -> {
                    if (!isShowOpen()) {
                        list.remove(i);
                        continue;
                    }
                }
                case DISPUTED -> {
                    if (!isShowDisputed()) {
                        list.remove(i);
                        continue;
                    }
                }
                case REJECTED -> {
                    if (!isShowRejected()) {
                        list.remove(i);
                        continue;
                    }
                }
                case DONE -> {
                    if (!isShowDone()) {
                        list.remove(i);
                        continue;
                    }
                }
            }
            if (!isShowClosed() && data.isClosed()){
                list.remove(i);
                continue;
            }
            if (isShowOnlyRemainingWork() && !data.isRemainingWork()){
                list.remove(i);
                continue;
            }
            if (getProjectPhase() != null && getProjectPhase() != data.getProjectPhase()) {
                list.remove(i);
                continue;
            }
            if (!getSelectedCompanyIds().contains(data.getLastAssignedId())){
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
                switch (data.getStatus()){
                    case OPEN -> {
                        if (!isShowOpen()) {
                            list.remove(i);
                            continue;
                        }
                    }
                    case DISPUTED -> {
                        if (!isShowDisputed()) {
                            list.remove(i);
                            continue;
                        }
                    }
                    case REJECTED -> {
                        if (!isShowRejected()) {
                            list.remove(i);
                            continue;
                        }
                    }
                    case DONE -> {
                        if (!isShowDone()) {
                            list.remove(i);
                            continue;
                        }
                    }
                }
                if (!isShowClosed() && data.isClosed()){
                    list.remove(i);
                    continue;
                }
                if (isShowOnlyRemainingWork() && !data.isRemainingWork()){
                    list.remove(i);
                    continue;
                }
                if (getProjectPhase() != null && getProjectPhase() != data.getProjectPhase()) {
                    list.remove(i);
                    continue;
                }
                if (!getSelectedCompanyIds().contains(data.getLastAssignedId())){
                    list.remove(i);
                }
            }
            defects.addAll(list);
        }
        DefectComparator.instance.sort(defects, sortType, ascending);
        return defects;
    }

}
