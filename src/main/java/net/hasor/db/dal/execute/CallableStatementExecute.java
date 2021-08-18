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
import net.hasor.db.dal.dynamic.BuilderContext;
import net.hasor.db.dal.dynamic.DalBoundSql.SqlArg;
import net.hasor.db.dal.dynamic.QuerySqlBuilder;
import net.hasor.db.dal.repository.config.ResultSetType;
import net.hasor.db.jdbc.SqlParameter;
import net.hasor.db.jdbc.SqlParameterUtils;
import net.hasor.db.jdbc.core.JdbcTemplate;
import net.hasor.db.jdbc.extractor.MultipleProcessType;
import net.hasor.db.jdbc.extractor.SimpleCallableStatementCallback;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * 负责存储过程调用的执行器
 * @version : 2021-07-20
 * @author 赵永春 (zyc@hasor.net)
 */
public class CallableStatementExecute extends AbstractStatementExecute<Object> {
    public CallableStatementExecute(BuilderContext builderContext, ExecuteInfo executeInfo, JdbcTemplate jdbcTemplate) {
        super(builderContext, executeInfo, jdbcTemplate);
    }

    protected CallableStatement createCallableStatement(Connection conn, String queryString, ResultSetType resultSetType) throws SQLException {
        if (resultSetType == null || resultSetType.getResultSetType() == null) {
            return conn.prepareCall(queryString);
        } else {
            int resultSetTypeInt = resultSetType.getResultSetType();
            return conn.prepareCall(queryString, resultSetTypeInt, ResultSet.CONCUR_READ_ONLY);
        }
    }

    protected Object executeQuery(Connection con, QuerySqlBuilder queryBuilder) throws SQLException {
        ExecuteInfo executeInfo = getExecuteInfo();
        if (!con.getMetaData().supportsStoredProcedures()) {
            throw new UnsupportedOperationException("target DataSource Unsupported.");
        }
        //
        String sqlString = queryBuilder.getSqlString();
        try (CallableStatement ps = createCallableStatement(con, sqlString, executeInfo.resultSetType)) {
            return executeQuery(ps, queryBuilder);
        }
    }

    protected Object executeQuery(CallableStatement cs, QuerySqlBuilder queryBuilder) throws SQLException {
        ExecuteInfo executeInfo = getExecuteInfo();
        configStatement(executeInfo, cs);
        //
        List<SqlArg> sqlArgList = queryBuilder.getSqlArg();
        List<SqlParameter> paramList = new ArrayList<>();
        Map<String, SqlArg> paramMap = new HashMap<>();
        for (int i = 0; i < sqlArgList.size(); i++) {
            String paramName = "param_" + i;
            SqlArg arg = sqlArgList.get(i);
            paramMap.put(paramName, arg);
            switch (arg.getSqlMode()) {
                case In: {
                    paramList.add(SqlParameterUtils.withInput(arg.getValue(), arg.getJdbcType(), arg.getTypeHandler()));
                    break;
                }
                case Out: {
                    paramList.add(SqlParameterUtils.withOutput(paramName, arg.getJdbcType(), arg.getTypeHandler()));
                    break;
                }
                case InOut: {
                    paramList.add(SqlParameterUtils.withInOut(paramName, arg.getValue(), arg.getJdbcType(), arg.getTypeHandler()));
                    break;
                }
                default:
                    throw new UnsupportedOperationException("SqlMode " + arg.getSqlMode() + " Unsupported.");
            }
        }
        //
        MultipleProcessType multipleType = MultipleProcessType.valueOf(executeInfo.multipleResultType.getTypeName());
        SimpleCallableStatementCallback callback = new SimpleCallableStatementCallback(multipleType, paramList);
        Map<String, Object> result = callback.doInCallableStatement(cs);
        if (result.isEmpty()) {
            return null;
        }
        //
        Map<String, Object> resultMap = new LinkedHashMap<>();
        result.forEach((key, value) -> {
            if (paramMap.containsKey(key)) {
                SqlArg sqlArg = paramMap.get(key);
                String argExpr = sqlArg.getExpr();
                if (StringUtils.isBlank(argExpr)) {
                    int argIndex = sqlArgList.indexOf(sqlArg);
                    throw new IllegalArgumentException("parameter[index = " + argIndex + "] container name not set.");
                }
                resultMap.put(argExpr, value);
            } else {
                resultMap.put(key, value);
            }
        });
        //executeInfo.resultMap
        if (multipleType == MultipleProcessType.FIRST) {
            if (sqlArgList.isEmpty()) {
                return result.entrySet().iterator().next().getValue();
            } else {
                String expr = sqlArgList.get(0).getExpr();
                return resultMap.get(expr);
            }
        } else if (multipleType == MultipleProcessType.LAST) {
            if (sqlArgList.isEmpty()) {
                return result.entrySet().iterator().next().getValue();
            } else {
                String expr = sqlArgList.get(sqlArgList.size() - 1).getExpr();
                return resultMap.get(expr);
            }
        } else {
            return resultMap;
        }
    }
}
