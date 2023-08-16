/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.codef.root;

import de.elbe5.base.*;
import de.elbe5.codef.project.ProjectData;
import de.elbe5.company.CompanyBean;
import de.elbe5.company.CompanyData;
import de.elbe5.content.ContentCache;
import java.util.ArrayList;
import java.util.List;

public class RootData implements IJsonData {

    List<Integer> projectIds = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public JsonObject getJson() {
        List<CompanyData> companies = CompanyBean.getInstance().getAllCompanies();
        JsonArray jsCompanies = new JsonArray();
        for (CompanyData company : companies) {
            JsonObject jsCompany = company.getJson();
            jsCompanies.add(jsCompany);
        }
        JsonArray jsProjects = new JsonArray();
        for (int projectId : projectIds) {
            ProjectData project = ContentCache.getContent(projectId, ProjectData.class);
            if (project == null || !project.isActive())
                continue;
            JsonObject jsProject = project.getJsonRecursive();
            jsProjects.add(jsProject);
        }
        return new JsonObject()
                .add("companies", jsCompanies)
                .add("projects", jsProjects);
    }

}
