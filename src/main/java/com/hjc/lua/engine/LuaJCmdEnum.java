package com.hjc.lua.engine;

import com.hjc.util.enumutil.EnumUtil;
import com.hjc.util.enumutil.IndexedEnum;
import lombok.Getter;

import java.util.List;

/**
 * LuaJ中默认环境支持的调用命令
 *
 * @ClassName LuaJCmdEnum
 * @Author hejincheng
 * @Date 2021/12/7 17:38
 * @Version 1.0
 **/
public enum LuaJCmdEnum implements IndexedEnum {
    _G(0, "_G"),
    _VERSION(1, "_VERSION"),
    ASSERT(2, "assert"),
    COLLECT_GARBAGE(3, "collectgarbage"),
    DO_FILE(4, "dofile"),
    ERROR(5, "error"),
    GET_META_TABLE(6, "getmetatable"),
    LOAD(7, "load"),
    LOAD_FILE(8, "loadfile"),
    PCALL(9, "pcall"),
    PRINT(10, "print"),
    RAW_EQUAL(11, "rawequal"),
    RAW_GET(12, "rawget"),
    RAW_LEN(13, "rawlen"),
    RAW_SET(14, "rawset"),
    SELECT(15, "select"),
    SET_META_TABLE(16, "setmetatable"),
    TO_NUMBER(17, "tonumber"),
    TO_STRING(18, "tostring"),
    TYPE(19, "type"),
    XPCALL(20, "xpcall"),
    NEXT(21, "next"),
    PAIRS(22, "pairs"),
    IPAIRS(23, "ipairs"),
    ;

    private int type;
    @Getter
    private String cmd;

    LuaJCmdEnum(int type, String cmd) {
        this.type = type;
        this.cmd = cmd;
    }

    @Override
    public int getIndex() {
        return this.type;
    }

    private static final List<LuaJCmdEnum> values = IndexedEnumUtil.toIndexes(LuaJCmdEnum.values());

    public static List<LuaJCmdEnum> getValues() {
        return values;
    }

    public static LuaJCmdEnum valueOf(int value) {
        return EnumUtil.valueOf(values, value);
    }
}