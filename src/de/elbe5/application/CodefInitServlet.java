/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.application;

import de.elbe5.administration.ContentAdminController;
import de.elbe5.base.LocalizedStrings;
import de.elbe5.base.Log;
import de.elbe5.defect.DefectController;
import de.elbe5.defectstatus.StatusChangeController;
import de.elbe5.project.ProjectController;
import de.elbe5.root.RootController;
import de.elbe5.unit.UnitController;
import de.elbe5.company.CompanyCache;
import de.elbe5.company.CompanyController;
import de.elbe5.content.*;
import de.elbe5.database.DbConnector;
import de.elbe5.file.*;
import de.elbe5.group.GroupController;
import de.elbe5.servlet.InitServlet;
import de.elbe5.timer.CleanupTaskData;
import de.elbe5.timer.HeartbeatTaskData;
import de.elbe5.timer.Timer;
import de.elbe5.timer.TimerController;
import de.elbe5.user.CodefUserController;
import de.elbe5.user.UserCache;
import de.elbe5.user.UserController;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

public class CodefInitServlet extends InitServlet {

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        System.out.println("initializing Codef Application...");
        ServletContext context=servletConfig.getServletContext();
        ApplicationPath.initializePath(ApplicationPath.getCatalinaAppDir(context), ApplicationPath.getCatalinaAppROOTDir(context));
        Configuration.setConfigs(context);
        LocalizedStrings.addBundle("bandika", Configuration.getLocale());
        LocalizedStrings.addBundle("application", Configuration.getLocale());
        Log.initLog(ApplicationPath.getAppName());
        if (!DbConnector.getInstance().initialize("jdbc/codef"))
            return;
        Configuration.setAppTitle("CoDef");
        Configuration.setLogContent(false);
        Configuration.setEmailMandatory(false);

        AdminController.register(new ContentAdminController());
        ContentController.register(new ContentController());
        DocumentController.register(new DocumentController());
        ImageController.register(new ImageController());
        GroupController.register(new GroupController());
        TimerController.register(new TimerController());
        CompanyController.register(new CompanyController());
        UserController.register(new CodefUserController());
        RootController.register(new RootController());
        ProjectController.register(new ProjectController());
        UnitController.register(new UnitController());
        DefectController.register(new DefectController());
        StatusChangeController.register(new StatusChangeController());

        CompanyCache.load();
        ContentCache.load();
        UserCache.load();
        Timer.getInstance().registerTimerTask(new HeartbeatTaskData());
        Timer.getInstance().registerTimerTask(new CleanupTaskData());
        Log.log("load tasks");
        Timer.getInstance().loadTasks();
        Timer.getInstance().startThread();
        Log.log("Codef initialized");
        //generatePassword();
    }

}
