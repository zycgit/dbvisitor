/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.types;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 用于定义JDBC类型与Java类型之间的映射关系注解，该注解可重复使用
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
@Repeatable(MappedCrossTypesGroup.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface MappedCrossTypes {
    /** JDBC 类型代码 */
    int jdbcType();

    /** 对应的Java类型 */
    Class<?> javaType();
}
