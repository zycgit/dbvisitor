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
package net.hasor.db.jdbc.mapper;
import net.hasor.db.jdbc.ResultSetExtractor;
import net.hasor.db.mapping.TableReader;
import net.hasor.db.mapping.def.TableMapping;
import net.hasor.db.mapping.resolve.ClassTableMappingResolve;
import net.hasor.db.types.TypeHandlerRegistry;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 用于 POJO 的 RowMapper，带有 ORM 能力
 * @version : 2020-10-31
 * @author 赵永春 (zyc@hasor.net)
 */
public class MappingResultSetExtractor<T> implements ResultSetExtractor<List<T>> {
    private final TableReader<T> tableReader;

    /**
     * 创建 {@link MappingResultSetExtractor} 对象
     * @param mapperClass 类型
     */
    public MappingResultSetExtractor(final Class<T> mapperClass) {
        this(mapperClass, TypeHandlerRegistry.DEFAULT);
    }

    public MappingResultSetExtractor(final Class<T> mapperClass, TypeHandlerRegistry typeRegistry) {
        Objects.requireNonNull(mapperClass, "mapperClass is required");
        TableMapping<?> tableMapping = ClassTableMappingResolve.resolveTableMapping(mapperClass, mapperClass.getClassLoader(), typeRegistry);
        this.tableReader = (TableReader<T>) tableMapping.toReader();
    }

    @Override
    public List<T> extractData(final ResultSet rs) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int nrOfColumns = rsmd.getColumnCount();
        List<String> columnList = new ArrayList<>();
        for (int i = 1; i <= nrOfColumns; i++) {
            columnList.add(lookupColumnName(rsmd, i));
        }

        return this.tableReader.extractData(columnList, rs);
    }

    private static String lookupColumnName(final ResultSetMetaData resultSetMetaData, final int columnIndex) throws SQLException {
        String name = resultSetMetaData.getColumnLabel(columnIndex);
        if (name == null || name.length() < 1) {
            name = resultSetMetaData.getColumnName(columnIndex);
        }
        return name;
    }
}