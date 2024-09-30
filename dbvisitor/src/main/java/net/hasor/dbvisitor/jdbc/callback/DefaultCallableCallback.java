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
package net.hasor.dbvisitor.jdbc.callback;
import net.hasor.cobble.ArrayUtils;
import net.hasor.cobble.ClassUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.logging.LoggerFactory;
import net.hasor.cobble.ref.LinkedCaseInsensitiveMap;
import net.hasor.dbvisitor.dynamic.DynamicContext;
import net.hasor.dbvisitor.dynamic.SqlMode;
import net.hasor.dbvisitor.dynamic.rule.ReturnResultRule;
import net.hasor.dbvisitor.dynamic.segment.DefaultSqlSegment;
import net.hasor.dbvisitor.dynamic.segment.DefaultSqlSegment.RuleInfo;
import net.hasor.dbvisitor.jdbc.CallableStatementCallback;
import net.hasor.dbvisitor.jdbc.ResultSetExtractor;
import net.hasor.dbvisitor.jdbc.RowCallbackHandler;
import net.hasor.dbvisitor.jdbc.RowMapper;
import net.hasor.dbvisitor.jdbc.core.StatementSetterUtils;
import net.hasor.dbvisitor.jdbc.extractor.ColumnMapResultSetExtractor;
import net.hasor.dbvisitor.jdbc.extractor.RowCallbackHandlerResultSetExtractor;
import net.hasor.dbvisitor.jdbc.extractor.RowMapperResultSetExtractor;
import net.hasor.dbvisitor.jdbc.mapper.MappingRowMapper;
import net.hasor.dbvisitor.jdbc.mapper.SingleColumnRowMapper;
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link CallableStatementCallback} 接口实现类用于处理存储过程的参数传递和调用。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2024-09-29
 */
public class DefaultCallableCallback implements CallableStatementCallback<Map<String, Object>> {
    private static final Logger            logger                 = LoggerFactory.getLogger(DefaultCallableCallback.class);
    private final        DefaultSqlSegment parsedSql;
    private final        Object[]          parameters;
    private              boolean           resultsCaseInsensitive = false;
    private              DynamicContext    registry               = DynamicContext.DEFAULT;

    public DefaultCallableCallback(DefaultSqlSegment parsedSql, Object[] parameters) {
        this.parsedSql = parsedSql;
        this.parameters = parameters;
    }

    public boolean isResultsCaseInsensitive() {
        return this.resultsCaseInsensitive;
    }

    public void setResultsCaseInsensitive(final boolean resultsCaseInsensitive) {
        this.resultsCaseInsensitive = resultsCaseInsensitive;
    }

    public DynamicContext getRegistry() {
        return this.registry;
    }

    public void setRegistry(DynamicContext registry) {
        this.registry = registry;
    }

    protected Map<String, Object> createResultsMap() {
        if (this.isResultsCaseInsensitive()) {
            return new LinkedCaseInsensitiveMap<>();
        } else {
            return new LinkedHashMap<>();
        }
    }

    @Override
    public Map<String, Object> doInCallableStatement(CallableStatement cs) throws SQLException {
        try {
            // config parameters
            if (ArrayUtils.isNotEmpty(this.parameters)) {
                for (int i = 0; i < this.parameters.length; i++) {
                    this.registry.getTypeRegistry().setParameterValue(cs, i + 1, this.parameters[i]);
                }
            }

            // execute
            boolean retVal = cs.execute();
            if (logger.isTraceEnabled()) {
                logger.trace("CallableStatement.execute() returned '" + retVal + "'");
            }

            // fetch result
            return fetchResult(retVal, cs, createResultsMap());
        } finally {
            StatementSetterUtils.cleanupParameters(this.parameters);
        }
    }

