package com.hjc.demo.convert.lib;

import com.hjc.demo.service.BattleDemoService;
import com.hjc.lua.annotation.LuaParam;
import com.hjc.lua.annotation.LuaServerLib;
import com.hjc.lua.annotation.LuaServerLibFunc;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * lua战斗核心调用Java类
 * <p>
 * 这个类里的接口和ServerLuaBattle.lua映射
 * </p>
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/2/16 18:55
 **/
@LuaServerLib(fieldName = "battle", className = "ServerLuaBattle", fileDir = "lib")
public class LuaBattleFunction {

    private static final Logger logger = LoggerFactory.getLogger(LuaBattleFunction.class);

    /**
     * 获取战斗核心
     *
     * @param battleId 战斗id
     */
    @LuaServerLibFunc(comment = "获取战斗核心")
    public static LuaValue getFightCoreLua(@LuaParam(value = "battleId", comment = "战斗id") int battleId) {
        BattleDemoService battleDemoService = BattleDemoService.getInstance();
        if (battleDemoService == null) {
            logger.error(String.format("LuaBattle.getFightCoreLua.err - battleId[%d].battleDemoService.null", battleId));
            return null;
        }
        return battleDemoService.getFightCore(battleId);
    }

    /**
     * Lua脚本调用发送消息
     *
     * @param uid      玩家id
     * @param header   消息号
     * @param luaTable 推送参数
     */
    @LuaServerLibFunc(comment = "Lua脚本调用发送消息")
    public static void invokeSendMessageByLua(@LuaParam(value = "uid", comment = "玩家id") int uid,
                                              @LuaParam(value = "header", comment = "消息号") int header,
                                              @LuaParam(value = "luaTable", comment = "消息体") LuaTable luaTable) {
        // 调用Java的网络层发送网络消息
    }

    /**
     * Lua脚本调用广播消息
     *
     * @param raidId        副本id
     * @param header        消息号
     * @param luaTable      推送参数
     * @param includeServer 广播服务端逻辑核
     */
    @LuaServerLibFunc(comment = "Lua脚本调用广播消息")
    public static void invokeBroadcastMessageByLua(@LuaParam(value = "raidId", comment = "副本id") int raidId,
                                                   @LuaParam(value = "header", comment = "消息号") int header,
                                                   @LuaParam(value = "luaTable", comment = "推送参数") LuaTable luaTable,
                                                   @LuaParam(value = "includeServer", comment = "广播服务端逻辑核") boolean includeServer) {
        // 调用Java的网络层发送网络消息
    }
}
