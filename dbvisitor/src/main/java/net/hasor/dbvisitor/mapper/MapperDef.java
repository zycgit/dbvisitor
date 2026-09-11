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
 * Mapper定义注解，用于标注其他Mapper相关的注解
 * 这是一个元注解，通常用于自定义Mapper注解时使用。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-06-19
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface MapperDef {
}
