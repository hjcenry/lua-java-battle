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