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
package net.hasor.dbvisitor.mapping.def;
import net.hasor.cobble.reflect.Annotations;

import java.util.Collection;
import java.util.List;

/**
 * 查询的表
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2020-10-31
 */
public interface TableMapping<T> {
    /** catalog */
    String getCatalog();

    /** Schema */
    String getSchema();

    /** 表名 */
    String getTable();

    Class<T> entityType();

    Annotations getAnnotations();

    boolean useGeneratedKey();

    /** 映射的实体是否是基于 Map */
    boolean isMapEntity();

    /** 是否将类型下的所有属性都当作数据库字段进行映射，true 表示自动。false 表示必须通过 @Column 注解声明 */
    boolean isAutoProperty();

    /** 使用 lambda 查询期间是否使用 引号 */
    boolean useDelimited();

    /** 结果处理是否大小写敏感 */
    boolean isCaseInsensitive();

    /** 是否配置了驼峰转换（Map模式下依赖这个参数决定是否进行驼峰转换） */
    boolean isToCamelCase();

    Collection<ColumnMapping> getProperties();

    Collection<String> getColumns();

    List<ColumnMapping> getPropertyByColumn(String column);

    ColumnMapping getPrimaryPropertyByColumn(String column);

    ColumnMapping getPropertyByName(String property);

    /** 获取补充描述信息 */
    TableDescription getDescription();

    /** 获取索引描述信息 */
    List<IndexDescription> getIndexes();

    /** 获取索引描述信息 */
    IndexDescription getIndex(String name);
}