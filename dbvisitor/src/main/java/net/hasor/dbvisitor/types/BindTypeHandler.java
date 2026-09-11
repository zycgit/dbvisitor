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
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2020-10-31
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BindTypeHandler {
    Class<? extends TypeHandler<?>> value();
}
