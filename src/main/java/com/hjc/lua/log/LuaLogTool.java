package com.hjc.lua.log;

import com.hjc.lua.annotation.LuaParam;
import com.hjc.lua.annotation.LuaServerLib;
import com.hjc.lua.annotation.LuaServerLibFunc;
import org.apache.logging.log4j.Level;
import org.luaj.vm2.LuaBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @ClassName LuaDebugTool
 * @Description
 * @Author hejincheng
 * @Date 2021/12/16 11:54
 * @Version 1.0
 **/
@LuaServerLib(fieldName = "logTool", className = "ServerLogTool", fileDir = "Lib/Server")
public class LuaLogTool {

    private static Logger log = LoggerFactory.getLogger(LuaLogTool.class);

    /**
     * 过滤堆栈中结尾信息
     */
    private static final String[] FILTER_END_TRACES = new String[]{
            "(Unknown Source)",
            "(Native Method)",
            ".java"
    };

    private static final String JAVA_EXT = ".java:";

    @LuaServerLibFunc(comment = "获取堆栈", returnComment = "堆栈信息")
    public static String traceback(@LuaParam("log") String msg) {
        LuaTracebackException exception = new LuaTracebackException();
        return traceback(exception, msg);
    }

    /**
     * 获取堆栈堆栈
     *
     * @param msg
     * @return
     */
    public static String traceback(Exception exception, String msg) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        // 获取堆栈信息
        StackTraceElement[] stackTraceElements = exception.getStackTrace();
        if (stackTraceElements != null) {
            printWriter.println(msg);
            for (StackTraceElement element : stackTraceElements) {
                String traceString = element.toString();
                boolean isValid = true;
                for (String filterTrace : FILTER_END_TRACES) {
                    if (traceString.contains(filterTrace)) {
                        // 过滤掉无用信息
                        isValid = false;
                        break;
                    }
                }
                if (!isValid) {
                    continue;
                }
                if (traceString.contains("(")) {
                    printWriter.println("\tat " + traceString.substring(traceString.indexOf("(")));
                } else {
                    printWriter.println("\tat " + traceString);
                }
            }
        }
        return stringWriter.toString();
    }

    /**
     * 打印log
     *
     * @param logLevel log级别
     * @param msg      log消息
     */
    @LuaServerLibFunc(comment = "打印log")
    public static void log(@LuaParam(value = "logLevel", comment = "log级别") int logLevel,
                           @LuaParam(value = "msg", comment = "log消息") String msg) {
        LuaLogLevel luaLogLevel = LuaLogLevel.valueOf(logLevel);
        if (luaLogLevel == null) {
            luaLogLevel = LuaLogLevel.INFO;
        }
        luaLogLevel.log(log, msg);
    }

    /**
     * log级别是否可用
     *
     * @param logLevel log级别
     * @return log级别是否可用
     */
    @LuaServerLibFunc(comment = "log级别是否可用", returnComment = "log级别是否可用")
    public static LuaBoolean isLogEnabled(@LuaParam(value = "logLevel", comment = "log级别") int logLevel) {
        LuaLogLevel luaLogLevel = LuaLogLevel.valueOf(logLevel);
        if (luaLogLevel == null) {
            return LuaBoolean.valueOf(false);
        }
        return LuaBoolean.valueOf(luaLogLevel.isEnabled(log));
    }

}
