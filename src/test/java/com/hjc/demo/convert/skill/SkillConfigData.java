package com.hjc.demo.convert.skill;

import com.hjc.demo.convert.skill.SkillBaseNode;
import com.hjc.lua.annotation.LuaServerModel;
import lombok.Data;

import java.util.ArrayList;

/**
 * @author hejincheng
 * @version 1.0
 * @date 2022/2/15 10:42
 **/
@Data
@LuaServerModel(className = "SkillConfigData", comment = "技能配置数据", fileDir = "config/skill")
public class SkillConfigData {

    //技能Id
    public int id;
    //能力消耗
    public String manaCast;
    //是否之做检测
    public boolean onlyCheck;
    //极奏消耗
    public String superCast;
    //最大积累次数
    public String maxTime;
    //起始次数
    public String startTime;
    //开始节点
    public int startNodeId;
    //打断节点
    public int breakNodeId;
    //所有技能节点
    public ArrayList<SkillBaseNode> allNodes;

}
