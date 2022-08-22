package com.hjc.demo.convert.skill;

import com.hjc.lua.annotation.LuaServerModel;
import lombok.Data;

import java.util.ArrayList;

/**
 * @author hejincheng
 * @version 1.0
 * @date 2022/2/15 15:13
 **/
@Data
@LuaServerModel(className = "SkillBaseNode", comment = "技能节点基类", fileDir = "config/skill")
public class SkillBaseNode {

    //节点id
    public int nodeId;
    //节点类型
    public int nodeType;
    //节点参数
    public SkillNodeParam param;
    //节点子节点
    public ArrayList<SkillNodeExitData> nextNodes;
    //特殊出口
    public SkillNodeExitData specialExit;

}
