package com.hjc.lua;

import com.hjc.lua.engine.ILuaJEngine;
import com.hjc.lua.engine.LuaJEngine;
import com.hjc.util.FileUtil;
import com.hjc.util.StringUtil;
import org.apache.commons.collections.CollectionUtils;
import org.luaj.vm2.Globals;
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

    private Logger logger = LoggerFactory.getLogger(LuaBattleManager.class);

    private static volatile LuaBattleManager instance;

    /**
     * lua全局对象
     */
    private Globals globals;
    /**
     * lua引擎
     */
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

        this.luaEngine.logGlobals(globals);
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
}
