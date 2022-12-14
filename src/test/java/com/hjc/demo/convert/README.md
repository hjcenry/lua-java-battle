# Java类转换Lua文件工具

`用于需要互相调用，或者同一份代码，需要两边语言都写的情况`

> 此工具类基于class.lua(src/test/lua/lib/class.lua)的面向对象模式

## 一、Java调用库

Lua中需要调用java方法的地方，需要java创建调用类，然后在lua中调用，创建对应的lua类，能在lua 代码中更方便的调用。

这个过程可以通过工具生成lua文件，并加到Lua统一调用接口ServerLib.lua中 Lua中所有调用Java方法的接口都通过ServerLib调用，如SERVER_LIB.battle:invokeBattleResult(
BATTLE_ROOM:GetBattleId(), unit_player:GetPlayerId(), self.battleResult:GetId())

### 使用方法：

#### 1. 新建Java调用库，增加类注解@LuaServerLib

|参数名|描述|默认值|
|---|---|---|
|fieldName|ServerLib字段名|Java类名首字母小写|
|className|lua类名|Java类名|
|fileDir|lua文件目录，以luaRootPath为根路径开始|工具类同目录下：~/src/test/java/lua/|
|comment|注释|无|
|addToServerLib|是否添加到ServerLib|是|

#### 2. 对需要调用的静态方法增加方法注解@LuaServerLibFunc

|参数名|描述|默认值|
|---|---|---|
|comment|注释|无|
|returnComment|返回注释|无|

#### 3. 对方法内的参数增加参数注解@LuaParam

|参数名|描述|默认值|
|---|---|---|
|comment|注释|无|
|value|lua字段名|无|

> 因为luaj编译后的class的字段名都变成arg0,arg1了，所以不加@LuaParam注解生成的参数名都不认识

#### 4. 运行工具类：lua.LuaServerLibFileConverter，增加VM参数指定lua路径

```properties
-DluaRootPath=lua项目根路径
-DtemplateFilePath=模板文件路径（默认取框架自带模板）
-DjavaScanPackage=要扫描的Java包路径（默认com.hjc）
-DserverLibFilePath=ServerLib文件路径（默认Lib\\Server）
```

#### 5. 刷新IDEA：File -> Reload All from Disk

参考代码：

```java

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
@LuaServerLib(fieldName = "battle", className = "ServerLuaBattle", fileDir = "Lib/Server/")
public class LuaBattleFunction {

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
        IHumanService humanService = GameServiceManager.getService(IHumanService.class);
        if (humanService == null) {
            Log.battleLogger.error(String.format("%d.LuaBattle.invokeSendMessageByLua.server.err - header[%d].luaTable[%s]", uid, header, luaTable));
            return;
        }
        Human human = humanService.getHuman(uid);
        if (human == null) {
            Log.battleLogger.error(String.format("%d.LuaBattle.invokeSendMessageByLua.err.player.null - header[%d].luaTable[%s]", uid, header, luaTable));
            return;
        }
        human.push(header, luaTable);
    }
}
```

生成lua文件：

```lua
---
--- Generated by EmmyLua(https://github.com/EmmyLua)
--- Created by Administrator.
--- DateTime: 2022-08-22 22:57:29
---
--- 通过Java工具类自动生成，请勿修改，重新生成会被覆盖
---

require "Lib/class"

---@class ServerLuaBattle : table
ServerLuaBattle = class(nil, 'ServerLuaBattle');

function ServerLuaBattle:ctor()
end

-- 获取战斗核心
---@param battleId number 战斗id
---@type function
---@return any
---@public
function ServerLuaBattle:getFightCoreLua(battleId)
    return
end

-- Lua脚本调用发送消息
---@param uid number 玩家id
---@param header number 消息号
---@param luaTable table 消息体
---@type function
---@return void
---@public
function ServerLuaBattle:invokeSendMessageByLua(uid, header, luaTable)
    return
end

-- Lua脚本调用广播消息
---@param raidId number 副本id
---@param header number 消息号
---@param luaTable table 推送参数
---@param includeServer boolean 广播服务端逻辑核
---@type function
---@return void
---@public
function ServerLuaBattle:invokeBroadcastMessageByLua(raidId, header, luaTable, includeServer)
    return
end

return ServerLuaBattle;
```

生成ServerLib.lua文件：

```lua
---
--- Generated by EmmyLua(https://github.com/EmmyLua)
--- Created by Administrator.
--- DateTime: 2022-08-22 22:57:30
---
--- 通过Java工具类自动生成，请勿修改，重新生成会被覆盖
---
--- 服务端Java调用库

require "Lib/class"

---@class ServerLib : table
---@field battle ServerLuaBattle
---@field logTool ServerLogTool
ServerLib = class(nil, 'ServerLib');

function ServerLib:ctor()
    self.battle = luajava.bindClass("com.hjc.demo.convert.lib.LuaBattleFunction")
    self.logTool = luajava.bindClass("com.hjc.lua.log.LuaLogTool")
end
```

## 二、Java数据类

有的数据，需要服务端全局共享，不能每场战斗都独一份lua数据，这种情况下可以在Java创建共享数据，这样的Model类可以通过工具生成需要的lua文件。

### 使用方法：

#### 1. 新建Java调用库，增加类注解@LuaServerModel

|参数名|描述|默认值|
|---|---|---|
|className|lua类名|Java类名|
|fileDir|lua文件目录，以~/为根路径开始|工具类同目录下：~/src/test/java/lua/|
|comment|注释|无|

#### 2. 运行工具类：lua.LuaServerModelFileConverter，增加VM参数指定lua路径

```properties
-DluaRootPath=lua项目根路径
-DtemplateFilePath=模板文件路径（默认取框架自带模板）
-DjavaScanPackage=要扫描的Java包路径（默认com.hjc）
```

#### 3. 刷新IDEA：File -> Reload All from Disk

参考代码： FallDictData.java

```java
package com.hjc.helper;

import com.hjc.annotation.LuaParam;
import com.hjc.annotation.LuaServerModel;
import lombok.Data;

/**
 * @author hejincheng
 * @version 1.0
 * @date 2022/3/7 17:19
 **/
@LuaServerModel(className = "FallDictData", comment = "掉落表数据", fileDir = "Battle/Logic/Room/BattleObject/Fall")
@Data
public class FallDictData {

    @LuaParam(comment = "掉落条件")
    private int conditionType;

    @LuaParam(comment = "掉落条件参数")
    private float conditionParam;

    @LuaParam(comment = "生效次数")
    private int activeTimes;

    @LuaParam(comment = "掉落id")
    private int fallObjectId;

    @LuaParam(comment = "掉落数量")
    private int fallCount;

    @LuaParam(comment = "冷却时间")
    private float cdLimitTime;

}

```

生成的lua：

```lua
--- 掉落表数据

require "Lib/class"

---@class FallDictData : table
---@field conditionType number 掉落条件
---@field conditionParam number 掉落条件参数
---@field activeTimes number 生效次数
---@field fallObjectId number 掉落id
---@field fallCount number 掉落数量
---@field cdLimitTime number 冷却时间
FallDictData = class(nil, 'FallDictData');

function FallDictData:ctor(_conditionType, _conditionParam, _activeTimes, _fallObjectId, _fallCount, _cdLimitTime)
    self.conditionType = _conditionType
    self.conditionParam = _conditionParam
    self.activeTimes = _activeTimes
    self.fallObjectId = _fallObjectId
    self.fallCount = _fallCount
    self.cdLimitTime = _cdLimitTime
end

return FallDictData;
```

