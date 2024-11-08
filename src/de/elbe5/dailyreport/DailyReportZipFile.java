package de.elbe5.dailyreport;

import de.elbe5.base.BinaryFile;
import de.elbe5.base.DateHelper;
import de.elbe5.base.Log;
import de.elbe5.base.StringHelper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DailyReportZipFile {

    private final ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
    private final ZipOutputStream zipOut = new ZipOutputStream(byteOutput);

    public DailyReportZipFile(){
        zipOut.setMethod(ZipOutputStream.ENDSUB);
        zipOut.setLevel(9);
    }

    public boolean addFile(String fileName, byte[] bytes) {
        try {
            ZipEntry entry = new ZipEntry(fileName);
            zipOut.putNextEntry(entry);
            zipOut.write(bytes);
            zipOut.closeEntry();
            return true;
        }
        catch (IOException e) {
            Log.error("could not zip file", e);
            return false;
        }
    }

    public BinaryFile getZipFile(String unitName){
        try{
            zipOut.finish();
            BinaryFile file=new BinaryFile();
            String fileName="dailyreports-" + StringHelper.toSafeWebFileName(unitName) + ".zip";
            file.setFileName(fileName);
            file.setContentType("application/zip");
            zipOut.flush();
            file.setBytes(byteOutput.toByteArray());
            file.setFileSize(file.getBytes().length);
            return file;
        }
        catch (IOException e) {
            Log.error("could not create zip file", e);
            return null;
        }
    }

}
