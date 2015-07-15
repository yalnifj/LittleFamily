package org.finlayfamily.littlefamily.data;

import android.content.Context;

import org.finlayfamily.littlefamily.util.ImageHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by kids on 7/14/15.
 */
public class ErrorLogger extends Thread {
    private Context context;
    private final String folderName = "logs";
    private String logLevel = "E";

    private boolean running = true;

    private static ErrorLogger instance;

    public static ErrorLogger getInstance(Context context) {
        if (instance==null) {
            instance = new ErrorLogger(context);
        }
        return instance;
    }

    private ErrorLogger(Context context) {
        this.context = context;
    }

    public void stopRunning() {
        running = false;
    }

    @Override
    public void run() {
        try {
            Process process = Runtime.getRuntime().exec("logcat -v threadtime");
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            File dataFolder = ImageHelper.getDataFolder(context);
            File folder = new File(dataFolder, folderName);
            if (!folder.exists()) {
                folder.mkdir();
            }
            else if (!folder.isDirectory()) {
                folder.delete();
                folder.mkdir();
            }
            Date date = new Date();
            DateFormat df = new SimpleDateFormat("yyyyMMdd");
            File logfile = new File(folder, "error_log_"+df.format(date)+".log");
            PrintWriter out = new PrintWriter(logfile);

            String line = "";
            while (running && (line = bufferedReader.readLine()) != null) {
                String[] parts = line.split("\\s+");
                if (parts.length>4) {
                    if (parts[4].equals(logLevel) || parts[4].equals("E"))
                        out.println(line);
                }
            }

        }
        catch (IOException e) {
        }
    }
}
