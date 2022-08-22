package com.hjc.lua.convertor;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.hjc.lua.annotation.LuaParam;
import com.hjc.lua.annotation.LuaServerModel;
import com.hjc.util.ScanUtil;
import com.hjc.util.StringUtil;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * 生成javaBean转换lua文件
 *
 * <p>
 * 使用方法：</br>
 * <li>1. 把需要转换的JavaBean类加上注解{@link LuaServerModel}</li>
 * <li>2.运行这个类方法，加上VM启动参数-DluaRootPath指定lua路径</li>
 *     <ul>
 *     <li>-DluaRootPath=lua项目根路径</li>
 *     <li>-DtemplateFilePath=模板文件路径（默认取框架自带模板）</li>
 *     <li>-DjavaScanPackage=要扫描的Java包路径（默认com.hjc）</li>
 *     </ul>
 * <li>3.同时生成lua文件，并更新ServerLib.lua文件中的引用</li>
 * </p>
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/2/18 11:47
 **/
public class LuaServerModelFileConverter extends LuaServerFileConverter {

    private static String templateFile = "ServerModelLuaFile.template";

    /**
     * 忽略生成文件的类
     */
    private static final Set<Class<?>> IGNORE_GENERATE_TYPE = Sets.newHashSet(Integer.class, Float.class, Double.class, String.class, Boolean.class);

    public static void main(String[] args) {
        initParams();
        templateFile = templateFilePath + File.separator + templateFile;

        // 找出所有ServerModel的文件
        boolean findFiles = false;
        for (Class<?> javaClass : ScanUtil.getClasses(javaScanPackage)) {
            LuaServerModel luaServerModel = javaClass.getAnnotation(LuaServerModel.class);
            if (luaServerModel == null) {
                continue;
            }
            findFiles = true;

            String javaSimpleName = javaClass.getSimpleName();
            String className = luaServerModel.className();
            String fileDir = luaServerModel.fileDir();
            String comment = luaServerModel.comment();

            // 默认类名
            className = StringUtil.isEmpty(className) ? javaSimpleName : className;
            // 文件路径
            fileDir = StringUtil.isEmpty(fileDir) ? templateFilePath : luaLoadRootPath + File.separator + fileDir;

            // 生成lua文件
            generateLuaFile(javaClass, fileDir, className, comment);
        }
        if (!findFiles) {
            System.out.println(String.format("包目录[%s]没有找到@LuaServerModel的文件", javaScanPackage));
        }
    }

    public static void generateLuaFile(Class<?> javaClz, String fileDir, String className, String comment) {
        if (IGNORE_GENERATE_TYPE.contains(javaClz)) {
            return;
        }
        StringBuilder templateBuilder = readFile(templateFile, className);
        if (templateBuilder == null) {
            return;
        }
        String template = templateBuilder.toString();

        LinkedHashMap<String, Class<?>> fieldTypes = new LinkedHashMap<>();
        LinkedHashMap<String, String> fieldComments = new LinkedHashMap<>();

        List<Field> declaredFieldList = Lists.newArrayList(javaClz.getDeclaredFields());
        Class<?> superClz = javaClz.getSuperclass();

        if (superClz != null && superClz != Object.class) {
            declaredFieldList.addAll(Arrays.asList(superClz.getDeclaredFields()));
        }

        Class<?> superClass = javaClz.getSuperclass();
        LuaServerModel superModel = superClass.getAnnotation(LuaServerModel.class);
        if (superModel != null) {
            String superClassName = superModel.className();
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

        // 属性
        for (Field declaredField : declaredFieldList) {

            String fieldName = declaredField.getName();
            String fieldComment = null;
            Class<?> fieldType = declaredField.getType();

            LuaParam luaParam = declaredField.getAnnotation(LuaParam.class);
            if (luaParam != null) {
                if (StringUtil.isNotEmpty(luaParam.value())) {
                    fieldName = luaParam.value();
                }
                if (StringUtil.isNotEmpty(luaParam.comment())) {
                    fieldComment = luaParam.comment();
                }
            }

            if (List.class.isAssignableFrom(declaredField.getType())) {
                // List类型
                Type ft = declaredField.getGenericType();
                Class<?> subClz = (Class<?>) ((ParameterizedType) ft).getActualTypeArguments()[0];
                Object tmpObj = Array.newInstance(subClz, 0);
                fieldType = tmpObj.getClass();
            } else if (Map.class.isAssignableFrom(declaredField.getType())) {
                // Map类型
                Type ft = declaredField.getGenericType();
                Class<?> subClz = (Class<?>) ((ParameterizedType) ft).getActualTypeArguments()[1];
                Object tmpObj = Array.newInstance(subClz, 0);
                fieldType = tmpObj.getClass();
            }

            fieldTypes.put(fieldName, fieldType);
            fieldComments.put(fieldName, fieldComment);
        }
        String ctorFunction = getLuaCtorFunctionString(className, fieldTypes);

        String fieldsComment = getLuaClassFieldCommentString(fieldTypes, fieldComments);
        String luaFile = template.replace("${CTOR_FUNCTION}", ctorFunction);
        luaFile = luaFile.replace("${FILEDS_COMMENTS}", fieldsComment);

        String luaFilePath = fileDir + File.separator + className + ".lua";
        writeFile(luaFile, luaFilePath);
    }

}
