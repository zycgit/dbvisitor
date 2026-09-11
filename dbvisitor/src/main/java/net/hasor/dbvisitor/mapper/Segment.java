/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.mapper;
import java.lang.annotation.*;

/**
 * 预定义注解，用于标记方法，可给方法关联一组字符串值。
 * 通常这些字符串值可用于定义 SQL 片段等信息。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-05-19
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Segment {
    /**
     * 获取与该注解关联的字符串数组。
     * 这些字符串可用于表示 SQL 片段等自定义信息。
     * @return 字符串数组
     */
    String[] value();
}
