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
import net.hasor.db.dal.dynamic.DynamicContext;
import net.hasor.db.dal.dynamic.SqlArg;
import net.hasor.db.dal.repository.ResultSetType;
import net.hasor.db.dialect.BoundSql;
import net.hasor.db.dialect.PageSqlDialect;
import net.hasor.db.dialect.SqlBuilder;
import net.hasor.db.types.TypeHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 负责参数化SQL调用的执行器
 * @version : 2021-07-20
 * @author 赵永春 (zyc@hasor.net)
 */
public class PreparedStatementExecute extends AbstractStatementExecute<Object> {
    public PreparedStatementExecute(DynamicContext context) {
        super(context);
    }

    protected PreparedStatement createPreparedStatement(Connection conn, String queryString, ResultSetType resultSetType) throws SQLException {
        if (resultSetType == null || resultSetType.getResultSetType() == null) {
            return conn.prepareStatement(queryString);
        } else {
            int resultSetTypeInt = resultSetType.getResultSetType();
            return conn.prepareStatement(queryString, resultSetTypeInt, ResultSet.CONCUR_READ_ONLY);
        }
    }

    @Override
    protected Object executeQuery(Connection con, ExecuteInfo executeInfo, SqlBuilder sqlBuilder) throws SQLException {
        BoundSql querySql = sqlBuilder;

        if (usingPage(executeInfo)) {
            PageSqlDialect dialect = getContext().getDialect();
            int position = executeInfo.pageInfo.getFirstRecordPosition();
            int pageSize = executeInfo.pageInfo.getPageSize();
            querySql = dialect.pageSql(sqlBuilder, position, pageSize);
        }

        try (PreparedStatement ps = createPreparedStatement(con, querySql.getSqlString(), executeInfo.resultSetType)) {
            configStatement(executeInfo, ps);
            return executeQuery(ps, executeInfo, querySql);
        }
    }

    protected Object executeQuery(PreparedStatement ps, ExecuteInfo executeInfo, BoundSql queryBuilder) throws SQLException {

        List<SqlArg> sqlArgs = toArgs(queryBuilder);
        for (int i = 0; i < sqlArgs.size(); i++) {
            SqlArg arg = sqlArgs.get(i);
            TypeHandler typeHandler = arg.getTypeHandler();
            typeHandler.setParameter(ps, i + 1, arg.getValue(), arg.getJdbcType());
        }

        DalResultSetExtractor extractor = super.buildExtractor(executeInfo);
        boolean retVal = ps.execute();
        List<Object> result = extractor.doResult(retVal, ps);

        return getResult(result, executeInfo);
    }
}
