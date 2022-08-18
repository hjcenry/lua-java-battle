package com.hjc.lua;

import com.hjc.lua.engine.ILuaJEngine;
import com.hjc.lua.engine.LuaJEngine;
import com.hjc.util.FileUtil;
import com.hjc.util.StringUtil;
import lombok.Getter;
import org.apache.commons.collections4.CollectionUtils;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author hejincheng
 * @version 1.0
 * @date 2022/8/17 23:05
 **/
public class LuaBattleManager {

    private final Logger logger = LoggerFactory.getLogger(LuaBattleManager.class);

    private static volatile LuaBattleManager instance;

    /**
     * lua全局对象
     */
    @Getter
    private Globals globals;
    /**
     * lua引擎
     */
    @Getter
    private ILuaJEngine luaEngine;

    private LuaBattleManager() {
    }

    public static LuaBattleManager getInstance() {
        if (instance == null) {
            synchronized (LuaBattleManager.class) {
                if (instance == null) {
                    instance = new LuaBattleManager();
                }
            }
        }
        return instance;
    }

    /**
     * 初始化lua战斗管理器
     *
     * @param luaInit lua初始参数
     */
    public void init(LuaInit luaInit) {
        // 实例Lua全局引擎
        luaEngine = new LuaJEngine();
        // lua包目录
        String packagePath = StringUtil.isEmpty(luaInit.getLuaPackagePath()) ? String.format(LuaBattleConst.DEFAULT_LUA_PACKAGE_PATH, luaInit.getLuaRootPath()) : luaInit.getLuaPackagePath();

        // 加载lua文件
        List<File> loadFiles = this.loadLuaFileList(luaInit.getLuaRootPath(), luaInit.getLuaLoadFiles(), luaInit.getLuaLoadDirectories());
        if (CollectionUtils.isEmpty(loadFiles)) {
            // 创建战斗全局globals
            logger.warn(String.format("lua.init.err.lua.load.files.null \n\troot:\t[%s]\n\tfiles:\t[%s]\n\tdir:\t[%s]",
                    luaInit.getLuaRootPath(), luaInit.getLuaLoadFiles(), luaInit.getLuaLoadDirectories()));
        }
        Globals globals = luaEngine.createLuaGlobals(packagePath, luaInit.getPreScript(), loadFiles);
        if (globals == null) {
            logger.error("lua.init.err.create.globals.err");
            return;
        }

        if (luaInit.isShowLog()) {
            this.luaEngine.logGlobals(globals);
        }
        this.globals = globals;
    }

    private List<File> loadLuaFileList(String luaRootPath, String loadFiles, String loadDirectory) {
        List<File> loadLuaFileList = new ArrayList<>();
        if (StringUtil.isEmpty(luaRootPath)) {
            // lua根路径空
            logger.error("lua.root.path.null");
            return Collections.emptyList();
        }
        if (!StringUtil.isEmpty(loadFiles)) {
            // add需要加载的文件
            for (String filePath : loadFiles.split(LuaBattleConst.LUA_LOAD_FILE_SEPARATE)) {
                // 找文件
                File file = FileUtil.findFile(luaRootPath +
                        filePath, LuaBattleConst.LUA_FILE_EXT);
                if (file != null) {
                    loadLuaFileList.add(file);
                }
            }
        }
        if (!StringUtil.isEmpty(loadDirectory)) {
            // add需要加载的文件目录
            for (String dirPath : loadDirectory.split(LuaBattleConst.LUA_LOAD_FILE_SEPARATE)) {
                // 找出该目录下的所有文件
                List<File> fileList = FileUtil.getAllFileInDir(luaRootPath +
                        dirPath, LuaBattleConst.LUA_FILE_EXT, true);
                if (!CollectionUtils.isEmpty(fileList)) {
                    loadLuaFileList.addAll(fileList);
                }
            }
        }
        logger.info("{}.loadLuaFileList...", this.getClass().getSimpleName());
        for (File file : loadLuaFileList) {
            logger.info("load lua file:{}", file.getAbsolutePath());
        }
        return loadLuaFileList;
    }

    //===========================================================================================================================================================
    // 常用对外接口
    //===========================================================================================================================================================

    /**
     * 获取Lua方法
     *
     * @param luaModule Lua全局对象
     * @param funcName  方法名
     * @return Lua方法
     */
    public LuaFunction getLuaFunction(LuaValue luaModule, String funcName) {
        return this.luaEngine.getLuaFunction(luaModule, funcName);
    }

    /**
     * 获取lua方法
     *
     * @param funcName 方法名
     * @return lua方法
     */
    public LuaFunction getLuaFunction(String funcName) {
        return this.luaEngine.getLuaFunction(this.globals, funcName);
    }

    /**
     * 获取Lua对象
     *
     * @param luaObj  Lua全局对象
     * @param objName 对象名
     * @return Lua对象
     */
    public LuaValue getLuaObj(LuaValue luaObj, String objName) {
        return this.luaEngine.getLuaObj(luaObj, objName);
    }

    /**
     * 调用字符串脚本
     *
     * @param script 脚本
     * @return 脚本返回值
     */
    public LuaValue callScript(String script) {
        return this.luaEngine.callScript(this.globals, script);
    }

    /**
     * 调用lua方法
     * <p>原始调用：无任何封装和规范的方法，可自行发挥</p>
     *
     * @param luaModel lua模块
     * @param varargs  参数
     * @return {@link Varargs}
     */
    public Varargs invokeLua(String luaModel, Varargs varargs) {
        return this.luaEngine.invokeLua(this.globals, luaModel, varargs);
    }

    /**
     * 调用lua方法
     * <p>原始调用：无任何封装和规范的方法，可自行发挥</p>
     *
     * @param luaValue lua对象
     * @param varargs  参数
     * @return {@link Varargs}
     */
    public Varargs invokeLua(LuaValue luaValue, Varargs varargs) {
        return this.luaEngine.invokeLua(luaValue, varargs);
    }

    /**
     * 初始化lua执行方法
     * 可按"."分割查找
     *
     * @param funcName 方法名
     * @return lua方法
     */
    public LuaFunction initExecuteFunction(String funcName) {
        // 消息执行方法
        if (StringUtil.isEmpty(funcName)) {
            logger.error("lua.init.execute.function.err.null");
            return null;
        }
        // 按“.”分割，支持按模块调用
        String[] functionFullNameArr = funcName.split(LuaBattleConst.SPLIT_POINT);
        LuaValue luaModule = globals;
        String functionName = null;
        // 遍历模块目录
        for (int index = 0; index < functionFullNameArr.length; index++) {
            if (index == functionFullNameArr.length - 1) {
                // 找出方法名
                functionName = functionFullNameArr[index];
                break;
            }
            // lua模块
            luaModule = luaEngine.getLuaObj(luaModule, functionFullNameArr[index]);
        }
        if (luaModule == null) {
            logger.error(String.format("lua.init.execute.function.err.[%s].null", funcName));
            return null;
        }
        LuaFunction luaExecuteFunction = luaEngine.getLuaFunction(luaModule, functionName);
        if (luaExecuteFunction == null) {
            logger.error(String.format("lua.init.execute.function.err.[%s].null", funcName));
            return null;
        }
        return luaExecuteFunction;
    }
    //===========================================================================================================================================================
    // 常用对外接口
    //===========================================================================================================================================================
}
