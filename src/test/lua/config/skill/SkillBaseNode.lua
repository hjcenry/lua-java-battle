---
--- Generated by EmmyLua(https://github.com/EmmyLua)
--- Created by Administrator.
--- DateTime: 2022-08-22 22:39:28
---
--- 技能节点基类

require "Lib/class"

---@class SkillBaseNode : table
---@field nodeId number
---@field nodeType number
---@field param SkillNodeParam
---@field nextNodes SkillNodeExitData[]
---@field specialExit SkillNodeExitData
SkillBaseNode = class(nil, 'SkillBaseNode');

function SkillBaseNode:ctor(_nodeId, _nodeType, _param, _nextNodes, _specialExit)
    self.nodeId = _nodeId
    self.nodeType = _nodeType
    self.param = _param
    self.nextNodes = _nextNodes
    self.specialExit = _specialExit
end

return SkillBaseNode;
