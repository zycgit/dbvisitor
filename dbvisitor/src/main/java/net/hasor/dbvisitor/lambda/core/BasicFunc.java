/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.lambda.core;
/**
 * 基础函数式接口
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-04-02
 */
public interface BasicFunc<R> {
    /** 类型 */
    Class<?> exampleType();

    /** 重置所有状态 */
    R reset();
}
