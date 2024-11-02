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
import net.hasor.cobble.reflect.SFunction;
import net.hasor.dbvisitor.wrapper.core.BasicFunc;
import net.hasor.dbvisitor.wrapper.core.QueryCompare;
import net.hasor.dbvisitor.wrapper.core.UpdateExecute;
import net.hasor.dbvisitor.wrapper.support.entity.EntityQueryCompare;

/**
 * lambda Update for Entity.
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2022-04-02
 */
public interface EntityUpdateWrapper<T> extends                //
        BasicFunc<EntityUpdateWrapper<T>>,                     //
        UpdateExecute<EntityUpdateWrapper<T>, T, SFunction<T>>,//
        QueryCompare<EntityUpdateWrapper<T>, T, SFunction<T>>, //
        EntityQueryCompare<EntityUpdateWrapper<T>> {

    MapUpdateWrapper asMap();
}
