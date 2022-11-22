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
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.dbvisitor.dal.dynamic.DynamicContext;
import net.hasor.dbvisitor.jdbc.CallableStatementCallback;
import net.hasor.dbvisitor.jdbc.PreparedStatementCallback;
import net.hasor.dbvisitor.jdbc.ResultSetExtractor;
import net.hasor.dbvisitor.jdbc.extractor.MultipleProcessType;
import net.hasor.dbvisitor.mapping.TableReader;
import net.hasor.dbvisitor.mapping.reader.DynamicTableReader;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * {@link ResultSetExtractor} 接口实现类，该类会将结果集中的每一行进行处理，并返回一个 List 用以封装处理结果集。
 * @author 赵永春 (zyc@hasor.net)
 */
public class DalResultSetExtractor implements PreparedStatementCallback<List<Object>>, CallableStatementCallback<List<Object>> {
    private static final Logger               logger = LoggerFactory.getLogger(DalResultSetExtractor.class);
    private final        TableReader<?>       defaultTableReader;
    private final        List<TableReader<?>> tableReaders;
    private final        MultipleProcessType  processType;

    public DalResultSetExtractor(boolean defaultCaseInsensitive, DynamicContext context, MultipleProcessType processType, TableReader<?>[] tableReaders) {
        this.processType = processType == null ? MultipleProcessType.ALL : processType;
        this.tableReaders = Arrays.asList(tableReaders);
        this.defaultTableReader = new DynamicTableReader(defaultCaseInsensitive, context.getTypeRegistry());
    }

    @Override
    public List<Object> doInCallableStatement(CallableStatement cs) throws SQLException {
        boolean retVal = cs.execute();
        return doResult(retVal, cs);
    }

    @Override
    public List<Object> doInPreparedStatement(PreparedStatement ps) throws SQLException {
        boolean retVal = ps.execute();
        return doResult(retVal, ps);
    }

    public List<Object> doResult(boolean retVal, Statement stmt) throws SQLException {
        if (logger.isTraceEnabled()) {
            logger.trace("statement.execute() returned '" + retVal + "'");
        }

        List<Object> resultList = new ArrayList<>();
        if (retVal) {
            try (ResultSet resultSet = stmt.getResultSet()) {
                TableReader<?> tableReader = this.tableReaders.isEmpty() ? this.defaultTableReader : this.tableReaders.get(0);
                resultList.add(processResultSet(resultSet, tableReader));
            }
        } else {
            resultList.add(stmt.getUpdateCount());
        }

        if (this.processType == MultipleProcessType.FIRST) {
            return resultList;
        }

        int resultIndex = 1;
        while ((stmt.getMoreResults()) || (stmt.getUpdateCount() != -1)) {
            int updateCount = stmt.getUpdateCount();
            Object last = null;
            try (ResultSet resultSet = stmt.getResultSet()) {
                if (resultSet != null) {
                    if (this.tableReaders.size() > resultIndex) {
                        last = processResultSet(resultSet, this.tableReaders.get(resultIndex++));
                    } else {
                        last = processResultSet(resultSet, this.defaultTableReader);
                    }
                } else {
                    last = updateCount;
                }
            }

            if (this.processType == MultipleProcessType.LAST) {
                resultList.set(0, last);
            } else {
                resultList.add(last);
            }
        }
        return resultList;
    }

    /**
     * Process the given ResultSet from a stored procedure.
     * @param rs the ResultSet to process
     * @param tableReader the corresponding stored procedure parameter
     * @return a Map that contains returned results
     */
    protected Object processResultSet(ResultSet rs, TableReader<?> tableReader) throws SQLException {
        if (rs == null) {
            return null;
        }

        ResultSetMetaData rsmd = rs.getMetaData();
        int nrOfColumns = rsmd.getColumnCount();
        List<String> columnList = new ArrayList<>();
        for (int i = 1; i <= nrOfColumns; i++) {
            columnList.add(lookupColumnName(rsmd, i));
        }

        List<Object> results = new ArrayList<>();
        int rowNum = 0;
        while (rs.next()) {
            results.add(tableReader.extractRow(columnList, rs, rowNum++));
        }
        return results;
    }

    private static String lookupColumnName(final ResultSetMetaData resultSetMetaData, final int columnIndex) throws SQLException {
        String name = resultSetMetaData.getColumnLabel(columnIndex);
        if (name == null || name.length() < 1) {
            name = resultSetMetaData.getColumnName(columnIndex);
        }
        return name;
    }
}
