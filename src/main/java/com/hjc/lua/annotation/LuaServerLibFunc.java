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
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LuaServerLibFunc {

    /**
     * 注释
     *
     * @return 注释
     */
    String comment() default "";

    /**
     * 返回注释
     *
     * @return 返回注释
     */
    String returnComment() default "";
}
