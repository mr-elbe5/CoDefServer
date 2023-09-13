/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.company;

import de.elbe5.base.Log;

import java.util.*;

public class CompanyCache {

    private static int version = 1;
    private static boolean dirty = true;
    private static final Object lockObj = new Object();

    private static List<CompanyData> companyList = new ArrayList<>();
    private static Map<Integer, CompanyData> companyMap = new HashMap<>();

    public static synchronized void load() {
        CompanyBean bean = CompanyBean.getInstance();
        List<CompanyData> list = bean.getAllCompanies();
        Map<Integer, CompanyData> map = new HashMap<>();
        for (CompanyData company : list) {
            map.put(company.getId(), company);
        }
        companyList = list;
        companyMap = map;
        Log.info("company cache reloaded");
    }

    public static void setDirty() {
        increaseVersion();
        dirty = true;
    }

    public static void checkDirty() {
        if (dirty) {
            synchronized (lockObj) {
                if (dirty) {
                    load();
                    dirty = false;
                }
            }
        }
    }

    public static void increaseVersion() {
        version++;
    }

    public static int getVersion() {
        return version;
    }

    public static List<CompanyData> getAllCompanies(){
        checkDirty();
        return companyList;
    }

    public static List<CompanyData> getCompanies(Set<Integer> ids){
        checkDirty();
        List<CompanyData> list = new ArrayList<>();
        for (CompanyData company : companyList){
            if (ids.contains(company.getId())){
                list.add(company);
            }
        }
        return list;
    }

    public static CompanyData getCompany(int id) {
        checkDirty();
        return companyMap.get(id);
    }
}
