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
package net.hasor.dbvisitor.jdbc.mapper;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.types.TypeHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/**
 * 用于 POJO 的 RowMapper，带有 ORM 能力
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2020-10-31
 */
public abstract class AbstractMapping<T> {
    protected final TableMapping<?> tableMapping;

    /**
     * 创建 {@link AbstractMapping} 对象
     * @param entityType 类型
     */
    public AbstractMapping(final Class<?> entityType, MappingRegistry registry) {
        Objects.requireNonNull(entityType, "entityType is required");
        TableMapping<?> tableMapping = registry.findByEntity(entityType);
        if (tableMapping == null) {
            if (MappingRegistry.isEntity(entityType)) {
                tableMapping = registry.loadEntityToSpace(entityType);
            } else {
                tableMapping = registry.loadResultMapToSpace(entityType);
            }
        }
        this.tableMapping = tableMapping;
    }

    /**
     * 创建 {@link AbstractMapping} 对象
     * @param tableMapping 类型
     */
    public AbstractMapping(TableMapping<?> tableMapping) {
        this.tableMapping = Objects.requireNonNull(tableMapping, "tableMapping is null.");
    }

    protected T extractRow(List<String> columns, ResultSet rs, int rowNum) throws SQLException {
        T target;
        try {
            target = (T) this.tableMapping.entityType().newInstance();
        } catch (Exception e) {
            throw new SQLException("newInstance " + this.tableMapping.entityType().getName() + " failed.", e);
        }

        for (int i = 0; i < columns.size(); i++) {
            String column = columns.get(i);

            List<ColumnMapping> list = this.tableMapping.getPropertyByColumn(column);
            if (list == null) {
                continue;
            }

            for (ColumnMapping mapping : list) {
                if (mapping == null || mapping.getHandler().isReadOnly()) {
                    continue;
                }

                TypeHandler<?> realHandler = mapping.getTypeHandler();
                Object result = realHandler.getResult(rs, i + 1);
                mapping.getHandler().set(target, result);
            }
        }
        return target;
    }
}
