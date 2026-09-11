/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.lambda;
import net.hasor.dbvisitor.lambda.core.BasicFunc;
import net.hasor.dbvisitor.lambda.core.InsertExecute;

/**
 * lambda Insert for Entity.
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-04-02
 */
public interface Insert<T> extends  //
        BasicFunc<Insert<T>>,       //
        InsertExecute<Insert<T>, T> {
    /** 转换为基于 Map 的 insert 接口。 */
    MapInsert asMap();
}
