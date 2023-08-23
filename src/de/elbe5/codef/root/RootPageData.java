/*
 Elbe 5 CMS - A Java based modular Content Management System
 Copyright (C) 2009-2020 Michael Roennau

 This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 You should have received a copy of the GNU General Public License along with this program; if not, see <http://www.gnu.org/licenses/>.
 */
package de.elbe5.codef.root;

import de.elbe5.content.ContentData;
import de.elbe5.request.ContentRequestKeys;
import de.elbe5.request.RequestData;
import jakarta.servlet.ServletException;
import jakarta.servlet.jsp.PageContext;
import java.io.IOException;
import java.io.Writer;

public class RootPageData extends ContentData {

    public RootPageData() {
    }

    public boolean hasUserReadRight(RequestData rdata) {
        return rdata.isLoggedIn();
    }

    // view

    @Override
    public String getContentDataJsp() {
        return "/WEB-INF/_jsp/root/editContentData.ajax.jsp";
    }

    public void displayContent(PageContext context, RequestData rdata) throws IOException, ServletException {
        Writer writer = context.getOut();
        writer.write("<div id=\"pageContent\" class=\"viewArea\">");
        context.include("/WEB-INF/_jsp/root/page.jsp");
        writer.write("</div>");
    }

    //used in jsp
    public void displayTreeContent(PageContext context, RequestData rdata) throws IOException, ServletException {
        if (hasUserReadRight(rdata)) {
            //backup
            ContentData currentContent=ContentData.getCurrentContent(rdata);
            rdata.setRequestObject(ContentRequestKeys.KEY_CONTENT, this);
            context.include("/WEB-INF/_jsp/root/treeContent.inc.jsp", true);
            //restore
            rdata.setRequestObject(ContentRequestKeys.KEY_CONTENT, currentContent);
        }
    }

}
