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
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.io.IOUtils;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.dbvisitor.dal.dynamic.DynamicContext;
import net.hasor.dbvisitor.dal.dynamic.DynamicSql;
import net.hasor.dbvisitor.dal.dynamic.SqlArg;
import net.hasor.dbvisitor.dal.dynamic.SqlMode;
import net.hasor.dbvisitor.dal.repository.MultipleResultsType;
import net.hasor.dbvisitor.dal.repository.ResultSetType;
import net.hasor.dbvisitor.dal.repository.config.DmlSqlConfig;
import net.hasor.dbvisitor.dal.repository.config.InsertSqlConfig;
import net.hasor.dbvisitor.dal.repository.config.QuerySqlConfig;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.PageSqlDialect;
import net.hasor.dbvisitor.dialect.SqlBuilder;
import net.hasor.dbvisitor.jdbc.extractor.MultipleProcessType;
import net.hasor.dbvisitor.mapping.TableReader;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.mapping.reader.ResultTableReader;
import net.hasor.dbvisitor.page.Page;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * 执行器基类
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-07-20
 */
public abstract class AbstractStatementExecute<T> {
    protected static final Logger         logger = LoggerFactory.getLogger(AbstractStatementExecute.class);
    private final          DynamicContext context;

    public AbstractStatementExecute(DynamicContext context) {
        this.context = context;
    }

    protected DynamicContext getContext() {
        return this.context;
    }

    public final T execute(Connection conn, DynamicSql dynamicSql, Map<String, Object> data) throws SQLException {
        return this.execute(conn, dynamicSql, data, null, false, null, false);
    }

    public final T execute(Connection conn, DynamicSql dynamicSql, Map<String, Object> data, Page pageInfo, boolean pageResult, PageSqlDialect dialect) throws SQLException {
        return this.execute(conn, dynamicSql, data, pageInfo, pageResult, dialect, false);
    }

    public final T execute(Connection conn, DynamicSql dynamicSql, Map<String, Object> data, Page pageInfo, boolean pageResult, PageSqlDialect dialect, boolean resultAsMap) throws SQLException {
        SqlBuilder queryBuilder = dynamicSql.buildQuery(data, this.context);
        ExecuteInfo executeInfo = new ExecuteInfo();

        executeInfo.pageInfo = pageInfo;
        executeInfo.timeout = -1;
        executeInfo.resultMap = "";
        executeInfo.resultType = Map.class.getName();
        executeInfo.fetchSize = 256;
        executeInfo.resultSetType = ResultSetType.DEFAULT;
        executeInfo.multipleResultType = MultipleResultsType.LAST;
        executeInfo.pageDialect = dialect;
        executeInfo.pageResult = pageResult;
        executeInfo.data = data;
        executeInfo.hasSelectKey = false;

        if (dynamicSql instanceof DmlSqlConfig) {
            executeInfo.timeout = ((DmlSqlConfig) dynamicSql).getTimeout();
            executeInfo.hasSelectKey = ((DmlSqlConfig) dynamicSql).getSelectKey() != null;
        }
        if (dynamicSql instanceof QuerySqlConfig) {
            executeInfo.resultMap = ((QuerySqlConfig) dynamicSql).getResultMap();
            executeInfo.resultType = ((QuerySqlConfig) dynamicSql).getResultType();
            executeInfo.fetchSize = ((QuerySqlConfig) dynamicSql).getFetchSize();
            executeInfo.resultSetType = ((QuerySqlConfig) dynamicSql).getResultSetType();
            executeInfo.multipleResultType = ((QuerySqlConfig) dynamicSql).getMultipleResultType();
        }

        if (dynamicSql instanceof InsertSqlConfig && !executeInfo.hasSelectKey) {
            executeInfo.useGeneratedKeys = ((InsertSqlConfig) dynamicSql).isUseGeneratedKeys();
            executeInfo.keyProperty = ((InsertSqlConfig) dynamicSql).getKeyProperty();
            executeInfo.parameterType = ((InsertSqlConfig) dynamicSql).getParameterType();
        }

        if (resultAsMap) {
            executeInfo.resultType = Map.class.getName();
            executeInfo.resultMap = "";
        }

        return executeQuery(conn, executeInfo, queryBuilder);
    }

    protected boolean usingPage(ExecuteInfo executeInfo) {
        return executeInfo.pageInfo != null && executeInfo.pageInfo.getPageSize() > 0;
    }

    protected boolean refreshTotalCount(ExecuteInfo executeInfo) {
        return executeInfo.pageInfo.isRefreshTotalCount() || executeInfo.pageInfo.getTotalCount() <= 0;
    }

