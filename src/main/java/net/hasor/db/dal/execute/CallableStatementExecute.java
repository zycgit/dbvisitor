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
import net.hasor.db.dal.dynamic.DalBoundSql.SqlArg;
import net.hasor.db.dal.dynamic.DynamicContext;
import net.hasor.db.dal.dynamic.QuerySqlBuilder;
import net.hasor.db.dal.dynamic.SqlMode;
import net.hasor.db.dal.repository.ResultSetType;
import net.hasor.db.types.TypeHandler;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 负责存储过程调用的执行器
 * @version : 2021-07-20
 * @author 赵永春 (zyc@hasor.net)
 */
public class CallableStatementExecute extends AbstractStatementExecute<Object> {
    public CallableStatementExecute(DynamicContext context) {
        super(context);
    }

    protected CallableStatement createCallableStatement(Connection conn, String queryString, ResultSetType resultSetType) throws SQLException {
        if (resultSetType == null || resultSetType.getResultSetType() == null) {
            return conn.prepareCall(queryString);
        } else {
            int resultSetTypeInt = resultSetType.getResultSetType();
            return conn.prepareCall(queryString, resultSetTypeInt, ResultSet.CONCUR_READ_ONLY);
        }
    }

    protected boolean usingPage(ExecuteInfo executeInfo) {
        return false;
    }

    @Override
    protected Object executeQuery(Connection con, ExecuteInfo executeInfo, QuerySqlBuilder queryBuilder) throws SQLException {
        if (!con.getMetaData().supportsStoredProcedures()) {
            throw new UnsupportedOperationException("target DataSource Unsupported.");
        }

        String sqlString = queryBuilder.getSqlString();
        try (CallableStatement ps = createCallableStatement(con, sqlString, executeInfo.resultSetType)) {
            configStatement(executeInfo, ps);
            return executeQuery(ps, executeInfo, queryBuilder);
        }
    }

    protected Object executeQuery(CallableStatement cs, ExecuteInfo executeInfo, QuerySqlBuilder queryBuilder) throws SQLException {
        List<SqlArg> sqlArg = queryBuilder.getSqlArg();

        int sqlColIndex = 1;
        for (int i = 0; i < sqlArg.size(); i++) {
            SqlArg arg = sqlArg.get(i);
            TypeHandler<Object> typeHandler = (TypeHandler<Object>) arg.getTypeHandler();

            switch (arg.getSqlMode()) {
                case In: {
                    typeHandler.setParameter(cs, sqlColIndex, arg.getValue(), arg.getJdbcType());
                    break;
                }
                case InOut: {
                    typeHandler.setParameter(cs, sqlColIndex, arg.getValue(), arg.getJdbcType());
                    cs.registerOutParameter(sqlColIndex, arg.getJdbcType());
                    break;
                }
                case Out: {
                    cs.registerOutParameter(sqlColIndex, arg.getJdbcType());
                    break;
                }
            }

            sqlColIndex++;
        }

        // execute call
        boolean retVal = cs.execute();

        // fetch output
        Map<String, Object> resultOut = new LinkedHashMap<>();
        boolean keepAll = executeInfo.resultOut.contains("*");
        for (int i = 0; i < sqlArg.size(); i++) {
            SqlArg arg = sqlArg.get(i);
            TypeHandler<Object> argHandler = (TypeHandler<Object>) arg.getTypeHandler();

            if (arg.getSqlMode() != SqlMode.Out) {
                continue;
            }

            String paramName = arg.getName();
            if (StringUtils.isBlank(paramName)) {
                paramName = "out_" + i;
            }

            if (keepAll || executeInfo.resultOut.contains(paramName)) {
                Object resultValue = argHandler.getResult(cs, i + 1);
                resultOut.put(paramName, resultValue);
            }
        }

        // keepAll
        DalResultSetExtractor extractor = super.buildExtractor(executeInfo);
        List<Object> resultSet = extractor.doResult(retVal, cs);
        if (keepAll) {
            for (int i = 0; i < resultSet.size(); i++) {
                Object result = resultSet.get(i);
                resultOut.put("result-" + i, result);
            }
            return resultOut;
        }

        if (!executeInfo.resultOut.isEmpty()) {
            // 如果配置了 resultOut 那么一定是返回 resultOut 中的数据
            if (resultOut.size() == 1 && executeInfo.resultOut.size() == 1) {
                return resultOut.entrySet().iterator().next().getValue();
            } else {
                return resultOut;
            }
        } else {
            return getResult(resultSet, executeInfo);
        }
    }

}
