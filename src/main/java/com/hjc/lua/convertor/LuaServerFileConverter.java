package com.hjc.lua.convertor;

import com.hjc.lua.annotation.LuaServerLib;
import com.hjc.util.StringUtil;
import org.apache.commons.collections4.KeyValue;
import org.apache.commons.collections4.MapUtils;
import org.luaj.vm2.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author hejincheng
 * @version 1.0
 * @date 2022/2/18 14:54
 **/
public class LuaServerFileConverter {

    /**
     * 模板文件路径
     */
    protected static String templateFilePath;
    /**
     * lua根路径
     */
    protected static String luaLoadRootPath;
    /**
     * Java扫描路径
     */
    protected static String javaScanPackage;
    /**
     * ServerLib文件路径
     */
    protected static String serverLibFilePath;

    static void initParams() {
        // 用户自定义lua地址了
        String customLuaRootPath = System.getProperty("luaRootPath");
        if (!StringUtil.isEmpty(customLuaRootPath)) {
            luaLoadRootPath = customLuaRootPath;
        } else {
            luaLoadRootPath = "C:\\Lua\\";
        }
        // 自定义模板文件目录
        String customTemplateFilePath = System.getProperty("templateFilePath");
        if (!StringUtil.isEmpty(customTemplateFilePath)) {
            templateFilePath = customTemplateFilePath;
        } else {
            templateFilePath = Objects.requireNonNull(LuaServerFileConverter.class.getResource("/")).getPath();
        }
        // 自定义模板文件目录
        String customJavaScanPackage = System.getProperty("javaScanPackage");
        if (!StringUtil.isEmpty(customJavaScanPackage)) {
            javaScanPackage = customJavaScanPackage;
        } else {
            javaScanPackage = "com.hjc";
        }
        // 自定义模板文件目录
        String customServerLibFilePath = System.getProperty("serverLibFilePath");
        if (!StringUtil.isEmpty(customServerLibFilePath)) {
            serverLibFilePath = customServerLibFilePath;
        } else {
            serverLibFilePath = "Lib\\Server\\";
        }
    }

