/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.root;

import de.elbe5.base.*;
import de.elbe5.company.CompanyCache;
import de.elbe5.content.ContentData;
import de.elbe5.project.ProjectData;
import de.elbe5.company.CompanyData;
import de.elbe5.content.ContentCache;
import de.elbe5.request.RequestData;
import jakarta.servlet.ServletException;
import jakarta.servlet.jsp.PageContext;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class RootData extends ContentData {

    public boolean hasUserReadRight(RequestData rdata) {
        return rdata.isLoggedIn();
    }

    // view

    @Override
    public String getBackendContentTreeJsp() {
        return "/WEB-INF/_jsp/root/backendTreeContent.inc.jsp";
    }

    @Override
    public String getBackendEditJsp() {
        return "/WEB-INF/_jsp/root/editBackendContent.ajax.jsp";
    }

    @Override
    public String getFrontendContentTreeJsp() {
        return "/WEB-INF/_jsp/root/frontendTreeContent.inc.jsp";
    }

    public void displayContent(PageContext context, RequestData rdata) throws IOException, ServletException {
        Writer writer = context.getOut();
        writer.write("<div id=\"pageContent\" class=\"viewArea\">");
        context.include("/WEB-INF/_jsp/root/page.jsp");
        writer.write("</div>");
    }

    @SuppressWarnings("unchecked")
    public JsonObject getAllDataJson(RequestData rdata) {
        List<ProjectData> projects = new ArrayList<>();
        for (ProjectData project : ContentCache.getContents(ProjectData.class)){
            if (project != null && project.isActive() && project.hasUserReadRight(rdata.getLoginUser()))
                projects.add(project);
        }
        List<CompanyData> companies = CompanyCache.getAllCompanies();
        JSONArray jsCompanies = new JSONArray();
        for (CompanyData company : companies) {
            JsonObject jsCompany = company.getJson();
            jsCompanies.add(jsCompany);
        }
        JSONArray jsProjects = new JSONArray();
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
                    if (project.hasValidData()) {
                        //todo
                    }
                }
            }
        }
        JSONArray jsCompanies = rdata.getAttributes().get("companies", JSONArray.class);
        if (jsCompanies != null){
            for (Object obj : jsCompanies){
                if (obj instanceof JSONObject jsObj){
                    CompanyData company = new CompanyData();
                    company.fromJsonRecursive(jsObj);
                    if (company.hasValidData()) {
                        //todo
                    }
                }
            }
        }
    }

}
