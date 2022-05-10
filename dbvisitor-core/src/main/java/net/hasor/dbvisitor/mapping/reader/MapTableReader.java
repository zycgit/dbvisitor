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
package net.hasor.dbvisitor.mapping.reader;
import net.hasor.cobble.ref.LinkedCaseInsensitiveMap;
import net.hasor.dbvisitor.mapping.TableReader;
import net.hasor.dbvisitor.mapping.def.ColumnMapping;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.types.TypeHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Map 化 TableReader
 * @version : 2021-07-20
 * @author 赵永春 (zyc@hasor.net)
 */
public class MapTableReader implements TableReader<Map<String, Object>> {
    private final boolean         caseInsensitive;
    private final TableMapping<?> tableMapping;

    public MapTableReader(TableMapping<?> tableMapping) {
        this.tableMapping = tableMapping;
        this.caseInsensitive = tableMapping.isCaseInsensitive();
    }

    /** 创建一个 Map 用于存放数据 */
    protected Map<String, Object> createColumnMap(final int columnCount) {
        if (this.caseInsensitive) {
            return new LinkedCaseInsensitiveMap<>(columnCount);
        } else {
            return new LinkedHashMap<>();
        }
    }

    @Override
    public Map<String, Object> extractRow(List<String> columns, ResultSet rs, int rowNum) throws SQLException {
        Map<String, Object> target = this.createColumnMap(columns.size());

        for (int i = 0; i < columns.size(); i++) {
            String column = columns.get(i);

            ColumnMapping mapping = this.tableMapping.getPropertyByColumn(column);
            if (mapping == null || mapping.getHandler().isReadOnly()) {
                continue;
            }

            TypeHandler<?> realHandler = mapping.getTypeHandler();
            Object result = realHandler.getResult(rs, i + 1);
            target.put(mapping.getProperty(), result);
        }
        return target;
    }
}
