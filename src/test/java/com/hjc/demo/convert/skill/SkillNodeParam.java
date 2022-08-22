package com.hjc.demo.convert.skill;

import com.hjc.lua.annotation.LuaServerModel;
import lombok.Data;

import java.util.ArrayList;

/**
 * @author hejincheng
 * @version 1.0
 * @date 2022/2/25 17:17
 **/
@Data
@LuaServerModel(className = "SkillNodeParam", comment = "技能节点参数", fileDir = "config/skill")
public class SkillNodeParam {

    public String time;
    public String barrageID;
    public String spreadParam;
    public String elementID;
    public String shootEffect;
    public String consume;
    public String colliderId;
    public ArrayList<String> addBuff;
    public ArrayList<String> removeBuff;
    public float maxSpeedRatio;
    public float currentSpeedRatio;
    public int loopId;
    public int loopTime;
    public double loopDelayTime;
    public boolean isForceBreak;
    public ArrayList<String> buffGroupIds;
    public boolean selfAll;
}
