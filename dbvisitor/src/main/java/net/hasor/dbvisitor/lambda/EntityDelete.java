/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.lambda;
import net.hasor.cobble.reflect.SFunction;
import net.hasor.dbvisitor.lambda.core.BasicFunc;
import net.hasor.dbvisitor.lambda.core.DeleteExecute;
import net.hasor.dbvisitor.lambda.core.QueryCompare;
import net.hasor.dbvisitor.lambda.support.entity.EntityQueryCompare;

/**
 * lambda Delete for Entity.
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-04-02
 */
public interface EntityDelete<T> extends               //
        BasicFunc<EntityDelete<T>>,                    //
        DeleteExecute<EntityDelete<T>>,                //
        QueryCompare<EntityDelete<T>, T, SFunction<T>>,//
        EntityQueryCompare<EntityDelete<T>> {
    /** 转换为基于 Map 的 delete 接口。 */
    MapDelete asMap();
}
