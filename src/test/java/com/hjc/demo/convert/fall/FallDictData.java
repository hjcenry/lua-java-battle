package com.hjc.demo.convert.fall;

import com.hjc.lua.annotation.LuaParam;
import com.hjc.lua.annotation.LuaServerModel;
import lombok.Data;

/**
 * @author hejincheng
 * @version 1.0
 * @date 2022/3/7 17:19
 **/
@LuaServerModel(className = "FallDictData", comment = "掉落表数据", fileDir = "config/fall")
@Data
public class FallDictData {

    @LuaParam(comment = "index")
    private int index;

    @LuaParam(comment = "掉落条件")
    private int conditionType;

    @LuaParam(comment = "掉落条件参数")
    private double conditionParam;

    @LuaParam(comment = "生效次数")
    private int activeTime;

    @LuaParam(comment = "掉落id")
    private int fallObjectId;

    @LuaParam(comment = "掉落数量")
    private int fallCount;

    @LuaParam(comment = "冷却时间")
    private double cdLimitTime;

    @LuaParam(comment = "适应元素")
    private boolean adaptElement;

}
