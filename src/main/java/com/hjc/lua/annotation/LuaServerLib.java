package com.hjc.lua.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Lua服务端调用库
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/2/19 10:24
 **/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface LuaServerLib {

    /**
     * lua字段名
     *
     * @return lua字段名 lua中是self.xxx = _xxx
     */
    String fieldName() default "";

    /**
     * lua类名
     *
     * @return lua类名
     */
    String className() default "";

    /**
     * 注释
     *
     * @return 注释
     */
    String comment() default "";

    /**
     * 生成文件目录
     *
     * @return 文件目录，以BattleLogic/Lua为根路径开始
     */
    String fileDir() default "";

    /**
     * 是否加到ServerLib
     *
     * @return 是否加到ServerLib
     */
    boolean addToServerLib() default true;
}
