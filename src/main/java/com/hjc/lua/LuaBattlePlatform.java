package com.hjc.lua;

import com.hjc.util.StringUtil;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LoadState;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.Bit32Lib;
import org.luaj.vm2.lib.CoroutineLib;
import org.luaj.vm2.lib.DebugLib;
import org.luaj.vm2.lib.PackageLib;
import org.luaj.vm2.lib.StringLib;
import org.luaj.vm2.lib.TableLib;
import org.luaj.vm2.lib.jse.JseBaseLib;
import org.luaj.vm2.lib.jse.JseIoLib;
import org.luaj.vm2.lib.jse.JseMathLib;
import org.luaj.vm2.lib.jse.JseOsLib;
import org.luaj.vm2.lib.jse.LuajavaLib;
import org.luaj.vm2.luajc.LuaJC;

/**
 * @ClassName LuaBattlePlatform
 * @Description lua战斗库
 * @Author hejincheng
 * @Date 2021/12/10 19:35
 * @Version 1.0
 **/
public class LuaBattlePlatform {

    /**
     * 构建全局对象
     *
     * @return lua全局对象
     */
    public static Globals buildGlobals(String packagePath) {
        Globals globals = new Globals();
        globals.load(new JseBaseLib());

        globals.load(new PackageLib());
        if (!StringUtil.isEmpty(packagePath)) {
            // 加载lua包目录
            LuaTable packageLib = (LuaTable) globals.get("package");
            packageLib.set(LuaValue.valueOf("path"), LuaValue.valueOf(packagePath));
        }

        globals.load(new Bit32Lib());
        globals.load(new TableLib());
        globals.load(new StringLib());
        globals.load(new CoroutineLib());
        globals.load(new JseMathLib());
        globals.load(new JseIoLib());
        globals.load(new JseOsLib());
        globals.load(new DebugLib());
        globals.load(new LuajavaLib());
        LoadState.install(globals);
        LuaC.install(globals);
        // 使用luajc编译器，比默认luac编译器快3倍
        LuaJC.install(globals);
        return globals;
    }
}
