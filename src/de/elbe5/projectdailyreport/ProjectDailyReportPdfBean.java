/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.projectdailyreport;

import de.elbe5.base.BinaryFile;
import de.elbe5.base.DateHelper;
import de.elbe5.base.LocalizedSystemStrings;
import de.elbe5.base.StringHelper;
import de.elbe5.content.ContentCache;
import de.elbe5.defect.DefectData;
import de.elbe5.file.CodefFopBean;
import de.elbe5.file.FileBean;
import de.elbe5.file.FileData;
import de.elbe5.file.ImageData;
import de.elbe5.request.RequestData;
import de.elbe5.user.CodefUserData;
import de.elbe5.user.UserCache;
import de.elbe5.user.UserData;

import java.time.LocalDateTime;
import java.util.List;

public class ProjectDailyReportPdfBean extends CodefFopBean {

    private static ProjectDailyReportPdfBean instance = null;

    public static ProjectDailyReportPdfBean getInstance() {
        if (instance == null) {
            instance = new ProjectDailyReportPdfBean();
        }
        return instance;
    }

    public BinaryFile getProjectDailyReport(int projectDiaryId, RequestData rdata){
        CodefUserData user = rdata.getLoginUser(CodefUserData.class);
        if (user==null)
            return null;
        LocalDateTime now = LocalDateTime.now();
        ProjectDailyReport report = ContentCache.getContent(projectDiaryId, ProjectDailyReport.class);
        if (report==null)
            return null;
        StringBuilder sb=new StringBuilder();
        sb.append("<root>");
        addReportHeaderXml(sb,report);
        addReportXml(sb, report);
        addReportFooterXml(sb,report,now);
        sb.append("</root>");
        //System.out.println(sb.toString());
        String fileName="dailyreport-" + StringHelper.toSafeWebFileName(report.getDisplayName()) + "-" + DateHelper.toHtml(report.getCreationDate()).replace(' ','-')+".pdf";
        return getPdf(sb.toString(), "_templates/pdf.xsl", fileName);
    }

    private void addReportXml(StringBuilder sb, ProjectDailyReport data) {
        sb.append("<dailyreport>");
        addLabeledContent(sb,sxml("_project"),data.getProject().getDisplayName());
        addLabeledContent(sb,sxml("_location"),xml(data.getProject().getZipCode()) + " " + data.getProject().getCity());
        addLabeledContent(sb,sxml("_reportNumber"),String.valueOf(data.getIdx()));
        addLabeledContent(sb,sxml("_creationDate"),html(data.getCreationDate()));
        UserData user= UserCache.getUser(data.getCreatorId());
        addLabeledContent(sb,sxml("_creator"),user.getName());

        sb.append("<weatherheader>")
        .append("<header1>").append(sxml("_time")).append("</header1>")
        .append("<header2>").append(sxml("_weather")).append("</header2>")
        .append("<header3>").append(sxml("_wind")).append("</header3>")
        .append("<header4>").append(sxml("_temperature")).append("</header4>")
        .append("<header5>").append(sxml("_humidity")).append("</header5>")
        .append("</weatherheader>")
        .append("<weatherdata>")
        .append("<content1>").append(DateHelper.toHtmlTime(data.getCreationDate().toLocalTime())).append("</content1>")
        .append("<content2>").append(xml(data.getWeatherCoco())).append("</content2>")
        .append("<content3>").append(xml(data.getWeatherWspd() + " " + data.getWeatherWdir() )).append("</content3>")
        .append("<content4>").append(xml(data.getWeatherTemp())).append("</content4>")
        .append("<content5>").append(xml(data.getWeatherRhum())).append("</content5>")
        .append("</weatherdata>");

        List<ImageData> files = data.getFiles(ImageData.class);
        if (!files.isEmpty()) {
            addLabeledContent(sb,sxml("_images"),"");
            for (ImageData image : files) {
                BinaryFile file = FileBean.getInstance().getBinaryFile(image.getId());
                addLabeledImage(sb, image.getDisplayName(), file,"5.0cm");
            }
        }
        sb.append("</dailyreport>");
    }

    private void addReportHeaderXml(StringBuilder sb, ProjectDailyReport diary) {
        sb.append("<projectheader><title>");
        sb.append(sxml("_projectDailyReport"));
        sb.append(" ");
        sb.append(xml(diary.getDisplayName()));
        sb.append("</title></projectheader>");
    }

    private void addReportFooterXml(StringBuilder sb, ProjectDailyReport diary, LocalDateTime now) {
        sb.append("<footer><docAndDate>");
        sb.append(sxml("_projectDailyReport")).append(" ").append(xml(diary.getDisplayName())).append(" - ").append(DateHelper.toHtml(now));
        sb.append("</docAndDate></footer>");
    }

}
