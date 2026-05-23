package com.bupt.ta.util;

import jakarta.servlet.ServletContext;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Simple file utilities for reading/writing UTF-8 text files and resolving webapp data file paths.
 */
public class FileUtil {
    /**
     * Read file content as UTF-8 string. Used for reading JSON files.
     */
    public static String readFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line);
            }
        }
        return content.toString();
    }

    /**
     * Write the given UTF-8 string to the specified file (overwrites).
     */
    public static void writeFile(String filePath, String content) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
            bw.write(content);
        }
    }

    /**
     * Resolve absolute path to /data/<fileName> inside the webapp (suitable for Tomcat deployments).
     */
    public static String getDataFilePath(String fileName, ServletContext servletContext) {
        return servletContext.getRealPath("/data/" + fileName);
    }

    /**
     * Safely close multiple Closeable streams; ignores and logs IOExceptions.
     */
    public static void closeStream(Closeable... streams) {
        for (Closeable stream : streams) {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}