    protected Map<String, Object> fetchResult(boolean retVal, CallableStatement cs, Map<String, Object> resultMap) throws SQLException {
        // fetch output
        for (int i = 1; i <= this.parameters.length; i++) {
            Object arg = this.parameters[i - 1];
            if (!(arg instanceof SqlArg)) {
                continue;
            }
            SqlMode sqlMode = ((SqlArg) arg).getSqlMode();
            if (sqlMode == null || !sqlMode.isOut()) {
                continue;
            }

            SqlArg sqlArg = (SqlArg) arg;
            String outName = sqlArg.getName();
            outName = StringUtils.isNotBlank(outName) ? outName : "#out-" + i;

            if (sqlArg.getSqlMode() == SqlMode.Cursor) {
                ResultSet rs = (ResultSet) cs.getObject(i);
                ProcedureArg procedureArg = new ProcedureArg(sqlArg.getName(), sqlArg.getJavaType(), sqlArg.getMapper(), sqlArg.getHandler(), sqlArg.getExtractor());
                Object resultValue = this.processResultSet(procedureArg, rs);
                resultMap.put(outName, resultValue);
            } else {
                TypeHandlerRegistry typeRegistry = this.registry.getTypeRegistry();
                Object resultValue = typeRegistry.getParameterValue(cs, i, sqlArg);
                resultMap.put(outName, resultValue);
            }
        }

        // prepare Fetch
        ProcedureArg defaultRule = null;
        List<ProcedureArg> resultList = new ArrayList<>();
        for (RuleInfo r : this.parsedSql.getRuleList()) {
            if (StringUtils.equalsIgnoreCase(r.getRule(), ReturnResultRule.FUNC_RETURN) || StringUtils.equalsIgnoreCase(r.getRule(), ReturnResultRule.FUNC_RESULT)) {
                resultList.add(parserConfig(r));
            } else if (StringUtils.equalsIgnoreCase(r.getRule(), ReturnResultRule.FUNC_DEFAULT_RESULT)) {
                defaultRule = parserConfig(r);
            }
        }
        if (defaultRule == null) {
            defaultRule = new ProcedureArg(null, null, null, null, this.defaultExtractor());
        }

        // fetch ResultSet -- first ResultSet
        int resultIndex = 1;
        String resultName;
        Object resultValue;
        if (retVal) {
            try (ResultSet rs = cs.getResultSet()) {
                ProcedureArg arg = this.findProcedureArg(resultIndex, resultList, defaultRule);
                resultName = resultParameterName(arg.getName(), "#result-set-" + resultIndex);
                resultValue = this.processResultSet(arg, rs);
            }
        } else {
            ProcedureArg arg = this.findProcedureArg(resultIndex, resultList, defaultRule);
            resultName = resultParameterName(arg.getName(), "#update-count-" + resultIndex);
            resultValue = cs.getUpdateCount();
        }

        // fetch ResultSet -- more ResultSet
        resultMap.put(resultName, resultValue);
        while ((cs.getMoreResults()) || (cs.getUpdateCount() != -1)) {
            resultIndex++;
            ProcedureArg arg = this.findProcedureArg(resultIndex, resultList, defaultRule);
            int updateCount = cs.getUpdateCount();
            if (updateCount == -1) {
                try (ResultSet rs = cs.getResultSet()) {
                    resultName = resultParameterName(arg.getName(), "#result-set-" + resultIndex);
                    resultValue = this.processResultSet(arg, rs);
                }
            } else {
                resultName = resultParameterName(arg.getName(), "#update-count-" + resultIndex);
                resultValue = updateCount;
            }
            resultMap.put(resultName, resultValue);
        }
        return resultMap;
    }

    protected static String resultParameterName(String name, String defaultName) {
        return (name == null || StringUtils.isBlank(name)) ? defaultName : name;
    }

    protected ProcedureArg findProcedureArg(int resultIndex, List<ProcedureArg> resultList, ProcedureArg defaultRule) {
        int idx = resultIndex - 1;
        if (idx < resultList.size()) {
            ProcedureArg arg = resultList.get(idx);
            if (arg != null) {
                return arg;
            }
        }
        return defaultRule;
    }

