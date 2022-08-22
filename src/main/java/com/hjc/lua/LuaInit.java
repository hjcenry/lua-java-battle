package com.hjc.lua;

import lombok.Builder;
import lombok.Getter;
import org.luaj.vm2.Globals;

/**
 * @author hejincheng
 * @version 1.0
 * @date 2022/8/17 23:24
 **/
@Builder
@Getter
public class LuaInit {

    /**
     * lua预处理脚本
     */
    private String preScript;
    /**
     * lua根目录
     */
    private String luaRootPath;
    /**
     * lua包目录
     */
    private String luaPackagePath;
    /**
     * lua加载的文件(以英文逗号分割)
     */
    private String luaLoadFiles;
    /**
     * lua加载的目录(以英文逗号分割)
     */
    private String luaLoadDirectories;
    /**
     * 自定义lua globals
     */
    private Globals globals;
    /**
     * log
     */
    private boolean showLog;
}
