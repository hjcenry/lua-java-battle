package com.hjc.util;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class FileUtil {

    public static byte[] getFileBytes(String path) throws Exception {

        File file = new File(path);
        if (!file.exists()) {
            return null;
        }

        byte[] bytes = null;
        try (FileInputStream inputStream = new FileInputStream(file);
             DataInputStream dataInputStream = new DataInputStream(inputStream);) {
            bytes = new byte[dataInputStream.available()];
            dataInputStream.read(bytes, 0, bytes.length);
        }
        return bytes;
    }

    public static String getFileContent(String path) throws Exception {
        return getFileContent(path, "utf-8");
    }

    public static String getFileContent(String path, String encoding) throws Exception {
        StringBuilder result = new StringBuilder();
        File file = new File(path);
        if (!file.exists()) {
            return "";
        }
        try (InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);
             BufferedReader bufferedReader = new BufferedReader(read);) {
            String s = "";
            while ((s = bufferedReader.readLine()) != null) {
                result.append(s);
            }
        }
        return result.toString();
    }

    /**
     * 每行读取文件内容
     *
     * @param path
     * @return
     * @throws Exception
     */
    public static List<String> getFileLinesContent(String path) throws Exception {
        List<String> contents = new ArrayList<>();
        File file = new File(path);
        if (!file.exists()) {
            return contents;
        }

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(path))) {
            String s = "";
            while ((s = bufferedReader.readLine()) != null) {
                contents.add(s);
            }
        }
        return contents;
    }

    public static List<String> getFileLinesContent(String path, String encoding) throws Exception {
        List<String> contents = new ArrayList<>();
        File file = new File(path);
        if (!file.exists()) {
            return contents;
        }
        InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);
        BufferedReader bufferedReader = new BufferedReader(read);

        String s = "";
        while ((s = bufferedReader.readLine()) != null) {
            contents.add(s);
        }
        bufferedReader.close();
        return contents;
    }

    public static String writeFileContent(String path, String content) throws Exception {
        StringBuilder result = new StringBuilder();
        File file = new File(path);
        if (!file.exists()) {
            file.createNewFile();
        } else {
            file.delete();
            file.createNewFile();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writer.write(content);
            writer.flush();
        }

        return result.toString();
    }

    /**
     * 按行向文件中写数据
     *
     * @param fileName
     * @param sqlStr
     * @param isAppend 是否追加写
     */
    public static void writeLineToFile(String fileName, String sqlStr, boolean isAppend) throws IOException {
        File file = new File(fileName);
        if (!file.exists()) {
            file.createNewFile(); // 不存在，创建
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, isAppend));) {
            writer.write(sqlStr + "\r\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取目录下的所有文件
     *
     * @param dirPath     目录路径
     * @param expectExt   期望后缀，查找全部文件（不要求后缀）可传null
     * @param isRecursion 是否递归子目录查找
     * @return dirPath下的所有文件
     */
    public static List<File> getAllFileInDir(String dirPath, String expectExt, boolean isRecursion) {
        File dirFile = new File(dirPath);
        if (!dirFile.exists()) {
            // 根目录就不存在
            return Collections.emptyList();
        }
        if (!dirFile.isDirectory()) {
            // 传进来的不是一个目录
            return Collections.emptyList();
        }
        List<File> fileList = new ArrayList<>();
        getFileInDir(fileList, dirFile, expectExt, isRecursion);
        return fileList;
    }

    private static void getFileInDir(List<File> fileList, File dirFile, String expectExt, boolean isRecursion) {
        for (File listFile : Objects.requireNonNull(dirFile.listFiles())) {
            File file = listFile.getAbsoluteFile();
            if (!file.exists()) {
                continue;
            }
            if (file.isFile()) {
                // 找到文件
                if (!StringUtil.isEmpty(expectExt)) {
                    String suffix = getFileExt(file);
                    if (expectExt.equals(suffix)) {
                        fileList.add(file);
                    }
                } else {
                    // 不要求后缀
                    fileList.add(file);
                }
                continue;
            }
            if (isRecursion && file.isDirectory()) {
                // 是目录：如果需要递归，递归继续查找
                getFileInDir(fileList, file, expectExt, isRecursion);
            }
        }
    }

    private static String getFileExt(File file) {
        // 要求特定后缀文件
        int lastIndexOf = file.getAbsolutePath().lastIndexOf(".");
        //获取文件的后缀
        return file.getAbsolutePath().substring(lastIndexOf);
    }

    /**
     * 找到特定文件
     *
     * @param filePath 文件路径
     * @return 特定文件
     */
    public static File findFile(String filePath) {
        return findFile(filePath, null);
    }

    /**
     * 找到特定文件
     *
     * @param filePath 文件路径
     * @param ext      文件后缀
     * @return 特定文件
     */
    public static File findFile(String filePath, String ext) {
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }
        if (!file.isFile()) {
            return null;
        }
        if (!StringUtil.isEmpty(ext) && !ext.equals(getFileExt(file))) {
            return null;
        }
        return file;
    }

    /**
     * 获取项目根路径
     *
     * @return 项目根路径
     */
    public static String getRootPath() {
        URL url = ClassLoader.getSystemClassLoader().getResource("");
        if (url == null) {
            return StringUtil.EMPTY;
        }
        return url.getPath();
    }
}
