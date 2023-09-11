package com.supcon.ses.dataupload.utils;

import com.supcon.ses.dataupload.exceptions.FileReadException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileHelper {

    private FileHelper() {

    }

    public static String readFileContent(String filePath) {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(filePath));
            return new String(bytes);
        } catch (IOException e) {
            // 处理读取文件异常
            throw new FileReadException("文件读取报错", e);
        }
    }

}
