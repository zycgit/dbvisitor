/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.types;
import java.lang.annotation.*;

/**
 * 用于指定类型处理器 {@link TypeHandler} 可以处理的 Java 类型
 * 该注解通常标注在自定义类型处理器上，声明该处理器能够处理的Java类型。
 * @author 赵永春 (zyc@hasor.net)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MappedJavaTypes {
    /** 该类型处理器支持的Java类型数组 */
    Class<?>[] value();
}
