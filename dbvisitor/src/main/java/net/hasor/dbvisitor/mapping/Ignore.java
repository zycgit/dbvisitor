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
 * 当使用 @Table 注解并且设置了 autoMapping = true 之后，可以通过该注解忽略列的映射。
 * 注意：注解 @Ignore/Column 一起出现的情况下映射会被忽略。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Ignore {
}
