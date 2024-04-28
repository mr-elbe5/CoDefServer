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
import de.elbe5.base.StringHelper;
import de.elbe5.content.ContentCache;
import de.elbe5.file.CodefPdfCreator;
import de.elbe5.file.FileBean;
import de.elbe5.file.ImageData;
import de.elbe5.request.RequestData;
import de.elbe5.user.UserCache;
import de.elbe5.user.UserData;

import java.time.LocalDateTime;
import java.util.List;

public class ProjectDailyReportPdfCreator extends CodefPdfCreator {
    
    public BinaryFile getProjectDailyReport(int projectDiaryId, RequestData rdata){
        LocalDateTime now = LocalDateTime.now();
        ProjectDailyReport data = ContentCache.getContent(projectDiaryId, ProjectDailyReport.class);
        if (data==null)
            return null;
        addTopHeader(sxml("_projectDailyReport") + " " + xml(data.getDisplayName()));
        addLabeledContent(sxml("_project"),data.getProject().getDisplayName());
        addLabeledContent(sxml("_location"),xml(data.getProject().getZipCode()) + " " + data.getProject().getCity());
        addLabeledContent(sxml("_reportNumber"),String.valueOf(data.getIdx()));
        addLabeledContent(sxml("_creationDate"),html(data.getCreationDate()));
        UserData user= UserCache.getUser(data.getCreatorId());
        addLabeledContent(sxml("_creator"),user.getName());

        startTable5Col();
        startTableRow();
        addTableCellBold(sxml("_time"));
        addTableCellBold(sxml("_weather"));
        addTableCellBold(sxml("_wind"));
        addTableCellBold(sxml("_temperature"));
        addTableCellBold(sxml("_humidity"));
        endTableRow();
        startTableRow();
        addTableCell(DateHelper.toHtmlTime(data.getCreationDate().toLocalTime()));
        addTableCell(xml(data.getWeatherCoco()));
        addTableCell(xml(data.getWeatherWspd() + " " + data.getWeatherWdir() ));
        addTableCell(xml(data.getWeatherTemp()));
        addTableCell(xml(data.getWeatherRhum()));
        endTableRow();
        endTable5Col();

        List<ImageData> files = data.getFiles(ImageData.class);
        if (!files.isEmpty()) {
            addLabeledContent(sxml("_images"),"");
            for (ImageData image : files) {
                BinaryFile file = FileBean.getInstance().getBinaryFile(image.getId());
                addLabeledImage(image.getDisplayName(), file,"5.0cm");
            }
        }

        addFooter(sxml("_projectDailyReport") + " " + xml(data.getDisplayName()) + " - " + html(now));
        String fileName="dailyreport-" + StringHelper.toSafeWebFileName(data.getDisplayName()) + "-" + DateHelper.toHtml(data.getCreationDate()).replace(' ','-')+".pdf";
        return getPdf(finishXml(), "_templates/pdf.xsl", fileName);
    }

}
