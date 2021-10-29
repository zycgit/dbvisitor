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
import net.hasor.db.dal.dynamic.DynamicContext;
import net.hasor.db.dal.dynamic.DynamicSql;
import net.hasor.db.dal.dynamic.QuerySqlBuilder;
import net.hasor.db.dal.repository.MultipleResultsType;
import net.hasor.db.dal.repository.ResultSetType;
import net.hasor.db.dal.repository.config.CallableSqlConfig;
import net.hasor.db.dal.repository.config.DmlSqlConfig;
import net.hasor.db.dal.repository.config.QuerySqlConfig;
import net.hasor.db.dialect.Page;
import net.hasor.db.jdbc.extractor.MultipleProcessType;
import net.hasor.db.mapping.TableReader;
import net.hasor.db.mapping.def.TableMapping;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 执行器基类
 * @version : 2021-07-20
 * @author 赵永春 (zyc@hasor.net)
 */
public abstract class AbstractStatementExecute<T> {
    private final DynamicContext context;

    public AbstractStatementExecute(DynamicContext context) {
        this.context = context;
    }

    protected DynamicContext getContext() {
        return this.context;
    }

    public final T execute(Connection conn, DynamicSql dynamicSql, Map<String, Object> data, Page pageInfo) throws SQLException {
        QuerySqlBuilder queryBuilder = dynamicSql.buildQuery(data, this.context);
        ExecuteInfo executeInfo = new ExecuteInfo();

        executeInfo.pageInfo = pageInfo;
        executeInfo.timeout = -1;
        executeInfo.parameterType = null;
        executeInfo.resultMap = "";
        executeInfo.fetchSize = 256;
        executeInfo.resultSetType = ResultSetType.DEFAULT;
        executeInfo.multipleResultType = MultipleResultsType.LAST;

        if (dynamicSql instanceof DmlSqlConfig) {
            executeInfo.timeout = ((DmlSqlConfig) dynamicSql).getTimeout();
            executeInfo.parameterType = ((DmlSqlConfig) dynamicSql).getParameterType();
        }
        if (dynamicSql instanceof QuerySqlConfig) {
            String resultMapStr = ((QuerySqlConfig) dynamicSql).getResultMap();
            String resultTypeStr = ((QuerySqlConfig) dynamicSql).getResultType();
            executeInfo.resultMap = StringUtils.isNotBlank(resultTypeStr) ? resultTypeStr : resultMapStr;
            executeInfo.fetchSize = ((QuerySqlConfig) dynamicSql).getFetchSize();
            executeInfo.resultSetType = ((QuerySqlConfig) dynamicSql).getResultSetType();
            executeInfo.multipleResultType = ((QuerySqlConfig) dynamicSql).getMultipleResultType();
        }
        if (dynamicSql instanceof CallableSqlConfig) {
            executeInfo.resultOut = ((CallableSqlConfig) dynamicSql).getResultOut();
            if (executeInfo.resultOut == null) {
                executeInfo.resultOut = Collections.emptySet();
            }
        }

        return executeQuery(conn, executeInfo, queryBuilder);
    }

    protected boolean usingPage(ExecuteInfo executeInfo) {
        return executeInfo.pageInfo == null || executeInfo.pageInfo.getPageSize() <= 0;
    }

    protected abstract T executeQuery(Connection con, ExecuteInfo executeInfo, QuerySqlBuilder queryBuilder) throws SQLException;

    protected void configStatement(ExecuteInfo executeInfo, Statement statement) throws SQLException {
        if (executeInfo.timeout > 0) {
            statement.setQueryTimeout(executeInfo.timeout);
        }
        if (executeInfo.fetchSize > 0) {
            statement.setFetchSize(executeInfo.fetchSize);
        }
    }

    protected DalResultSetExtractor buildExtractor(ExecuteInfo executeInfo) {

        TableReader<?>[] tableReaders = null;
        if (StringUtils.isBlank(executeInfo.resultMap)) {
            tableReaders = new TableReader[] { getDefaultTableReader(executeInfo, this.context) };
        } else {
            String[] resultMapSplit = executeInfo.resultMap.split(",");
            tableReaders = new TableReader[resultMapSplit.length];
            for (int i = 0; i < resultMapSplit.length; i++) {
                TableMapping<?> tableMapping = this.context.findTableMapping(resultMapSplit[i]);
                if (tableMapping != null) {
                    tableReaders[i] = tableMapping.toReader();
                } else {
                    tableReaders[i] = getDefaultTableReader(executeInfo, this.context);
                }
            }
        }

        MultipleProcessType multipleType = MultipleProcessType.valueOf(executeInfo.multipleResultType.getTypeName());
        return new DalResultSetExtractor(executeInfo.caseInsensitive, this.context, multipleType, tableReaders);
    }

    private MapTableReader getDefaultTableReader(ExecuteInfo executeInfo, DynamicContext context) {
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

    protected static class ExecuteInfo {
        // query
        public String              parameterType      = null;
        public int                 timeout            = -1;
        public int                 fetchSize          = 256;
        public ResultSetType       resultSetType      = ResultSetType.FORWARD_ONLY;
        public String              resultMap;
        public boolean             caseInsensitive    = true;
        public MultipleResultsType multipleResultType = MultipleResultsType.LAST;
        public Set<String>         resultOut;

        public Page                pageInfo;
        public boolean             pageResult;
        public Map<String, Object> data;
    }
}