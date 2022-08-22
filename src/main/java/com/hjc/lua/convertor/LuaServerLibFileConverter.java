package com.hjc.lua.convertor;

import com.google.common.collect.Lists;
import com.hjc.lua.annotation.LuaParam;
import com.hjc.lua.annotation.LuaServerLib;
import com.hjc.lua.annotation.LuaServerLibFunc;
import com.hjc.util.ScanUtil;
import com.hjc.util.StringUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.KeyValue;
import org.apache.commons.collections4.keyvalue.DefaultKeyValue;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 生成lua调用java的静态方法的lua工具类
 *
 * <p>
 * 使用方法：</br>
 * <li>1. 把需要Lua调用的Java类加上注解{@link LuaServerLib}</li>
 * <li>2. 把Java类中需要生成的方法加上注解{@link LuaServerLibFunc}</li>
 * <li>3. 对方法中所有的参数加上注解{@link LuaParam}</li>
 * <li>4.运行这个类main方法，加上VM启动参数-DluaRootPath指定lua路径</li>
 *     <ul>
 *     <li>-DluaRootPath=lua项目根路径</li>
 *     <li>-DtemplateFilePath=模板文件路径（默认取框架自带模板）</li>
 *     <li>-DjavaScanPackage=要扫描的Java包路径（默认com.hjc）</li>
 *     <li>-DserverLibFilePath=ServerLib文件路径（默认Lib\\Server）</li>
 *     </ul>
 * <li>5.同时生成lua文件，并更新ServerLib.lua文件中的引用</li>
 * </p>
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/2/18 11:47
 **/
public class LuaServerLibFileConverter extends LuaServerFileConverter {

    private static String templateFile = "ServerLibLuaFile.template";

    private static String serverLibTemplateFile = "ServerLib.template";

    /**
     * ServerLib文件名
     */
    private static final String SERVER_LIB_FILE_NAME = "ServerLib";

    public static void main(String[] args) {
        initParams();
        templateFile = templateFilePath + templateFile;
        serverLibTemplateFile = templateFilePath + serverLibTemplateFile;

        // 找出所有ServerLib的文件
        boolean findFiles = false;
        List<Class<?>> serverLibClassList = Lists.newArrayList();
        for (Class<?> javaClass : ScanUtil.getClasses(javaScanPackage)) {
            LuaServerLib luaServerLib = javaClass.getAnnotation(LuaServerLib.class);
            if (luaServerLib == null) {
                continue;
            }
            findFiles = true;
            if (luaServerLib.addToServerLib()) {
                serverLibClassList.add(javaClass);
            }

            String javaSimpleName = javaClass.getSimpleName();
            String className = luaServerLib.className();
            String fileDir = luaServerLib.fileDir();
            String comment = luaServerLib.comment();

            // 默认类名
            className = StringUtil.isEmpty(className) ? javaSimpleName : className;
            // 文件路径
            fileDir = StringUtil.isEmpty(fileDir) ? templateFilePath : luaLoadRootPath + File.separator + fileDir;

            // 生成lua文件
            generateLuaFile(javaClass, fileDir, className, comment);
        }

        if (!findFiles) {
            System.out.println(String.format("包目录[%s]没有找到@LuaServerLib的文件", javaScanPackage));
            return;
        }

        genServerLibLuaFile(serverLibClassList);
    }

