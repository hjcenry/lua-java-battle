package com.hjc.demo.convert.skill;

import com.hjc.lua.annotation.LuaServerModel;
import lombok.Data;

import java.util.ArrayList;

/**
 * @author hejincheng
 * @version 1.0
 * @date 2022/2/16 17:29
 **/
@Data
@LuaServerModel(className = "SkillNodeExitData", comment = "技能退出节点", fileDir = "config/skill")
public class SkillNodeExitData {

    //事件类型
    public int type;
    //事件参数
    public String param;
    //后续节点
    public ArrayList<Integer> nextNodeID;

}
