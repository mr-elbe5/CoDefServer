/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.projectdiary;

import de.elbe5.base.BinaryFile;
import de.elbe5.base.DateHelper;
import de.elbe5.content.ContentCache;
import de.elbe5.file.CodefFopBean;
import de.elbe5.request.RequestData;
import de.elbe5.user.CodefUserData;

import java.time.LocalDateTime;

public class ProjectDiaryPdfBean extends CodefFopBean {

    private static ProjectDiaryPdfBean instance = null;

    public static ProjectDiaryPdfBean getInstance() {
        if (instance == null) {
            instance = new ProjectDiaryPdfBean();
        }
        return instance;
    }

    public BinaryFile getProjectDiary(int projectDiaryId, RequestData rdata){
        CodefUserData user = rdata.getLoginUser(CodefUserData.class);
        if (user==null)
            return null;
        LocalDateTime now = LocalDateTime.now();
        ProjectDiary diary= ContentCache.getContent(projectDiaryId,ProjectDiary.class);
        if (diary==null)
            return null;
        StringBuilder sb=new StringBuilder();
        sb.append("<root>");
        addDiaryHeaderXml(sb,diary);

        addDiaryFooterXml(sb,diary,now);
        sb.append("</root>");
        //System.out.println(sb.toString());
        String fileName="diary-" + diary.getId() + "-" + DateHelper.toHtml(now).replace(' ','-')+".pdf";
        return getPdf(sb.toString(), "_templates/pdf.xsl", fileName);
    }

    private void addDiaryHeaderXml(StringBuilder sb, ProjectDiary diary) {
        sb.append("<projectheader><title>");
        sb.append(sxml("_reports"));
        sb.append(": ");
        sb.append(xml(diary.getDisplayName()));
        sb.append("</title></projectheader>");
    }

    private void addDiaryFooterXml(StringBuilder sb, ProjectDiary diary, LocalDateTime now) {
        sb.append("<footer><docAndDate>");
        sb.append(sxml("_project")).append(" ").append(xml(diary.getDisplayName())).append(" - ").append(DateHelper.toHtml(now));
        sb.append("</docAndDate></footer>");
    }

}
