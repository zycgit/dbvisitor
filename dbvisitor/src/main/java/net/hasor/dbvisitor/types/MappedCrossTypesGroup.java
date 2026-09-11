/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.types;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link MappedCrossTypes} 注解的容器注解，用于支持多个 {@link MappedCrossTypes} 注解的重复使用
 * @author 赵永春 (zyc@hasor.net)
 * @version 2013-3-26
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MappedCrossTypesGroup {
    /** 包含的 {@link MappedCrossTypes} 注解数组 */
    MappedCrossTypes[] value();
}
