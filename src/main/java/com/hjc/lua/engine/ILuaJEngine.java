package com.hjc.lua.engine;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import java.io.File;
import java.util.List;

/**
 * LuaJ引擎接口
 *
 * @ClassName ILuaJEngine
 * @Author hejincheng
 * @Date 2021/12/8 17:58
 * @Version 1.0
 **/
public interface ILuaJEngine {

    /**
     * 创建Lua全局对象
     *
     * @return Lua全局对象
     */
    Globals createLuaGlobals();

    /**
     * 创建Lua全局对象，并加载文件
     *
     * @param loadPackagePath 加载包目录
     * @param preScript       预处理脚本
     * @param loadFilesPath   加载文件
     * @return Lua全局对象
     */
    Globals createLuaGlobals(String loadPackagePath, String preScript, String... loadFilesPath);

    /**
     * 创建Lua全局对象，并加载文件
     *
     * @param loadPackagePath 加载包目录
     * @param preScript       预处理脚本
     * @param loadFileList    加载文件list
     * @return Lua全局对象
     */
    Globals createLuaGlobals(String loadPackagePath, String preScript, List<File> loadFileList);

    /**
     * 初始化Globals
     *
     * @param preScript     预处理脚本
     * @param loadFilesPath 加载文件list
     * @param globals       Globals
     * @return 初始成功
     */
    boolean initGlobals(String preScript, String[] loadFilesPath, Globals globals);

    /**
     * 初始化Globals
     *
     * @param preScript    预处理脚本
     * @param loadFileList 加载文件list
     * @param globals      Globals
     * @return 初始成功
     */
    boolean initGlobals(String preScript, List<File> loadFileList, Globals globals);

    /**
     * 打印globals
     *
     * @param globals lua全局对象
     */
    void logGlobals(Globals globals);

    /**
     * 获取Lua方法
     *
     * @param luaModule Lua全局对象
     * @param funcName  方法名
     * @return Lua方法
     */
    LuaFunction getLuaFunction(LuaValue luaModule, String funcName);

    /**
     * 获取Lua对象
     *
     * @param luaObj  Lua全局对象
     * @param objName 对象名
     * @return Lua对象
     */
    LuaValue getLuaObj(LuaValue luaObj, String objName);

    /**
     * 调用字符串脚本
     *
     * @param globals Lua全局对象
     * @param script  脚本
     * @return 脚本返回值
     */
    LuaValue callScript(Globals globals, String script);

    /**
     * 调用lua方法
     * <p>原始调用：无任何封装和规范的方法，可自行发挥</p>
     *
     * @param globals  全局对象
     * @param luaModel lua模块
     * @param varargs  参数
     * @return {@link Varargs}
     */
    Varargs invokeLua(Globals globals, String luaModel, Varargs varargs);

    /**
     * 调用lua方法
     * <p>原始调用：无任何封装和规范的方法，可自行发挥</p>
     *
     * @param luaValue lua对象
     * @param varargs  参数
     * @return {@link Varargs}
     */
    Varargs invokeLua(LuaValue luaValue, Varargs varargs);
}
