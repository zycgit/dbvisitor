/*
 * Copyright 2002-2005 the original author or authors.
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
package net.hasor.db.dal.execute;
import net.hasor.cobble.ref.LinkedCaseInsensitiveMap;
import net.hasor.db.jdbc.mapper.ColumnMapRowMapper;
import net.hasor.db.mapping.TableReader;
import net.hasor.db.types.TypeHandler;
import net.hasor.db.types.TypeHandlerRegistry;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Map 化 TableReader
 * @version : 2021-07-20
 * @author 赵永春 (zyc@hasor.net)
 */
public class MapTableReader implements TableReader<Map<String, Object>> {
    private final Supplier<Map<String, Object>> objectSupplier;
    private final ColumnMapRowMapper            mapperUtils;

    public MapTableReader(boolean caseInsensitive, TypeHandlerRegistry typeHandler) {
        this.objectSupplier = () -> caseInsensitive ? new LinkedCaseInsensitiveMap<>() : new LinkedHashMap<>();
        this.mapperUtils = new ColumnMapRowMapper(caseInsensitive, typeHandler);
    }

    @Override
    public List<Map<String, Object>> extractData(List<String> columns, ResultSet rs) throws SQLException {
        List<TypeHandler<?>> handlers = new ArrayList<>();
        for (int i = 0; i < columns.size(); i++) {
            handlers.add(this.mapperUtils.getResultSetTypeHandler(rs, i + 1, null));
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
            handlers.add(this.mapperUtils.getResultSetTypeHandler(rs, i + 1, null));
        }

        return this.extractRow(columns, handlers, rs, rowNum);
    }

    protected Map<String, Object> extractRow(List<String> columns, List<TypeHandler<?>> handlers, ResultSet rs, int rowNum) throws SQLException {
        Map<String, Object> target = this.objectSupplier.get();

        for (int i = 0; i < columns.size(); i++) {
            String columnName = columns.get(i);
            TypeHandler<?> handler = handlers.get(i);
            if (handler == null) {
                handler = this.mapperUtils.getHandlerRegistry().getDefaultTypeHandler();
            }

            Object result = handler.getResult(rs, i + 1);
            target.put(columnName, result);
        }

        return target;
    }

}
