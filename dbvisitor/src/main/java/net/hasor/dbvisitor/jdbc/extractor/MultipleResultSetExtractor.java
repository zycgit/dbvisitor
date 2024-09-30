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
package net.hasor.dbvisitor.jdbc.extractor;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.dbvisitor.jdbc.CallableStatementCallback;
import net.hasor.dbvisitor.jdbc.PreparedStatementCallback;
import net.hasor.dbvisitor.jdbc.ResultSetExtractor;
import net.hasor.dbvisitor.jdbc.RowMapper;
import net.hasor.dbvisitor.jdbc.mapper.ColumnMapRowMapper;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * {@link ResultSetExtractor} 接口实现类，该类会将结果集中的每一行进行处理，并返回一个 List 用以封装处理结果集。
 * @author 赵永春 (zyc@hasor.net)
 */
public class MultipleResultSetExtractor implements PreparedStatementCallback<List<Object>>, CallableStatementCallback<List<Object>> {
    private static final Logger             logger = LoggerFactory.getLogger(MultipleResultSetExtractor.class);
    private final        List<RowMapper<?>> rowMappers;

    public MultipleResultSetExtractor(RowMapper<?>... rowMapper) {
        this.rowMappers = Arrays.asList(rowMapper);
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
                RowMapper<?> rowMapper = this.rowMappers.isEmpty() ? getDefaultRowMapper() : this.rowMappers.get(0);
                resultList.add(processResultSet(resultSet, rowMapper));
            }
        } else {
            resultList.add(stmt.getUpdateCount());
        }

        int resultIndex = 1;
        while ((stmt.getMoreResults()) || (stmt.getUpdateCount() != -1)) {
            int updateCount = stmt.getUpdateCount();
            Object last = null;
            try (ResultSet resultSet = stmt.getResultSet()) {
                if (resultSet != null) {
                    if (this.rowMappers.size() > resultIndex) {
                        last = processResultSet(resultSet, this.rowMappers.get(resultIndex++));
                    } else {
                        last = processResultSet(resultSet, getDefaultRowMapper());
                    }
                } else {
                    last = updateCount;
                }
            }

            resultList.add(last);
        }
        return resultList;
    }

    protected RowMapper<?> getDefaultRowMapper() {
        return new ColumnMapRowMapper(true, TypeHandlerRegistry.DEFAULT);
    }

    /**
     * Process the given ResultSet from a stored procedure.
     * @param rs the ResultSet to process
     * @param rowMapper the corresponding stored procedure parameter
     * @return a Map that contains returned results
     */
    protected Object processResultSet(ResultSet rs, RowMapper<?> rowMapper) throws SQLException {
        if (rs == null) {
            return null;
        }
        return createSetExtractor(rowMapper).extractData(rs);
    }

    protected RowMapperResultSetExtractor<?> createSetExtractor(RowMapper<?> rowMapper) {
        return new RowMapperResultSetExtractor<>(rowMapper);
    }
}