    /**
     * 读文件
     *
     * @param templateFile 模板文件
     * @param luaFileName  lua文件名
     * @return 读取字符串
     */
    public static StringBuilder readFile(String templateFile, String luaFileName) {
        File file = new File(templateFile);
        if (!file.exists()) {
            System.err.println(file.getAbsolutePath() + " not exist");
            return null;
        }

        String s;
        StringBuilder builder = new StringBuilder();
        Date nowDate = Calendar.getInstance().getTime();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            while ((s = br.readLine()) != null) {
                s = s.replace("${USER}", System.getProperty("user.name"));
                s = s.replace("${DATE}", new SimpleDateFormat("yyyy-MM-dd").format(nowDate));
                s = s.replace("${TIME}", new SimpleDateFormat("HH:mm:ss").format(nowDate));
                s = s.replace("${NAME}", luaFileName);
                builder.append(s).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return builder;
    }

    /**
     * 写文件
     *
     * @param content 内容
     * @param luaFile 文件
     */
    public static void writeFile(String content, String luaFile) {
        File file = new File(luaFile);
        if (file.exists()) {
            if (!file.delete()) {
                return;
            }
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(content);
            System.out.println("Generate File " + file.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("[" + luaFile + "]写入文件错误，请检查参数luaRootPath或serverLibFilePath（生成ServerLib时使用）是否正确设置");
            e.printStackTrace();
        }
    }

    /**
     * 获取lua方法的文本
     *
     * @param luaFileName  文件名
     * @param functionName 方法名
     * @param paramTypeMap 参数类型 <参数名，<参数类型,参数注释>>
     * @param returnType   返回类型
     * @param funcComment  方法注释
     * @return lua方法文本
     */
    public static String getLuaFunctionString(String luaFileName, String functionName, LinkedHashMap<String, KeyValue<Class<?>, String>> paramTypeMap, Class<?> returnType, String funcComment) {
        // 方法注释
        StringBuilder funcCommentBuilder = new StringBuilder();
        if (!StringUtil.isEmpty(funcComment)) {
            funcCommentBuilder.append(funcComment);
        }
        // 参数名字
        StringBuilder paramNameBuilder = new StringBuilder("(");
        // 参数类型
        StringBuilder paramCommentBuilder = new StringBuilder();

        if (!MapUtils.isEmpty(paramTypeMap)) {
            for (Map.Entry<String, KeyValue<Class<?>, String>> entry : paramTypeMap.entrySet()) {
                String paramName = entry.getKey();
                Class<?> type = entry.getValue().getKey();
                String parComment = entry.getValue().getValue();

                if (paramCommentBuilder.length() > 0) {
                    paramCommentBuilder.append("\n");
                }
                paramCommentBuilder.append(String.format("---@param %s %s %s", entry.getKey(), getLuaTypeString(type), parComment));

                if (paramNameBuilder.length() > 1) {
                    paramNameBuilder.append(", ");
                }
                paramNameBuilder.append(paramName);
            }
        }

        paramNameBuilder.append(")");

        String functionString = "";
        if (!StringUtil.isEmpty(funcCommentBuilder.toString())) {
            functionString += "-- " + funcCommentBuilder + "\n";
        } else {
            functionString += "-- \n";
        }
        if (!StringUtil.isEmpty(paramCommentBuilder.toString())) {
            functionString += paramCommentBuilder + "\n";
        }
        functionString += "---@type function\n";

        if (returnType != null) {
            functionString += "---@return " + getLuaTypeString(returnType) + "\n";
        }

        functionString += "---@public\n" +
                "function " + luaFileName + ":" + functionName +
                paramNameBuilder + "\n" +
                "    return\n" +
                "end\n";
        return functionString;
    }

    public static String getLuaCtorFunctionString(String luaFileName, LinkedHashMap<String, Class<?>> fieldTypes) {
        if (MapUtils.isEmpty(fieldTypes)) {
            return "";
        }
        StringBuilder fieldParamBuilder = new StringBuilder("(");
        StringBuilder fieldBuilder = new StringBuilder();
        for (Map.Entry<String, Class<?>> entry : fieldTypes.entrySet()) {
            String name = entry.getKey();

            // 赋值语句
            fieldBuilder.append("    self.").append(name).append(" = ").append("_").append(name).append("\n");

            // 参数
            if (fieldParamBuilder.length() > 1) {
                fieldParamBuilder.append(", ");
            }
            fieldParamBuilder.append("_").append(name);
        }
        fieldParamBuilder.append(")");

        String functionString = "function " + luaFileName + ":ctor" +
                fieldParamBuilder + "\n";
        if (!StringUtil.isEmpty(fieldBuilder.toString())) {
            functionString += fieldBuilder;
        }
        functionString += "end\n";
        return functionString;
    }

    public static String getLuaClassFieldCommentString(LinkedHashMap<String, Class<?>> fieldTypes, LinkedHashMap<String, String> fieldComments) {
        if (MapUtils.isEmpty(fieldTypes)) {
            return "\n";
        }
        StringBuilder fieldCommentBuilder = new StringBuilder();
        for (Map.Entry<String, Class<?>> entry : fieldTypes.entrySet()) {
            String name = entry.getKey();
            String type = getLuaTypeString(entry.getValue());
            String comment = fieldComments.get(name);

            // 注释
            fieldCommentBuilder.append("---@field ").append(name).append(" ").append(type);
            if (StringUtil.isNotEmpty(comment)) {
                fieldCommentBuilder.append(" ").append(comment);
            }
            fieldCommentBuilder.append("\n");
        }

        return fieldCommentBuilder.toString();
    }

    public static String getLuaTypeString(Class<?> type) {
        if (type == null) {
            return "nil";
        }
        if (type.equals(Integer.class) || type.equals(int.class)
                || type.equals(Long.class) || type.equals(long.class)
                || type.equals(Float.class) || type.equals(float.class)
                || type.equals(Double.class) || type.equals(double.class)
                || type.equals(LuaNumber.class)
        ) {
            return "number";
        }
        if (type.equals(Boolean.class) || type.equals(boolean.class) || type.equals(LuaBoolean.class)) {
            return "boolean";
        }
        if (type.equals(String.class) || type.equals(Character.class) || type.equals(char.class) || type.equals(LuaString.class)) {
            return "string";
        }
        if (type.equals(LuaTable.class)) {
            return "table";
        }
        if (type.equals(LuaValue.class)) {
            return "any";
        }
        if (type.equals(List.class)) {
            return "table";
        }
        if (type.isArray()) {
            return getLuaTypeString(type.getComponentType()) + "[]";
        }
        LuaServerLib luaServerLib = type.getAnnotation(LuaServerLib.class);
        if (luaServerLib != null) {
            return StringUtil.isEmpty(luaServerLib.className()) ? type.getSimpleName() : luaServerLib.className();
        } else {
            return type.getSimpleName();
        }
    }

}