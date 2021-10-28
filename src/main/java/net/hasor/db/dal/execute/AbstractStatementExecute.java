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
import net.hasor.cobble.StringUtils;
import net.hasor.db.dal.dynamic.QuerySqlBuilder;
import net.hasor.db.dal.repository.MultipleResultsType;
import net.hasor.db.dal.repository.manager.DalDynamicContext;
import net.hasor.db.jdbc.extractor.MultipleProcessType;
import net.hasor.db.mapping.TableReader;
import net.hasor.db.mapping.def.TableMapping;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.function.Supplier;

/**
 * 执行器基类
 * @version : 2021-07-20
 * @author 赵永春 (zyc@hasor.net)
 */
public abstract class AbstractStatementExecute<T> {
    private final Supplier<Connection> connection;

    public AbstractStatementExecute(Supplier<Connection> connection) {
        this.connection = connection;
    }

    public final T execute(QuerySqlBuilder queryBuilder) throws SQLException {
        try (Connection conn = this.connection.get()) {
            return executeQuery(conn, queryBuilder);
        }
    }

    protected boolean usingPage(ExecuteInfo executeInfo) {
        return executeInfo.pageInfo == null || executeInfo.pageInfo.getPageSize() <= 0;
    }

    protected abstract T executeQuery(Connection con, QuerySqlBuilder queryBuilder) throws SQLException;

    protected void configStatement(ExecuteInfo executeInfo, Statement statement) throws SQLException {
        if (executeInfo.timeout > 0) {
            statement.setQueryTimeout(executeInfo.timeout);
        }
        if (executeInfo.fetchSize > 0) {
            statement.setFetchSize(executeInfo.fetchSize);
        }
    }

    protected DalResultSetExtractor buildExtractor(ExecuteInfo executeInfo, DalDynamicContext context) {

        TableReader<?>[] tableReaders = null;
        if (StringUtils.isBlank(executeInfo.resultMap)) {
            tableReaders = new TableReader[] { getDefaultTableReader(executeInfo, context) };
        } else {
            String[] resultMapSplit = executeInfo.resultMap.split(",");
            tableReaders = new TableReader[resultMapSplit.length];
            for (int i = 0; i < resultMapSplit.length; i++) {
                TableMapping<?> tableMapping = context.findTableMapping(resultMapSplit[i]);
                if (tableMapping != null) {
                    tableReaders[i] = tableMapping.toReader();
                } else {
                    tableReaders[i] = getDefaultTableReader(executeInfo, context);
                }
            }
        }

        MultipleProcessType multipleType = MultipleProcessType.valueOf(executeInfo.multipleResultType.getTypeName());
        return new DalResultSetExtractor(executeInfo.caseInsensitive, context, multipleType, tableReaders);
    }

    private MapTableReader getDefaultTableReader(ExecuteInfo executeInfo, DalDynamicContext context) {
        return new MapTableReader(executeInfo.caseInsensitive, context.getTypeRegistry());
    }

    protected Object getResult(List<Object> result, ExecuteInfo executeInfo) {
        if (result == null || result.isEmpty()) {
            return null;
        }

        if (executeInfo.multipleResultType == MultipleResultsType.FIRST) {
            return result.get(0);
        } else if (executeInfo.multipleResultType == MultipleResultsType.LAST) {
            return result.get(result.size() - 1);
        } else {
            return result;
        }
    }

}
