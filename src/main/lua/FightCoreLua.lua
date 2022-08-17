---
--- Generated by EmmyLua(https://github.com/EmmyLua)
--- Created by wth.
--- DateTime: 2021/12/4 17:07
--- lua战斗逻辑外壳

require "Battle/Logic/Room/BattleRoom"

---@class FightCoreLua:table lua战斗逻辑外壳
FightCoreLua = class(nil, 'FightCoreLua')

function FightCoreLua:ctor(_battleId)
    print("FightCoreLua ctor")
end

function FightCoreLua:Init()
    print("FightCoreLua Init")
end

--- 创建和初始化战斗房间
---@public
---@param _battleData table 战斗初始化相关数据
function FightCoreLua:InitBattleData(_battleData)
    print("FightCoreLua InitBattleData")
end

--- 战斗逻辑帧更新
function FightCoreLua:Update(_dt, _frameId, _dtMs)
    print("FightCoreLua Update")
end

--- 服务器接收消息接口
---@public
---@param _uid number 发送协议的玩家id，如果是战斗房间消息该值为nil
---@param _id number 协议id
---@param _msgTable table 协议
function FightCoreLua:ReceiveMsg(_uid, _id, _msgTable)
    print("FightCoreLua Update")
end

--- 关闭核心
---@public
function FightCoreLua:Close()
    print("FightCoreLua Close")
end

return FightCoreLua