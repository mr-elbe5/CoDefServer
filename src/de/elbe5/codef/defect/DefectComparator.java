/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.codef.defect;

import java.time.LocalDate;
import java.util.Comparator;

public class DefectComparator implements Comparator<DefectData> {

    public static final int TYPE_MY = 0;
    public static final int TYPE_CREATION = 1;
    public static final int TYPE_CHANGER = 2;
    public static final int TYPE_CHANGE = 3;
    public static final int TYPE_DUE_DATE = 4;
    public static final int TYPE_CLOSE_DATE = 5;
    public static final int TYPE_UNIT = 6;
    public static final int TYPE_STATE = 7;
    public static final int TYPE_ASSIGNED = 8;
    public static final int TYPE_DESCRIPTION = 9;
    public static final int TYPE_NOTIFIED = 10;


    private int sortType=TYPE_CREATION;
    private boolean ascending = false;
    private int userId = 0;

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

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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
                    result = o1.getUnitName().toLowerCase().compareTo(o2.getUnitName().toLowerCase());
            case TYPE_STATE -> result = o1.getState().compareTo(o2.getState());
            case TYPE_ASSIGNED -> {
                if (o1.getAssignedId() == userId && o2.getAssignedId() == userId)
                    // 0
                    break;
                else if (o1.getAssignedId() == userId)
                    result = -1;
                else if (o2.getAssignedId() == userId)
                    result = 1;
                else
                    result = o1.getAssignedName().toLowerCase().compareTo(o2.getAssignedName().toLowerCase());
            }
            case TYPE_NOTIFIED -> {
                if (o1.isNotified() != o2.isNotified())
                    result = o1.isNotified() ? 1 : -1;
            }
            case TYPE_DESCRIPTION ->
                    result = o1.getDescription().toLowerCase().compareTo(o2.getDescription().toLowerCase());
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
