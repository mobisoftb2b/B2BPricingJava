package com.mobisale.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    public static void error(String data)
    {
        appendUsingFileWriter("ERROR" + getCurrentTimeStamp() + " " + data + System.lineSeparator());
    }

    public static void error(Exception e)
    {
        appendUsingFileWriter("ERROR" + getCurrentTimeStamp() + " " + e.getMessage() + System.lineSeparator());
    }
    public static void info(String data)
    {
        appendUsingFileWriter("INFO" + getCurrentTimeStamp() + " " + data + System.lineSeparator());
    }

    public static void warn(String data)
    {
        appendUsingFileWriter("WARN" + getCurrentTimeStamp() + " " + data + System.lineSeparator());
    }
    private static String getCurrentTimeStamp() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    private static void createFile(String relativePath) {
        try {
            File file = new File(relativePath);

            if (!file.exists()) {
                if (file.createNewFile()) {
                    System.out.println(relativePath + " File Created in Project root directory");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getCurrentFilePath()
    {
        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(System.currentTimeMillis());
        String todayDate = formatter.format(date);
        String relativePath = "logs/pricing-"+ todayDate+".log";
        return relativePath;
    }

    private static void writeUsingFiles(String data) {
        try {
            String relativePath = getCurrentFilePath();
            createFile(relativePath);
            Files.write(Paths.get(relativePath), data.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void appendUsingFileWriter(String text) {
        String relativePath = getCurrentFilePath();
        createFile(relativePath);
        File file = new File(relativePath);
        FileWriter fr = null;
        try {
            // Below constructor argument decides whether to append or override
            fr = new FileWriter(file, true);
            fr.write(text);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