    /**
     * Process the given ResultSet from a stored procedure.
     */
    protected Object processResultSet(ProcedureArg arg, ResultSet rs) throws SQLException {
        if (rs == null) {
            return null;
        }
        if (arg == null) {
            return this.defaultExtractor().extractData(rs);
        }

        if (arg.getJavaType() != null) {
            if (this.registry.getTypeRegistry().hasTypeHandler(arg.getJavaType())) {
                RowMapper<?> rowMapper = new SingleColumnRowMapper<>(arg.getJavaType());
                return new RowMapperResultSetExtractor<>(rowMapper).extractData(rs);
            } else {
                RowMapper<?> rowMapper = new MappingRowMapper<>(arg.getJavaType());
                return new RowMapperResultSetExtractor<>(rowMapper).extractData(rs);
            }
        } else if (arg.getRowMapper() != null) {
            RowMapper<?> rowMapper = arg.getRowMapper();
            return new RowMapperResultSetExtractor<>(rowMapper).extractData(rs);
        } else if (arg.getHandler() != null) {
            RowCallbackHandler rch = arg.getHandler();
            new RowCallbackHandlerResultSetExtractor(rch).extractData(rs);
            return "resultSet returned from stored procedure was processed";
        } else if (arg.getExtractor() != null) {
            return arg.getExtractor().extractData(rs);
        } else {
            return this.defaultExtractor().extractData(rs);
        }
    }

    protected ProcedureArg parserConfig(RuleInfo content) {
        // restore body
        String body = "";
        if (content.getActiveExpr() != null) {
            body = content.getActiveExpr();
            if (content.getRuleValue() != null) {
                body += ",";
            }
        }
        if (content.getRuleValue() != null) {
            body += content.getRuleValue();
        }

        // parser config to Map.
        Map<String, String> config = new LinkedCaseInsensitiveMap<>();
        for (String item : body.split(",")) {
            String[] kv = item.split("=");
            if (kv.length != 2) {
                throw new IllegalArgumentException("analysisSQL failed, config must be 'key = value' , '" + body + "' with '" + item + "'");
            }
            if (StringUtils.isNotBlank(kv[0])) {
                config.put(kv[0].trim(), kv[1].trim());
            }
        }

        String name = config.get(ProcedureArg.CFG_KEY_NAME);
        ProcedureArg arg = new ProcedureArg(name);

        Class<?> javaType = convertJavaType(this.registry, config.get(ProcedureArg.CFG_KEY_JAVA_TYPE));
        if (javaType != null) {
            arg.setJavaType(javaType);
            return arg;
        }

        Class<?> mapperType = convertJavaType(this.registry, config.get(ProcedureArg.CFG_KEY_MAPPER));
        if (mapperType != null) {
            arg.setRowMapper(ClassUtils.newInstance(mapperType));
            return arg;
        }

        Class<?> handlerType = convertJavaType(this.registry, config.get(ProcedureArg.CFG_KEY_HANDLER));
        if (handlerType != null) {
            arg.setHandler(ClassUtils.newInstance(handlerType));
            return arg;
        }

        Class<?> extractorType = convertJavaType(this.registry, config.get(ProcedureArg.CFG_KEY_EXTRACTOR));
        if (extractorType != null) {
            arg.setExtractor(ClassUtils.newInstance(extractorType));
            return arg;
        }

        arg.setExtractor(this.defaultExtractor());
        return arg;
    }

    protected ResultSetExtractor<?> defaultExtractor() {
        return new ColumnMapResultSetExtractor(0, this.registry.getTypeRegistry(), this.resultsCaseInsensitive);
    }

    private Class<?> convertJavaType(DynamicContext context, String javaType) {
        try {
            if (StringUtils.isNotBlank(javaType)) {
                return context.loadClass(javaType);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
