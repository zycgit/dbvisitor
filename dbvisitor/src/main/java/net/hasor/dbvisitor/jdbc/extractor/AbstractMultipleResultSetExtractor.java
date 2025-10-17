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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.cobble.ref.LinkedCaseInsensitiveMap;
import net.hasor.dbvisitor.dynamic.ResultArg;
import net.hasor.dbvisitor.dynamic.ResultArgType;
import net.hasor.dbvisitor.dynamic.SqlBuilder;
import net.hasor.dbvisitor.dynamic.rule.ResultRule;
import net.hasor.dbvisitor.jdbc.CallableStatementCallback;
import net.hasor.dbvisitor.jdbc.ResultSetExtractor;
import net.hasor.dbvisitor.jdbc.RowCallbackHandler;
import net.hasor.dbvisitor.jdbc.RowMapper;
import net.hasor.dbvisitor.jdbc.mapper.BeanMappingRowMapper;
import net.hasor.dbvisitor.jdbc.mapper.SingleColumnRowMapper;
import net.hasor.dbvisitor.mapping.MappingRegistry;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

/**
 * {@link CallableStatementCallback} 接口实现类用于处理存储过程的参数传递和调用。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2024-09-29
 */
public abstract class AbstractMultipleResultSetExtractor {
    private static final Logger          logger                 = LoggerFactory.getLogger(AbstractMultipleResultSetExtractor.class);
    private final        List<ResultArg> resultArgs;
    private              boolean         resultsCaseInsensitive = false;
    protected            MappingRegistry mappingRegistry        = MappingRegistry.DEFAULT;

    public AbstractMultipleResultSetExtractor() {
        this.resultArgs = Collections.emptyList();
    }

    public AbstractMultipleResultSetExtractor(SqlBuilder buildSql) {
        this.resultArgs = buildSql.getResultArgs();
    }

    public boolean isResultsCaseInsensitive() {
        return this.resultsCaseInsensitive;
    }

    public void setResultsCaseInsensitive(final boolean resultsCaseInsensitive) {
        this.resultsCaseInsensitive = resultsCaseInsensitive;
    }

    protected TypeHandlerRegistry getTypeRegistry() {
        return this.mappingRegistry.getTypeRegistry();
    }

    protected Map<String, Object> createResultsMap() {
        if (this.isResultsCaseInsensitive()) {
            return new LinkedCaseInsensitiveMap<>();
        } else {
            return new LinkedHashMap<>();
        }
    }

    protected Map<String, Object> doInStatement(Statement s) throws SQLException {
        try {
            this.beforeStatement(s);

            // execute
            this.beforeExecute(s);
            boolean retVal = this.doExecute(s);
            this.afterExecute(s);
            if (logger.isTraceEnabled()) {
                logger.trace("doExecute() returned '" + retVal + "'");
            }

            // fetch result
            Map<String, Object> resultsMap = createResultsMap();
            this.beforeFetchResult(s, resultsMap);
            this.fetchResult(retVal, s, resultsMap);
            this.afterFetchResult(s, resultsMap);
            return resultsMap;
        } finally {
            this.afterStatement(s);
        }
    }

    protected abstract void beforeStatement(Statement s) throws SQLException;

    protected abstract void afterStatement(Statement s) throws SQLException;

    protected abstract void beforeExecute(Statement s) throws SQLException;

    protected abstract boolean doExecute(Statement s) throws SQLException;

    protected abstract void afterExecute(Statement s) throws SQLException;

    protected abstract void beforeFetchResult(Statement s, Map<String, Object> resultMap) throws SQLException;

    protected abstract void afterFetchResult(Statement s, Map<String, Object> resultMap) throws SQLException;

