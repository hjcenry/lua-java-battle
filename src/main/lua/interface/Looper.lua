-- 外层tick更新
LOOPER = {}

-- tick间隔
local dt = 0.033
-- 当前帧号
local frameId = 0

-- 更新
---@param _fightCoreLua FightCoreLua
function LOOPER.ServerUpdate(_fightCoreLua)
    frameId = frameId + 1
    if _fightCoreLua ~= nil then
        _fightCoreLua:Update(dt, frameId, 33)
    end
end