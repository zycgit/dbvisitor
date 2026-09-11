/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.mapping;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据库序列注解，用于指定主键生成使用的数据库序列。
 * 可标注在字段或方法上，通过value属性指定数据库序列名称
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-12-01
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface KeySeq {
    /** 数据库序列名称 */
    String value() default "";
}
