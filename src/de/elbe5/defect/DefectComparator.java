/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.defect;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public class DefectComparator implements Comparator<DefectData> {

    public static final int TYPE_CREATION = 1;
    public static final int TYPE_CHANGER = 2;
    public static final int TYPE_CHANGE = 3;
    public static final int TYPE_DUE_DATE = 4;
    public static final int TYPE_CLOSE_DATE = 5;
    public static final int TYPE_UNIT = 6;
    public static final int TYPE_STATUS = 7;
    public static final int TYPE_ASSIGNED = 8;
    public static final int TYPE_DESCRIPTION = 9;
    public static final int TYPE_NOTIFIED = 10;
    public static final int TYPE_DEFECTPHASE = 11;
    public static final int TYPE_DEFECTTYPE = 12;

    public static DefectComparator instance = new DefectComparator();

    public void sort(List<DefectData> defects,int sortType, boolean ascending){
        this.sortType = sortType;
        this.ascending = ascending;
        defects.sort(this);
    }

    private int sortType=TYPE_CREATION;
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

    @Override
    public int compare(DefectData o1, DefectData o2) {
        int result=0;
        switch (sortType) {
            case TYPE_CREATION -> result = o1.getCreationDate().compareTo(o2.getCreationDate());
            case TYPE_CHANGER ->
                    result = o1.getChangerName().toLowerCase().compareTo(o2.getChangerName().toLowerCase());
            case TYPE_CHANGE -> result = o1.getChangeDate().compareTo(o2.getChangeDate());
            case TYPE_DUE_DATE -> result = compareLocalDates(o1.getDueDate(), o2.getDueDate());
            case TYPE_CLOSE_DATE -> result = compareLocalDates(o1.getCloseDate(), o2.getCloseDate());
            case TYPE_UNIT ->
                    result = o1.getUnit().getName().toLowerCase().compareTo(o2.getUnit().getName().toLowerCase());
            case TYPE_STATUS -> result = o1.getStatus().compareTo(o2.getStatus());
            case TYPE_ASSIGNED -> result = o1.getAssignedName().toLowerCase().compareTo(o2.getAssignedName().toLowerCase());
            case TYPE_NOTIFIED -> {
                if (o1.isNotified() != o2.isNotified())
                    result = o1.isNotified() ? 1 : -1;
            }
            case TYPE_DESCRIPTION ->
                    result = o1.getDescription().toLowerCase().compareTo(o2.getDescription().toLowerCase());
            case TYPE_DEFECTPHASE -> result = o1.getProjectPhase().compareTo(o2.getProjectPhase());
            case TYPE_DEFECTTYPE -> result = o1.isRemainingWork() ? 1 : -1;
        }
        return ascending ? result : -result;
    }

    private int compareLocalDates(LocalDate date1, LocalDate date2){
        if (date1==null && date2==null)
            return 0;
        if (date1==null)
            return -1;
        if (date2==null)
            return 1;
        return date1.compareTo(date2);
    }

}
