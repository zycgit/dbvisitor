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
import net.hasor.dbvisitor.lambda.core.QueryCompare;
import net.hasor.dbvisitor.lambda.core.UpdateExecute;

/**
 * lambda Update for Map.
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-04-02
 */
public interface MapUpdate extends                             //
        BasicFunc<MapUpdate>,                                  //
        UpdateExecute<MapUpdate, Map<String, Object>, String>, //
        QueryCompare<MapUpdate, Map<String, Object>, String> {
}