    /**
     * 生成ServerLib.lua
     *
     * @param serverLibClassList 类list
     */
    public static void genServerLibLuaFile(List<Class<?>> serverLibClassList) {
        if (CollectionUtils.isEmpty(serverLibClassList)) {
            return;
        }
        StringBuilder templateBuilder = readFile(serverLibTemplateFile, SERVER_LIB_FILE_NAME);
        if (templateBuilder == null) {
            return;
        }

        StringBuilder fieldsCommentBuilder = new StringBuilder();
        StringBuilder fieldsBuilder = new StringBuilder();

        for (Class<?> javaClass : serverLibClassList) {
            LuaServerLib luaServerLib = javaClass.getAnnotation(LuaServerLib.class);
            String javaSimpleName = javaClass.getSimpleName();
            String className = luaServerLib.className();
            String fieldName = luaServerLib.fieldName();
            // 默认类名
            className = StringUtil.isEmpty(className) ? javaSimpleName : className;
            // 默认类名首字母小写
            fieldName = StringUtil.isEmpty(fieldName) ? javaSimpleName.substring(0, 1).toLowerCase() + javaSimpleName.substring(1) : fieldName;

            fieldsCommentBuilder.append("---@field ").append(fieldName).append(" ").append(className).append("\n");
            fieldsBuilder.append("    self.").append(fieldName).append(" = ").append("luajava.bindClass(\"").append(javaClass.getName()).append("\")").append("\n");
        }

        String luaFile = templateBuilder.toString().replace("${SELF_FIELDS}", fieldsBuilder.toString());
        luaFile = luaFile.replace("${FIELDS_COMMENT}", fieldsCommentBuilder.toString());

        // 写文件
        writeFile(luaFile, luaLoadRootPath + File.separator + serverLibFilePath + File.separator + SERVER_LIB_FILE_NAME + ".lua");
    }

    /**
     * 生成类lua文件
     *
     * @param javaClz
     * @param fileDir
     * @param className
     * @param comment
     */
    public static void generateLuaFile(Class<?> javaClz, String fileDir, String className, String comment) {
        StringBuilder templateBuilder = readFile(templateFile, className);
        if (templateBuilder == null) {
            return;
        }

        String template = templateBuilder.toString();

        Class<?> superClass = javaClz.getSuperclass();
        LuaServerLib superLib = superClass.getAnnotation(LuaServerLib.class);
        if (superLib != null) {
            String superClassName = superLib.className();
            superClassName = StringUtil.isEmpty(superClassName) ? superClass.getSimpleName() : superClassName;

            template = template.replace("${SUPER_CLASS}", superClassName);
            template = template.replace("${SUPER_CLASS_COMMENT}", superClassName);
        } else {
            template = template.replace("${SUPER_CLASS}", "nil");
            template = template.replace("${SUPER_CLASS_COMMENT}", "table");
        }

        // 注释
        if (!StringUtil.isEmpty(comment)) {
            template = template.replace("${COMMENT}", "---\n--- " + comment + "\n");
        } else {
            template = template.replace("${COMMENT}", "");
        }

        // 方法
        StringBuilder functionBuilder = new StringBuilder();
        for (Method method : javaClz.getDeclaredMethods()) {
            LuaServerLibFunc luaServerLibFunc = method.getAnnotation(LuaServerLibFunc.class);
            if (luaServerLibFunc == null) {
                continue;
            }

            System.out.println("method : \t" + method.getName());

            LinkedHashMap<String, KeyValue<Class<?>, String>> paramType = new LinkedHashMap<>();
            for (Parameter parameter : method.getParameters()) {
                LuaParam luaParam = parameter.getAnnotation(LuaParam.class);
                String parName = parameter.getName();
                String parComment = "";
                Class<?> parType = parameter.getType();
                if (luaParam != null) {
                    parName = luaParam.value();
                    parComment = luaParam.comment();
                }
                DefaultKeyValue<Class<?>, String> value = new DefaultKeyValue<>(parType, parComment);
                paramType.put(parName, value);
            }
            String function = getLuaFunctionString(className, method.getName(), paramType, method.getReturnType(), luaServerLibFunc.comment());
            functionBuilder.append(function);
            functionBuilder.append("\n");
        }
        String luaFile = template.replace("${FUNCTIONS}", functionBuilder.toString());

        String luaFilePath = fileDir + File.separator + className + ".lua";
        // 写文件
        writeFile(luaFile, luaFilePath);
    }
}
