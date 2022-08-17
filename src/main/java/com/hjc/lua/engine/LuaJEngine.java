package com.hjc.lua.engine;

import com.hjc.lua.LuaBattlePlatform;
import com.hjc.lua.exception.LuaException;
import com.hjc.lua.log.LuaLogTool;
import com.hjc.util.StringUtil;
import org.luaj.vm2.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

/**
 * luaj引擎实现
 *
 * @ClassName LuaJEngine
 * @Description
 * @Author hejincheng
 * @Date 2021/12/7 17:25
 * @Version 1.0
 **/
public class LuaJEngine implements ILuaJEngine {

    private final Logger log = LoggerFactory.getLogger(LuaJEngine.class);

    @Override
    public Globals createLuaGlobals() {
        return createLuaGlobals(null, null);
    }

    @Override
    public Globals createLuaGlobals(String loadPackagePath, String preScript, String... loadFilesPath) {
        Globals globals = LuaBattlePlatform.buildGlobals(loadPackagePath);
        if (!StringUtil.isEmpty(preScript)) {
            this.callScript(globals, preScript);
        }
        for (String filePath : loadFilesPath) {
            if (!loadFile(filePath, globals)) {
                return null;
            }
        }
        return globals;
    }

    @Override
    public Globals createLuaGlobals(String loadPackagePath, String preScript, List<File> loadFileList) {
        Globals globals = LuaBattlePlatform.buildGlobals(loadPackagePath);
        if (!StringUtil.isEmpty(preScript)) {
            this.callScript(globals, preScript);
        }
        for (File file : loadFileList) {
            if (!loadFile(file.getAbsolutePath(), globals)) {
                return null;
            }
        }
        return globals;
    }

    private boolean loadFile(String loadFilePath, Globals globals) {
        if (!StringUtil.isEmpty(loadFilePath)) {
            // load文件
            LuaValue doFile = getLuaObj(globals, LuaJCmdEnum.DO_FILE.getCmd());
            LuaValue loadFile = LuaValue.valueOf(loadFilePath);
            // call调用会执行一遍lua文件，并加载所有function
            try {
                synchronized (this) {
                    doFile.call(loadFile);
                }
            } catch (Exception e) {
                log.error(String.format("LuaEngine.load.file.[%s].err", loadFilePath), e);
                return false;
            }
        }
        return true;
    }

    @Override
    public void logGlobals(Globals globals) {
        if (log.isDebugEnabled()) {
            log.debug(String.format("#%s.print.load.values...", this.getClass().getSimpleName()));
            for (LuaValue luaKey : globals.keys()) {
                log.debug(luaKey + "\t:\t" + globals.get(luaKey));
            }
        }
    }

    @Override
    public LuaFunction getLuaFunction(LuaValue luaModule, String funcName) {
        LuaValue luaValue = getLuaObj(luaModule, funcName);
        if (luaValue == LuaValue.NIL) {
            return null;
        }
        if (!luaValue.isfunction()) {
            return null;
        }
        return (LuaFunction) luaValue;
    }

    @Override
    public LuaValue getLuaObj(LuaValue luaObj, String objName) {
        if (luaObj == null || luaObj == LuaValue.NIL) {
            return LuaValue.NIL;
        }
        return luaObj.get(objName);
    }

    @Override
    public LuaValue callScript(Globals globals, String script) {
        if (globals == null) {
            log.error("", new LuaException("call script globals null"));
            return null;
        }
        if (StringUtil.isEmpty(script)) {
            log.error("", new LuaException("call script null"));
            return null;
        }
        LuaValue luaScript = globals.load(script);
        return luaScript.call();
    }

    @Override
    public Varargs invokeLua(Globals globals, String luaModel, Varargs varargs) {
        if (globals == null) {
            globals = createLuaGlobals();
        }
        LuaValue luaValue = getLuaObj(globals, luaModel);
        if (luaValue == null) {
            return null;
        }
        try {
            return luaValue.invoke(varargs);
        } catch (org.luaj.vm2.LuaError luaError) {
            log.error(LuaLogTool.traceback(luaError, luaError.getMessage()));
            return null;
        }
    }

    @Override
    public Varargs invokeLua(LuaValue luaValue, Varargs varargs) {
        if (luaValue == null) {
            log.error("invoke.err", new LuaException("invoke luaFunction null"));
            return null;
        }
        try {
            if (varargs == null) {
                return luaValue.invoke();
            }
            return luaValue.invoke(varargs);
        } catch (Exception luaError) {
            log.error(LuaLogTool.traceback(luaError, luaError.getMessage()));
            return null;
        }
    }

}
