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
package net.hasor.db.mapping;
import net.hasor.db.mapping.def.ColumnMapping;
import net.hasor.db.mapping.def.TableMapping;
import net.hasor.db.types.TypeHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于 TableMapping 的 TableReader 实现。
 * @version : 2021-04-13
 * @author 赵永春 (zyc@hasor.net)
 */
public class DefaultTableReader<T> implements TableReader<T> {
    private final Class<T>        mapperClass;
    private final TableMapping<T> tableMapping;

    /** Create a new TableReader.*/
    public DefaultTableReader(Class<T> mapperClass, TableMapping<T> tableMapping) {
        this.mapperClass = mapperClass;
        this.tableMapping = tableMapping;
    }

    public TableMapping<T> getTableMapping() {
        return this.tableMapping;
    }

    @Override
    public List<T> extractData(List<String> columns, ResultSet rs) throws SQLException {
        List<T> results = new ArrayList<>();
        int rowNum = 0;
        while (rs.next()) {
            results.add(this.extractRow(columns, rs, rowNum++));
        }
        return results;
    }

    @Override
    public T extractRow(List<String> columns, ResultSet rs, int rowNum) throws SQLException {
        T target = null;
        try {
            target = this.mapperClass.newInstance();
        } catch (Exception e) {
            throw new SQLException("newInstance " + this.mapperClass.getName() + " failed.", e);
        }
        for (int i = 0; i < columns.size(); i++) {
            String column = columns.get(i);

            ColumnMapping mapping = this.tableMapping.getPropertyByColumn(column);
            if (mapping == null || mapping.getHandler().isReadOnly()) {
                continue;
            }

            TypeHandler<?> realHandler = mapping.getTypeHandler();
            Object result = realHandler.getResult(rs, i + 1);
            mapping.getHandler().set(target, result);
        }
        return target;
    }
}
