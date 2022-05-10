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
package net.hasor.dbvisitor.jdbc.mapper;
import net.hasor.dbvisitor.jdbc.RowMapper;
import net.hasor.dbvisitor.mapping.TableReader;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.mapping.resolve.ClassTableMappingResolve;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 用于 POJO 的 RowMapper，带有 ORM 能力
 * @version : 2020-10-31
 * @author 赵永春 (zyc@hasor.net)
 */
public class MappingRowMapper<T> implements RowMapper<T> {
    private final TableReader<T> tableReader;

    /** Create a new ResultMapper.*/
    public MappingRowMapper(Class<T> mapperClass) {
        this(mapperClass, TypeHandlerRegistry.DEFAULT);
    }

    /** Create a new ResultMapper.*/
    public MappingRowMapper(Class<T> mapperClass, TypeHandlerRegistry typeRegistry) {
        TableMapping<?> tableMapping = ClassTableMappingResolve.resolveTableMapping(mapperClass, mapperClass.getClassLoader(), typeRegistry);
        this.tableReader = (TableReader<T>) tableMapping.toReader();
    }

    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int nrOfColumns = rsmd.getColumnCount();
        List<String> columnList = new ArrayList<>();
        for (int i = 1; i <= nrOfColumns; i++) {
            String colName = rsmd.getColumnName(i);
            columnList.add(colName);
        }
        return tableReader.extractRow(columnList, rs, rowNum);
    }
}