    protected void fetchResult(boolean retVal, Statement cs, Map<String, Object> resultMap) throws SQLException {
        // prepare Fetch
        ResultArg defaultRule = null;
        List<ResultArg> resultList = this.resultArgs;
        for (ResultArg r : this.resultArgs) {
            if (StringUtils.equalsIgnoreCase(r.getName(), ResultRule.FUNC_DEFAULT_RESULT)) {
                defaultRule = r;
            }
        }
        if (defaultRule == null) {
            defaultRule = new ResultArg(null, ResultArgType.Default, null, null, null, this.defaultExtractor());
        }

        // fetch ResultSet -- first ResultSet
        int resultIndex = 1;
        String resultName;
        Object resultValue;
        if (retVal) {
            try (ResultSet rs = cs.getResultSet()) {
                ResultArg arg = this.findResultArg(resultIndex, resultList, ResultArgType.ResultSet, defaultRule);
                resultName = resultParameterName(arg.getName(), "#result-set-" + resultIndex);
                resultValue = this.processResultSet(arg, rs);
            }
        } else {
            ResultArg arg = this.findResultArg(resultIndex, resultList, ResultArgType.ResultUpdate, defaultRule);
            resultName = resultParameterName(arg.getName(), "#update-count-" + resultIndex);
            resultValue = cs.getUpdateCount();
        }

        // fetch ResultSet -- more ResultSet
        resultMap.put(resultName, resultValue);
        while ((cs.getMoreResults()) || (cs.getUpdateCount() != -1)) {
            resultIndex++;
            int updateCount = cs.getUpdateCount();
            if (updateCount == -1) {
                ResultArg arg = this.findResultArg(resultIndex, resultList, ResultArgType.ResultSet, defaultRule);
                try (ResultSet rs = cs.getResultSet()) {
                    resultName = resultParameterName(arg.getName(), "#result-set-" + resultIndex);
                    resultValue = this.processResultSet(arg, rs);
                }
            } else {
                ResultArg arg = this.findResultArg(resultIndex, resultList, ResultArgType.ResultUpdate, defaultRule);
                resultName = resultParameterName(arg.getName(), "#update-count-" + resultIndex);
                resultValue = updateCount;
            }
            resultMap.put(resultName, resultValue);
        }
    }

    protected static String resultParameterName(String name, String defaultName) {
        return (name == null || StringUtils.isBlank(name)) ? defaultName : name;
    }

    protected ResultArg findResultArg(int resultIndex, List<ResultArg> resultList, ResultArgType findType, ResultArg defaultRule) {
        int idx = resultIndex - 1;
        if (idx < resultList.size()) {
            ResultArg arg = resultList.get(idx);
            if (arg != null && arg.getArgType() == findType) {
                return arg;
            }
        }
        return defaultRule;
    }

    /**
     * Process the given ResultSet from a stored procedure.
     */
    protected Object processResultSet(ResultArg arg, ResultSet rs) throws SQLException {
        if (rs == null) {
            return null;
        }
        if (arg == null) {
            return this.defaultExtractor().extractData(rs);
        }

        if (arg.getJavaType() != null) {
            if (this.getTypeRegistry().hasTypeHandler(arg.getJavaType())) {
                RowMapper<?> rowMapper = new SingleColumnRowMapper<>(arg.getJavaType());
                return new RowMapperResultSetExtractor<>(rowMapper).extractData(rs);
            } else {
                RowMapper<?> rowMapper = new BeanMappingRowMapper<>(arg.getJavaType(), this.mappingRegistry);
                return new RowMapperResultSetExtractor<>(rowMapper).extractData(rs);
            }
        } else if (arg.getRowMapper() != null) {
            RowMapper<?> rowMapper = arg.getRowMapper();
            return new RowMapperResultSetExtractor<>(rowMapper).extractData(rs);
        } else if (arg.getRowHandler() != null) {
            RowCallbackHandler rch = arg.getRowHandler();
            new RowCallbackHandlerResultSetExtractor(rch).extractData(rs);
            return "resultSet returned from stored procedure was processed";
        } else if (arg.getExtractor() != null) {
            return arg.getExtractor().extractData(rs);
        } else {
            return this.defaultExtractor().extractData(rs);
        }
    }

    protected ResultSetExtractor<?> defaultExtractor() {
        return new ColumnMapResultSetExtractor(0, this.getTypeRegistry(), this.resultsCaseInsensitive);
    }
}
