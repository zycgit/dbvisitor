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
import net.hasor.cobble.reflect.SFunction;
import net.hasor.dbvisitor.lambda.core.DeleteExecute;
import net.hasor.dbvisitor.lambda.core.QueryCompare;
import net.hasor.dbvisitor.lambda.support.entity.EntityQueryCompare;

/**
 * lambda Delete for Entity.
 * @version : 2022-04-02
 * @author 赵永春 (zyc@hasor.net)
 */
public interface EntityDeleteOperation<T> extends //
        CommonOperation<EntityDeleteOperation<T>>, //
        DeleteExecute<EntityDeleteOperation<T>>, //
        QueryCompare<EntityDeleteOperation<T>, SFunction<T>>,//
        EntityQueryCompare<EntityDeleteOperation<T>> {
}