    protected abstract T executeQuery(Connection con, ExecuteInfo executeInfo, SqlBuilder sqlBuilder) throws SQLException;

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
        if (StringUtils.isBlank(executeInfo.resultType) && StringUtils.isBlank(executeInfo.resultMap)) {

            tableReaders = new TableReader[] { getDefaultTableReader(executeInfo, this.context) };

        } else if (StringUtils.isNotBlank(executeInfo.resultType)) {

            String[] resultTypeSplit = executeInfo.resultType.split(",");
            tableReaders = new TableReader[resultTypeSplit.length];
            for (int i = 0; i < resultTypeSplit.length; i++) {
                if (StringUtils.isBlank(resultTypeSplit[i])) {
                    throw new NullPointerException("resultType is blank '" + resultTypeSplit[i] + "' of '" + executeInfo.resultType + "'");
                }

                tableReaders[i] = this.context.findTableReader(resultTypeSplit[i]);
                if (tableReaders[i] == null) {
                    throw new NoSuchElementException("not found resultType '" + resultTypeSplit[i] + "' of '" + executeInfo.resultType + "'");
                }
            }

        } else if (StringUtils.isNotBlank(executeInfo.resultMap)) {

            String[] resultMapSplit = executeInfo.resultMap.split(",");
            tableReaders = new TableReader[resultMapSplit.length];
            for (int i = 0; i < resultMapSplit.length; i++) {
                if (StringUtils.isBlank(resultMapSplit[i])) {
                    throw new NullPointerException("resultMap is blank '" + resultMapSplit[i] + "' of '" + executeInfo.resultMap + "'");
                }

                TableMapping<?> tableMapping = this.context.findTableMapping(resultMapSplit[i]);
                if (tableMapping != null) {
                    tableReaders[i] = tableMapping.toReader();
                } else {
                    throw new NoSuchElementException("not found resultMap '" + resultMapSplit[i] + "' of '" + executeInfo.resultMap + "'");
                }
            }
        } else {

            throw new IllegalStateException("doesn't trigger here");
        }

        MultipleProcessType multipleType = MultipleProcessType.valueOf(executeInfo.multipleResultType.getTypeName());
        return new DalResultSetExtractor(executeInfo.caseInsensitive, this.context, multipleType, tableReaders);
    }

    private ResultTableReader getDefaultTableReader(ExecuteInfo executeInfo, DynamicContext context) {
        return new ResultTableReader(executeInfo.caseInsensitive, context.getTypeRegistry());
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

    protected List<SqlArg> toArgs(BoundSql boundSql) {
        Object[] oriArgs = boundSql.getArgs();
        return Arrays.stream(oriArgs).map(o -> {
            if (o instanceof SqlArg) {
                return (SqlArg) o;
            } else {
                SqlArg sqlArg = SqlArg.valueOf(o);
                sqlArg.setSqlMode(SqlMode.In);
                if (o == null) {
                    sqlArg.setTypeHandler(getContext().getTypeRegistry().getDefaultTypeHandler());
                    sqlArg.setJdbcType(Types.NULL);
                } else {
                    sqlArg.setTypeHandler(getContext().findTypeHandler(o.getClass()));
                    sqlArg.setJdbcType(TypeHandlerRegistry.toSqlType(o.getClass()));
                }
                return sqlArg;
            }
        }).collect(Collectors.toList());
    }

    protected static StringBuilder fmtBoundSql(BoundSql boundSql) {
        StringBuilder builder = new StringBuilder("querySQL: ");

        try {
            List<String> lines = IOUtils.readLines(new StringReader(boundSql.getSqlString()));
            for (String line : lines) {
                if (StringUtils.isNotBlank(line)) {
                    builder.append(line.trim()).append(" ");
                }
            }
        } catch (Exception e) {
            builder.append(boundSql.getSqlString().replace("\n", ""));
        }
        builder.append(" ");

        builder.append(",parameter: [");
        int i = 0;
        for (Object arg : boundSql.getArgs()) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(fmtValue(arg));
            i++;
        }
        builder.append("] ");

        return builder;
    }

    protected static String fmtBoundSql(BoundSql boundSql, Map<String, Object> userData) {
        StringBuilder builder = fmtBoundSql(boundSql);

        builder.append(",userData: {");
        int j = 0;
        for (String key : userData.keySet()) {
            if (j > 0) {
                builder.append(", ");
            }
            builder.append(key);
            builder.append(" = ");
            builder.append(fmtValue(userData.get(key)));
            j++;
        }
        builder.append("}");
        return builder.toString();
    }

    protected static String fmtValue(Object value) {
        Object object = value instanceof SqlArg ? ((SqlArg) value).getValue() : value;
        if (object == null) {
            return "null";
        } else if (object instanceof String) {
            if (((String) object).length() > 2048) {
                return "'" + ((String) object).substring(0, 2048) + "...'";
            } else {
                return "'" + ((String) object).replace("'", "\\'") + "'";
            }
        } else if (object instanceof Page) {
            return "page[pageSize=" + ((Page) object).getPageSize()//
                    + ", currentPage=" + ((Page) object).getCurrentPage()//
                    + ", pageNumberOffset=" + ((Page) object).getPageNumberOffset() + "]";
        }
        return object.toString();
    }

    protected static class ExecuteInfo {
        // query
        public int                 timeout            = -1;
        public int                 fetchSize          = 256;
        public ResultSetType       resultSetType      = ResultSetType.FORWARD_ONLY;
        public String              resultType;
        public String              resultMap;
        public boolean             caseInsensitive    = true;
        public MultipleResultsType multipleResultType = MultipleResultsType.LAST;
        // key
        public boolean             hasSelectKey;
        public boolean             useGeneratedKeys;
        public String              keyProperty;
        public String              parameterType;
        // page
        public Page                pageInfo;
        public PageSqlDialect      pageDialect;
        public boolean             pageResult;
        // data
        public Map<String, Object> data;
    }
}