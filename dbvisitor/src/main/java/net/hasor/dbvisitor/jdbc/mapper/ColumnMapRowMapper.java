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
import net.hasor.cobble.ref.LinkedCaseInsensitiveMap;
import net.hasor.dbvisitor.jdbc.JdbcOperations;
import net.hasor.dbvisitor.jdbc.RowMapper;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@link RowMapper} implementation that creates a <code>java.util.Map</code>
 * for each row, representing all columns as key-value pairs: one
 * entry for each column, with the column name as key.
 *
 * <p>The Map implementation to use and the key to use for each column
 * in the column Map can be customized through overriding
 * {@link #createColumnMap} and {@link #getColumnKey}, respectively.
 *
 * <p><b>Note:</b> By default, ColumnMapRowMapper will try to build a linked Map
 * with case-insensitive keys, to preserve column order as well as allow any
 * casing to be used for column names. This requires Commons Collections on the
 * classpath (which will be autodetected). Else, the fallback is a standard linked
 * HashMap, which will still preserve column order but requires the application
 * to specify the column names in the same casing as exposed by the driver.
 *
 * @author Juergen Hoeller
 * @author 赵永春 (zyc@hasor.net)
 * @since 1.2
 * @see JdbcOperations#queryForList(String)
 * @see JdbcOperations#queryForMap(String)
 */
public class ColumnMapRowMapper extends AbstractRowMapper<Map<String, Object>> {
    private final boolean caseInsensitive;

    public ColumnMapRowMapper() {
        this(true, TypeHandlerRegistry.DEFAULT);
    }

    public ColumnMapRowMapper(boolean caseInsensitive, TypeHandlerRegistry typeHandler) {
        super(typeHandler);
        this.caseInsensitive = caseInsensitive;
    }

    public boolean isCaseInsensitive() {
        return this.caseInsensitive;
    }

    @Override
    public final Map<String, Object> mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        Map<String, Object> mapOfColValues = this.createColumnMap(columnCount);
        for (int i = 1; i <= columnCount; i++) {
            String key = this.getColumnKey(rsmd, i);
            Object obj = this.getColumnValue(rs, i);
            if (!mapOfColValues.containsKey(key)) {
                mapOfColValues.put(key, obj);
            }
        }
        return mapOfColValues;
    }

    protected String getColumnKey(final ResultSetMetaData rsmd, final int index) throws SQLException {
        String name = rsmd.getColumnLabel(index);
        if (name == null || name.length() < 1) {
            name = rsmd.getColumnName(index);
        }
        return name;
    }

    /** 取得指定列的值 */
    protected Object getColumnValue(final ResultSet rs, final int index) throws SQLException {
        return getResultSetValue(rs, index);
    }

    /** 创建一个 Map 用于存放数据 */
    protected Map<String, Object> createColumnMap(final int columnCount) {
        if (this.caseInsensitive) {
            return new LinkedCaseInsensitiveMap<>(columnCount);
        } else {
            return new LinkedHashMap<>();
        }
    }
}
