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
import net.hasor.dbvisitor.dal.repository.ResultSetType;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dynamic.RegistryManager;
import net.hasor.dbvisitor.dynamic.SqlBuilder;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * 负责一般SQL调用的执行器
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-07-20
 */
public class StatementExecute extends AbstractStatementExecute<Object> {
    public StatementExecute(RegistryManager context) {
        super(context);
    }

    protected Statement createStatement(Connection conn, ResultSetType resultSetType) throws SQLException {
        if (resultSetType == null || resultSetType.getResultSetType() == null) {
            return conn.createStatement();
        } else {
            int resultSetTypeInt = resultSetType.getResultSetType();
            return conn.createStatement(resultSetTypeInt, ResultSet.CONCUR_READ_ONLY);
        }
    }

    @Override
    protected Object executeQuery(Connection con, ExecuteInfo executeInfo, SqlBuilder sqlBuilder) throws SQLException {
        if (usingPage(executeInfo)) {
            throw new UnsupportedOperationException("Statement does not support page query, please using PreparedStatement.");
        }

        try (Statement stat = createStatement(con, executeInfo.resultSetType)) {
            configStatement(executeInfo, stat);
            return executeQuery(stat, executeInfo, sqlBuilder);
        }
    }

    protected Object executeQuery(Statement statement, ExecuteInfo executeInfo, BoundSql boundSql) throws SQLException {
        if (logger.isTraceEnabled()) {
            logger.trace(fmtBoundSql(boundSql).toString());
        }

        String querySQL = boundSql.getSqlString();

        DalResultSetExtractor extractor = super.buildExtractor(executeInfo);
        boolean retVal;
        try {
            retVal = statement.execute(querySQL);
        } catch (SQLException e) {
            logger.error("executeQuery failed, " + fmtBoundSql(boundSql, executeInfo.data), e);
            throw e;
        }
        List<Object> result = extractor.doResult(retVal, statement);

        return getResult(result, executeInfo);
    }
}