package com.hjc.lua.log;

import com.hjc.util.enumutil.EnumUtil;
import com.hjc.util.enumutil.IndexedEnum;
import lombok.Getter;
import org.apache.logging.log4j.Level;
import org.slf4j.Logger;

import java.util.List;

/**
 * lua使用的log等级
 *
 * @ClassName LuaLogLevel
 * @Author hejincheng
 * @Date 2021/12/16 19:03
 * @Version 1.0
 **/
public enum LuaLogLevel implements IndexedEnum {
    // info
    INFO(1) {
        @Override
        public void log(Logger logger, String msg) {
            logger.info(msg);
        }

        @Override
        public boolean isEnabled(Logger logger) {
            return logger.isInfoEnabled();
        }
    },
    // warn
    WARN(2) {
        @Override
        public void log(Logger logger, String msg) {
            logger.warn(msg);
        }

        @Override
        public boolean isEnabled(Logger logger) {
            return logger.isWarnEnabled();
        }
    },
    DEBUG(3) {
        @Override
        public void log(Logger logger, String msg) {
            logger.debug(msg);
        }

        @Override
        public boolean isEnabled(Logger logger) {
            return logger.isDebugEnabled();
        }
    },
    // error
    ERROR(4) {
        @Override
        public void log(Logger logger, String msg) {
            logger.error(msg);
        }

        @Override
        public boolean isEnabled(Logger logger) {
            return logger.isErrorEnabled();
        }
    },
    ;

    private final int type;

    LuaLogLevel(int type) {
        this.type = type;
    }

    @Override
    public int getIndex() {
        return this.type;
    }

    public abstract void log(Logger logger, String msg);

    public abstract boolean isEnabled(Logger logger);

    private static final List<LuaLogLevel> values = IndexedEnumUtil.toIndexes(LuaLogLevel.values());

    public static List<LuaLogLevel> getValues() {
        return values;
    }

    public static LuaLogLevel valueOf(int value) {
        return EnumUtil.valueOf(values, value);
    }
}