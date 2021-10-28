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
import net.hasor.db.dal.dynamic.QuerySqlBuilder;
import net.hasor.db.dal.repository.ResultSetType;
import net.hasor.db.dal.repository.manager.DalDynamicContext;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.function.Supplier;

/**
 * 负责一般SQL调用的执行器
 * @version : 2021-07-20
 * @author 赵永春 (zyc@hasor.net)
 */
public class StatementExecute extends AbstractStatementExecute<Object> {
    private final DalDynamicContext context;
    private final ExecuteInfo       executeInfo;

    public StatementExecute(DalDynamicContext context, ExecuteInfo executeInfo, Supplier<Connection> connection) {
        super(connection);
        this.context = context;
        this.executeInfo = executeInfo;
    }

    protected Statement createStatement(Connection conn, ResultSetType resultSetType) throws SQLException {
        if (resultSetType == null || resultSetType.getResultSetType() == null) {
            return conn.createStatement();
        } else {
            int resultSetTypeInt = resultSetType.getResultSetType();
            return conn.createStatement(resultSetTypeInt, ResultSet.CONCUR_READ_ONLY);
        }
    }

    protected Object executeQuery(Connection con, QuerySqlBuilder queryBuilder) throws SQLException {
        try (Statement stat = createStatement(con, this.executeInfo.resultSetType)) {
            configStatement(this.executeInfo, stat);
            return executeQuery(stat, queryBuilder);
        }
    }

    protected Object executeQuery(Statement statement, QuerySqlBuilder queryBuilder) throws SQLException {

        DalResultSetExtractor extractor = super.buildExtractor(this.executeInfo, this.context);
        boolean retVal = statement.execute(queryBuilder.getSqlString());
        List<Object> result = extractor.doResult(retVal, statement);

        return getResult(result, this.executeInfo);
    }
}