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
import net.hasor.cobble.ArrayUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dynamic.ResultArg;
import net.hasor.dbvisitor.dynamic.ResultArgType;
import net.hasor.dbvisitor.dynamic.SqlBuilder;
import net.hasor.dbvisitor.dynamic.SqlMode;
import net.hasor.dbvisitor.jdbc.CallableStatementCallback;
import net.hasor.dbvisitor.jdbc.core.StatementSetterUtils;
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * {@link CallableStatementCallback} 接口实现类用于处理存储过程的参数传递和调用。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2024-09-29
 */
public class CallableMultipleResultSetExtractor extends AbstractMultipleResultSetExtractor implements CallableStatementCallback<Map<String, Object>> {
    private final Object[] useArgs;

    public CallableMultipleResultSetExtractor() {
        super();
        this.useArgs = ArrayUtils.EMPTY_OBJECT_ARRAY;
    }

    public CallableMultipleResultSetExtractor(SqlBuilder buildSql) {
        super(buildSql);
        this.useArgs = buildSql.getArgs();
    }

    @Override
    public Map<String, Object> doInCallableStatement(CallableStatement cs) throws SQLException {
        return super.doInStatement(cs);
    }

    public List<Object> doInCallableStatementAsList(CallableStatement cs) throws SQLException {
        return new ArrayList<>(super.doInStatement(cs).values());
    }

    @Override
    protected void beforeStatement(Statement s) {

    }

    @Override
    protected void beforeExecute(Statement s) throws SQLException {
        if (ArrayUtils.isNotEmpty(this.useArgs)) {
            TypeHandlerRegistry registry = this.getTypeRegistry();
            for (int i = 0; i < this.useArgs.length; i++) {
                registry.setParameterValue((CallableStatement) s, i + 1, this.useArgs[i]);
            }
        }
    }

    @Override
    protected boolean doExecute(Statement s) throws SQLException {
        return ((CallableStatement) s).execute();
    }

    @Override
    protected void afterExecute(Statement s) {

    }

    @Override
    protected void beforeFetchResult(Statement s, Map<String, Object> resultMap) throws SQLException {
        // fetch output
        for (int i = 1; i <= this.useArgs.length; i++) {
            Object arg = this.useArgs[i - 1];
            if (!(arg instanceof SqlArg)) {
                continue;
            }
            SqlMode sqlMode = ((SqlArg) arg).getSqlMode();
            if (sqlMode == null || !sqlMode.isOut()) {
                continue;
            }

            SqlArg sqlArg = (SqlArg) arg;
            String asName = sqlArg.getAsName();
            String argName = sqlArg.getName();
            String name;
            if (StringUtils.isNotBlank(asName)) {
                name = asName;
            } else if (StringUtils.isNotBlank(argName)) {
                name = argName;
            } else {
                name = "#out-" + i;
            }

            if (sqlArg.getSqlMode() == SqlMode.Cursor) {
                ResultSet rs = (ResultSet) ((CallableStatement) s).getObject(i);
                ResultArg procedureArg = new ResultArg(sqlArg.getName(), ResultArgType.ResultSet, sqlArg.getJavaType(), sqlArg.getRowMapper(), sqlArg.getRowHandler(), sqlArg.getExtractor());
                Object resultValue = this.processResultSet(procedureArg, rs);
                resultMap.put(name, resultValue);
            } else {
                TypeHandlerRegistry registry = this.getTypeRegistry();
                Object resultValue = registry.getParameterValue((CallableStatement) s, i, sqlArg);
                resultMap.put(name, resultValue);
            }
        }
    }

    @Override
    protected void afterFetchResult(Statement s, Map<String, Object> resultMap) {

    }

    @Override
    protected void afterStatement(Statement s) {
        StatementSetterUtils.cleanupParameters(this.useArgs);
    }

}
