/*
 * Copyright 2015-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.hasor.dbvisitor.lambda;
import net.hasor.dbvisitor.lambda.core.BasicFunc;
import net.hasor.dbvisitor.lambda.core.QueryCompare;
import net.hasor.dbvisitor.lambda.core.UpdateExecute;

import java.util.Map;

/**
 * lambda Update for Map.
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2022-04-02
 */
public interface MapUpdateOperation extends                             //
        BasicFunc<MapUpdateOperation>,                                  //
        UpdateExecute<MapUpdateOperation, Map<String, Object>, String>, //
        QueryCompare<MapUpdateOperation, Map<String, Object>, String> {
}
