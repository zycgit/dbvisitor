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
import net.hasor.dbvisitor.jdbc.RowMapper;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.mapping.def.TableMapping;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 用于 POJO 的 RowMapper，带有 ORM 能力
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public class BeanMappingRowMapper<T> extends AbstractMapping<T> implements RowMapper<T> {
    /**
     * 创建 {@link RowMapper} 对象
     * @param entityType 类型
     */
    public BeanMappingRowMapper(final Class<T> entityType) {
        super(entityType, MappingRegistry.DEFAULT);
    }

    /**
     * 创建 {@link RowMapper} 对象
     * @param entityType 类型
     */
    public BeanMappingRowMapper(final Class<T> entityType, MappingRegistry registry) {
        super(entityType, registry);
    }

    /**
     * 创建 {@link RowMapper} 对象
     * @param tableMapping 类型
     */
    public BeanMappingRowMapper(TableMapping<?> tableMapping) {
        super(tableMapping);
    }

    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int nrOfColumns = rsmd.getColumnCount();
        List<String> columnList = new ArrayList<>();
        for (int i = 1; i <= nrOfColumns; i++) {
            columnList.add(lookupColumnName(rsmd, i));
        }

        return this.extractRow(columnList, rs, rowNum);
    }

    private static String lookupColumnName(final ResultSetMetaData resultSetMetaData, final int columnIndex) throws SQLException {
        String name = resultSetMetaData.getColumnLabel(columnIndex);
        if (name == null || name.length() < 1) {
            name = resultSetMetaData.getColumnName(columnIndex);
        }
        return name;
    }
}
