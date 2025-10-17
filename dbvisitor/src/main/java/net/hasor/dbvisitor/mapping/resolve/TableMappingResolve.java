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
import java.io.IOException;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.Options;
import net.hasor.dbvisitor.mapping.def.TableMapping;

/**
 * TableMapping 解析器
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-06-21
 */
public interface TableMappingResolve<T> {
    /**
     * 解析并返回表映射配置
     * @param refData 要解析的引用数据，可以是类、注解或其他类型
     * @param refFile 解析过程中使用的配置选项
     * @param registry 映射注册表，用于存储和查找映射关系
     * @param <V> 返回的TableMapping中映射的目标类型
     * @return 解析后的表映射配置
     * @throws ReflectiveOperationException 当反射操作失败时抛出
     * @throws IOException 当IO操作失败时抛出
     */
    <V> TableMapping<V> resolveTableMapping(T refData, Options refFile, MappingRegistry registry) throws ReflectiveOperationException, IOException;
}