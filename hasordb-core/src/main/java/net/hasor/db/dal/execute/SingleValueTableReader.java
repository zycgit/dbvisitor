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
import net.hasor.db.mapping.TableReader;
import net.hasor.db.types.TypeHandler;
import net.hasor.db.types.TypeHandlerRegistry;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 单值 化 TableReader
 * @version : 2021-07-20
 * @author 赵永春 (zyc@hasor.net)
 */
public class SingleValueTableReader<T> implements TableReader<T> {
    private final TypeHandler<T> typeHandler;

    public SingleValueTableReader(Class<T> resultType, TypeHandlerRegistry typeRegistry) {
        this.typeHandler = (TypeHandler<T>) typeRegistry.getTypeHandler(resultType);
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
        return this.typeHandler.getResult(rs, 1);
    }
}
