/*
package uz.uzcard.genesis;

import org.apache.commons.fileupload.disk.DiskFileItem;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileUtilsTest {
    public static CommonsMultipartFile getMultipartFile(String fileName) throws IOException {
        File file = new File(fileName);
        FileInputStream inputStream = new FileInputStream(file);
        byte[] bytes = inputStream.readAllBytes();
        inputStream.close();

        DiskFileItem dfi = new DiskFileItem("file", "image/png", true,
                file.getName(), 100000000, file);

        dfi.getOutputStream().write(bytes);
        CommonsMultipartFile multipartFile = new CommonsMultipartFile(dfi);
        return multipartFile;
    }
}*/
