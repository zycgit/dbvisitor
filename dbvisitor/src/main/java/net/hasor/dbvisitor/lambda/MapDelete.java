/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0.
 * See the LICENSE.txt file for the full license.
 * https://www.apache.org/licenses/LICENSE-2.0
 */
package net.hasor.dbvisitor.lambda;
import java.util.Map;
import net.hasor.dbvisitor.lambda.core.BasicFunc;
import net.hasor.dbvisitor.lambda.core.DeleteExecute;
import net.hasor.dbvisitor.lambda.core.QueryCompare;

/**
 * lambda Delete for Map.
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-04-02
 */
public interface MapDelete extends  //
        BasicFunc<MapDelete>,       //
        DeleteExecute<MapDelete>,   //
        QueryCompare<MapDelete, Map<String, Object>, String> {
}
