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
 * 用于标记类型处理器(TypeHandler)不启用缓存的注解，
 * 被该注解标注的类型处理器在执行时将不会使用缓存机制，
 * 每次都会创建新的处理器实例。适用于有状态的类型处理器。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface NoCache {
}
