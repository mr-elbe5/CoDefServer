/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.unit;

import de.elbe5.base.BinaryFile;
import de.elbe5.base.DateHelper;
import de.elbe5.file.CodefPdfCreator;
import de.elbe5.content.ContentCache;
import de.elbe5.request.RequestData;
import de.elbe5.user.CodefUserData;

import java.time.LocalDateTime;

public class UnitPdfCreator extends CodefPdfCreator {

    public BinaryFile getUnitReport(int unitId, RequestData rdata, boolean includeStatusChanges){
        LocalDateTime now = DateHelper.getCurrentTime();
        CodefUserData user = rdata.getLoginUser(CodefUserData.class);
        UnitData unit= ContentCache.getContent(unitId,UnitData.class);
        if (user==null || unit==null || unit.getProject()==null)
            return null;

        startXml();

        addTopHeader(sxml("_project") + ": " + xml(unit.getProject().getDisplayName()));

        addUnit(unit, user, includeStatusChanges);

        addFooter(sxml("_project") + " " + xml(unit.getProject().getDisplayName()) +
                ", " + sxml("_unit") + " " + xml(unit.getDisplayName()) +
                " - " + xml(now));
        finishXml();
        String xml = getXml();
        //Log.log(xml);
        String fileName="report-of-unit-defects-" + unit.getId() + "-" + xml(now).replace(' ','-')+".pdf";
        return getPdf(xml, "_templates/pdf.xsl", fileName);
    }

}
