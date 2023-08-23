/*
 Bandika CMS - A Java based modular Content Management System
 Copyright (C) 2009-2021 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.company;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompanyCache {

    private static CompanyCache instance = null;

    public static CompanyCache getInstance() {
        if (instance == null) {
            instance = new CompanyCache();
        }
        return instance;
    }

    private static int version = 1;
    private static boolean dirty = true;
    private static final Object lockObj = new Object();

    private static Map<Integer, CompanyData> companyMap = new HashMap<>();

    public static synchronized void load() {
        CompanyBean bean = CompanyBean.getInstance();
        List<CompanyData> companyList = bean.getAllCompanies();
        Map<Integer, CompanyData> companies = new HashMap<>();
        for (CompanyData company : companyList) {
            companies.put(company.getId(), company);
        }
        companyMap = companies;
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

    public static CompanyData getCompany(int id) {
        checkDirty();
        return companyMap.get(id);
    }
}
