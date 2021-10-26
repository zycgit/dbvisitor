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
package net.hasor.db.mapping.def;

import net.hasor.db.mapping.DefaultTableReader;
import net.hasor.db.mapping.TableReader;

import java.util.Collection;

/**
 * 查询的表
 * @version : 2020-10-31
 * @author 赵永春 (zyc@hasor.net)
 */
public interface TableMapping<T> {

    /** Schema */
    public String getSchema();

    /** 表名 */
    public String getTable();

    public Class<T> entityType();

    /** 是否将类型下的所有属性都当作数据库字段进行映射，true 表示自动。false 表示必须通过 @Column 注解声明 */
    public boolean isAutoProperty();

    /** 使用 lambda 查询期间是否使用 引号 */
    public boolean useDelimited();

    public boolean isCaseInsensitive();

    public Collection<ColumnMapping> getProperties();

    public ColumnMapping getPropertyByColumn(String column);

    public ColumnMapping getPropertyByName(String property);

    public default TableReader<T> toReader() {
        return new DefaultTableReader<>(this.entityType(), this);
    }

}