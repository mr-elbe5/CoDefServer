/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.root;

import de.elbe5.base.*;
import de.elbe5.project.ProjectData;
import de.elbe5.company.CompanyBean;
import de.elbe5.company.CompanyData;
import de.elbe5.content.ContentCache;
import de.elbe5.request.RequestData;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RootData implements IJsonData {

    List<ProjectData> projects = new ArrayList<>();
    List<CompanyData> companies = new ArrayList<>();

    public void init(List<Integer> projectIds){
        for (int projectId : projectIds) {
            ProjectData project = ContentCache.getContent(projectId, ProjectData.class);
            if (project != null && project.isActive())
                projects.add(project);
        }
        companies = CompanyBean.getInstance().getAllCompanies();
    }

    public JsonObject getJson() {
        JsonArray jsCompanies = new JsonArray();
        for (CompanyData company : companies) {
            JsonObject jsCompany = company.getJson();
            jsCompanies.add(jsCompany);
        }
        JsonArray jsProjects = new JsonArray();
        for (ProjectData project : projects) {
            JsonObject jsProject = project.getJsonRecursive();
            jsProjects.add(jsProject);
        }
        return new JsonObject()
                .add("companies", jsCompanies)
                .add("projects", jsProjects);
    }

    public void readRequestData(RequestData rdata){
        JSONArray jsProjects = rdata.getAttributes().get("projects", JSONArray.class);
        if (jsProjects != null){
            for (Object obj : jsProjects){
                if (obj instanceof JSONObject jsObj){
                    ProjectData project = new ProjectData();
                    project.fromJsonRecursive(jsObj);
                    if (project.hasValidData())
                        projects.add(project);
                }
            }
        }
        JSONArray jsCompanies = rdata.getAttributes().get("companies", JSONArray.class);
        if (jsCompanies != null){
            for (Object obj : jsCompanies){
                if (obj instanceof JSONObject jsObj){
                    CompanyData company = new CompanyData();
                    company.fromJsonRecursive(jsObj);
                    if (company.hasValidData())
                        companies.add(company);
                }
            }
        }
    }

}
