/*
 * Copyright 2002-2010 the original author or authors.
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
import net.hasor.dbvisitor.mapping.TableReader;
import net.hasor.dbvisitor.mapping.reader.DefaultTableReader;
import net.hasor.dbvisitor.mapping.reader.MapTableReader;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import java.util.Collection;
import java.util.Map;

/**
 * 查询的表
 * @version : 2020-10-31
 * @author 赵永春 (zyc@hasor.net)
 */
public interface TableMapping<T> {

    /** Schema */
    String getSchema();

    /** 表名 */
    String getTable();

    Class<T> entityType();

    /** 是否将类型下的所有属性都当作数据库字段进行映射，true 表示自动。false 表示必须通过 @Column 注解声明 */
    boolean isAutoProperty();

    /** 使用 lambda 查询期间是否使用 引号 */
    boolean useDelimited();

    /** 结果处理是否大小写敏感 */
    boolean isCaseInsensitive();

    Collection<ColumnMapping> getProperties();

    ColumnMapping getPropertyByColumn(String column);

    ColumnMapping getPropertyByName(String property);

    default TableReader<Map<String, Object>> toMapReader() {
        return new MapTableReader(this);
    }

    default TableReader<T> toReader() {
        return new DefaultTableReader<>(this);
    }

    TypeHandlerRegistry getTypeHandlerRegistry();

}