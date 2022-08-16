package com.hjc.lua.compiler;

import com.google.common.collect.Lists;
import com.hjc.util.StringUtil;
import org.apache.commons.collections.KeyValue;
import org.apache.commons.collections.keyvalue.DefaultKeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * lua编译工具类
 * 由于luaj在运行中产生动态字节码，无法看到源码，因此可通过LuaCompiler手动编译得到一份完整class字节码文件
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/2/22 16:29
 **/
public class LuaCompiler {

    private static final Logger logger = LoggerFactory.getLogger(LuaCompiler.class);

    /**
     * 编译lua文件为class
     *
     * @param classPath 编译后的class路径
     * @param luaPath   lua文件路径
     * @param ignores   忽略的目录名,以英文逗号分割
     * @param showLog   是否展示详细的编译log
     */
    public static void compile(String classPath, String luaPath, String ignores, boolean showLog) {
        compile(classPath, luaPath, null, ignores, showLog);
    }

    /**
     * 编译lua文件为class
     *
     * @param classPath   编译后的class路径
     * @param luaPath     lua文件路径
     * @param packageName lua包目录名
     * @param ignores     忽略的目录名,以英文逗号分割
     * @param showLog     是否展示详细的编译log
     */
    public static void compile(String classPath, String luaPath, String packageName, String ignores, boolean showLog) {
        // 忽略的文件和目录
        if (StringUtil.isEmpty(packageName)) {
            packageName = new File(luaPath).getAbsolutePath().replace(File.separator, "_").replace(":", "");
        }
        List<KeyValue> keyValues = Lists.newArrayList(
                // 包名前缀
                new DefaultKeyValue("-p", packageName),
                // 递归编译所有
                new DefaultKeyValue("-r", ""),
                // load class验证生成的字节码
                new DefaultKeyValue("-l", ""),
                // 生成class目录
                new DefaultKeyValue("-d", classPath),
                // lua文件目录
                new DefaultKeyValue("-s", luaPath),
                // 忽略的lua文件
                new DefaultKeyValue("-i", ignores),
                // lua文件目录
                new DefaultKeyValue("/", "")
        );
        if (showLog) {
            // 显示信息
            keyValues.add(new DefaultKeyValue("-v", ""));
        }

        // 编译
        luajc.compile(keyValues, logger);
    }

    /**
     * Test Main
     *
     * @param args
     */
    public static void main(String[] args) {
        String classPath = "E:\\lua\\";
        String luaPath = "E:\\thd\\BattleLogic\\Lua\\";
        String ignores = "mobdebug.lua;dataTable;proto;protobuf;Test;Battle/Configs;Battle/LogicV2;Common";
        compile(classPath, luaPath, "BattleLogic", ignores, true);
    }
}
