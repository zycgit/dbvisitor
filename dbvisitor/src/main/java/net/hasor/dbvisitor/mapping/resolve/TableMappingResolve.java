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
package net.hasor.dbvisitor.mapping.resolve;
import net.hasor.dbvisitor.mapping.MappingOptions;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import java.io.IOException;

/**
 * TableMapping 解析器
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-06-21
 */
public interface TableMappingResolve<T> {
    <V> TableMapping<V> resolveTableMapping(T refData, MappingOptions refFile, ClassLoader classLoader, TypeHandlerRegistry typeRegistry) throws ReflectiveOperationException, IOException;
}