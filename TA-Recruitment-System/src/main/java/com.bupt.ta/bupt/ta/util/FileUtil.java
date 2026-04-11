package com.bupt.ta.util;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileUtil {
    // 读取文件内容为字符串（适配JSON文件读取）
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

    // 写入字符串到文件（适配JSON文件写入）
    public static void writeFile(String filePath, String content) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
            bw.write(content);
        }
    }

    // 获取项目webapp/data目录的绝对路径（适配Tomcat部署）
    public static String getDataFilePath(String fileName, ServletContext servletContext) {
        return servletContext.getRealPath("/data/" + fileName);
    }

    // 安全关闭流
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