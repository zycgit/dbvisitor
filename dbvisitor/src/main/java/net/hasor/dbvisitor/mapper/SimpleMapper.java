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
 * 基于注解的 Mapper，该注解用于标记一个类为简单的 Mapper 类。
 * Mapper 通常用于将数据库操作与 Java 类和方法进行关联。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-06-19
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@MapperDef
public @interface SimpleMapper {
}
