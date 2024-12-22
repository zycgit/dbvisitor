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
package net.hasor.dbvisitor.dal.execute;
import net.hasor.dbvisitor.jdbc.ResultSetExtractor;
import net.hasor.dbvisitor.jdbc.mapper.ColumnMapRowMapper;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Map 化 TableReader
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-07-20
 */
@Deprecated
public class ResultTableReader extends ColumnMapRowMapper implements TableReader<Map<String, Object>>, ResultSetExtractor<List<Map<String, Object>>> {

    public ResultTableReader(boolean caseInsensitive, TypeHandlerRegistry typeHandler) {
        super(caseInsensitive, typeHandler);
    }

    @Override
    public List<Map<String, Object>> extractData(ResultSet rs) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();

        List<String> columns = new ArrayList<>();
        List<TypeHandler<?>> handlers = new ArrayList<>();
        for (int i = 0; i < rsmd.getColumnCount(); i++) {
            columns.add(this.getColumnKey(rsmd, i));
            handlers.add(this.getResultSetTypeHandler(rs, i + 1, null));
        }

        List<Map<String, Object>> results = new ArrayList<>();
        int rowNum = 0;
        while (rs.next()) {
            results.add(this.extractRow(columns, handlers, rs, rowNum++));
        }
        return results;
    }

    @Override
    public Map<String, Object> extractRow(List<String> columns, ResultSet rs, int rowNum) throws SQLException {
        List<TypeHandler<?>> handlers = new ArrayList<>();
        for (int i = 0; i < columns.size(); i++) {
            handlers.add(this.getResultSetTypeHandler(rs, i + 1, null));
        }

        return this.extractRow(columns, handlers, rs, rowNum);
    }

    protected Map<String, Object> extractRow(List<String> columns, List<TypeHandler<?>> handlers, ResultSet rs, int rowNum) throws SQLException {
        Map<String, Object> target = super.createColumnMap(columns.size());

        for (int i = 0; i < columns.size(); i++) {
            String columnName = columns.get(i);
            TypeHandler<?> handler = handlers.get(i);
            if (handler == null) {
                handler = this.getHandlerRegistry().getDefaultTypeHandler();
            }

            Object result = handler.getResult(rs, i + 1);
            target.put(columnName, result);
        }

        return target;
    }
}