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
package net.hasor.dbvisitor.wrapper;
import net.hasor.dbvisitor.wrapper.core.BasicFunc;
import net.hasor.dbvisitor.wrapper.core.DeleteExecute;
import net.hasor.dbvisitor.wrapper.core.QueryCompare;

import java.util.Map;

/**
 * lambda Delete for Map.
 * @author 赵永春 (zyc@hasor.net)
 * @version 2022-04-02
 */
public interface MapDeleteWrapper extends  //
        BasicFunc<MapDeleteWrapper>,       //
        DeleteExecute<MapDeleteWrapper>,   //
        QueryCompare<MapDeleteWrapper, Map<String, Object>, String> {
}
