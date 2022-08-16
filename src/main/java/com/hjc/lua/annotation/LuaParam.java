package com.hjc.lua.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Lua参数
 *
 * @author hejincheng
 * @version 1.0
 * @date 2022/2/19 10:24
 **/
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LuaParam {

    /**
     * lua字段名
     *
     * @return lua字段名
     */
    String value() default "";

    /**
     * 字段注释
     *
     * @return 字段注释
     */
    String comment() default "";
}
