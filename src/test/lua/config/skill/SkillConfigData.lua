---
--- Generated by EmmyLua(https://github.com/EmmyLua)
--- Created by Administrator.
--- DateTime: 2022-08-22 22:39:28
---
--- 技能配置数据

require "Lib/class"

---@class SkillConfigData : table
---@field id number
---@field manaCast string
---@field onlyCheck boolean
---@field superCast string
---@field maxTime string
---@field startTime string
---@field startNodeId number
---@field breakNodeId number
---@field allNodes SkillBaseNode[]
SkillConfigData = class(nil, 'SkillConfigData');

function SkillConfigData:ctor(_id, _manaCast, _onlyCheck, _superCast, _maxTime, _startTime, _startNodeId, _breakNodeId, _allNodes)
    self.id = _id
    self.manaCast = _manaCast
    self.onlyCheck = _onlyCheck
    self.superCast = _superCast
    self.maxTime = _maxTime
    self.startTime = _startTime
    self.startNodeId = _startNodeId
    self.breakNodeId = _breakNodeId
    self.allNodes = _allNodes
end

return SkillConfigData;