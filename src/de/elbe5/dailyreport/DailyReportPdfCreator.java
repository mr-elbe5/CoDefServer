/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.dailyreport;

import de.elbe5.base.BinaryFile;
import de.elbe5.base.DateHelper;
import de.elbe5.base.Log;
import de.elbe5.base.StringHelper;
import de.elbe5.company.CompanyCache;
import de.elbe5.company.CompanyData;
import de.elbe5.content.ContentCache;
import de.elbe5.file.CodefPdfCreator;
import de.elbe5.file.FileBean;
import de.elbe5.file.ImageData;
import de.elbe5.request.RequestData;
import de.elbe5.user.UserCache;
import de.elbe5.user.UserData;

import java.time.LocalDateTime;
import java.util.List;

public class DailyReportPdfCreator extends CodefPdfCreator {
    
    public BinaryFile getProjectDailyReport(int projectDiaryId, RequestData rdata){
        LocalDateTime now = LocalDateTime.now();
        DailyReport report = ContentCache.getContent(projectDiaryId, DailyReport.class);
        if (report==null || report.getProject() == null)
            return null;

        startXml();
        addTopHeader(sxml("_project") + ": " + xml(report.getProject().getDisplayName()));
        addSubHeader(sxml("_projectDailyReport") + " " + xml(report.getDisplayName()));

        startTable2Col();
        addLabeledContent(sxml("_location"),xml(report.getProject().getZipCode()) + " " + xml(report.getProject().getCity()));
        addLabeledContent(sxml("_reportNumber"),String.valueOf(report.getIdx()));
        addLabeledContent(sxml("_creationDate"),xml(report.getCreationDate()));
        UserData user= UserCache.getUser(report.getCreatorId());
        addLabeledContent(sxml("_creator"),xml(user.getName()));
        endTable2Col();

        addSubHeader(sxml("_weather"));

        startTable5Col();
        startTableRow();
        addTableCellBold(sxml("_time"));
        addTableCellBold(sxml("_weather"));
        addTableCellBold(sxml("_wind"));
        addTableCellBold(sxml("_temperature"));
        addTableCellBold(sxml("_humidity"));
        endTableRow();

        startTableRow();
        addTableCell(xml(DateHelper.toHtmlTime(report.getCreationDate().toLocalTime())));
        addTableCell(xml(report.getWeatherCoco()));
        addTableCell(xml(report.getWeatherWspd() + " " + report.getWeatherWdir() ));
        addTableCell(xml(report.getWeatherTemp()));
        addTableCell(xml(report.getWeatherRhum()));
        endTableRow();
        endTable5Col();

        addSubHeader(sxml("_participants"));

        if (!report.getCompanyBriefings().isEmpty()){
            startTable3Col();
            startTableRow();
            addTableCellBold(sxml("_company"));
            addTableCellBold(sxml("_activity"));
            addTableCellBold(sxml("_briefing"));
            endTableRow();
            for (CompanyBriefing briefing : report.getCompanyBriefings()){
                CompanyData company = CompanyCache.getCompany(briefing.getCompanyId());
                startTableRow();
                addTableCell(xml(company.getName()));
                addTableCell(xml(briefing.getActivity()));
                addTableCell(xml(briefing.getBriefing()));
                endTableRow();
            }
            endTable3Col();
        }

        List<ImageData> files = report.getFiles(ImageData.class);
        if (!files.isEmpty()) {
            startTable2Col();
            addLabeledContent(sxml("_images"),"");
            for (ImageData image : files) {
                BinaryFile file = FileBean.getInstance().getBinaryFile(image.getId());
                addLabeledImage("", file,"5.0cm");
            }
            endTable2Col();
        }



        addFooter(sxml("_projectDailyReport") + " " + xml(report.getDisplayName()) + " - " + xml(now));
        finishXml();
        String xml = getXml();
        //Log.log(xml);
        String fileName="dailyreport-" + StringHelper.toSafeWebFileName(report.getDisplayName()) + "-" + xml(report.getCreationDate()).replace(' ','-')+".pdf";
        return getPdf(xml, "_templates/pdf.xsl", fileName);
    }

}
