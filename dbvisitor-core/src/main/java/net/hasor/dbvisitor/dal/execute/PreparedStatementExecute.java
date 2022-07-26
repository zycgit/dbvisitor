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
import net.hasor.cobble.ExceptionUtils;
import net.hasor.dbvisitor.dal.dynamic.DynamicContext;
import net.hasor.dbvisitor.dal.dynamic.SqlArg;
import net.hasor.dbvisitor.dal.repository.ResultSetType;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.PageSqlDialect;
import net.hasor.dbvisitor.dialect.SqlBuilder;
import net.hasor.dbvisitor.page.PageResult;
import net.hasor.dbvisitor.types.TypeHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
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
        BoundSql boundSql = sqlBuilder;
        BoundSql countSql = null;

        if (usingPage(executeInfo)) {
            PageSqlDialect dialect = executeInfo.pageDialect;
            int position = executeInfo.pageInfo.getFirstRecordPosition();
            int pageSize = executeInfo.pageInfo.getPageSize();
            boundSql = dialect.pageSql(sqlBuilder, position, pageSize);
            if (executeInfo.pageResult) {
                countSql = dialect.countSql(sqlBuilder);
            }
        }

        Object resultData = null;
        try (PreparedStatement ps = createPreparedStatement(con, boundSql.getSqlString(), executeInfo.resultSetType)) {
            configStatement(executeInfo, ps);
            resultData = executeQuery(ps, executeInfo, boundSql);
            if (countSql == null) {
                return resultData;
            }
        } catch (SQLException e) {
            logger.error("executeQuery failed, " + ExceptionUtils.getRootCauseMessage(e) + ", " + fmtBoundSql(boundSql, executeInfo.data), e);
            throw e;
        }

        int resultCount = 0;
        try (PreparedStatement ps = createPreparedStatement(con, countSql.getSqlString(), executeInfo.resultSetType)) {
            configStatement(executeInfo, ps);
            resultCount = executeCount(ps, countSql);
        } catch (SQLException e) {
            logger.error("executeCount failed, " + ExceptionUtils.getRootCauseMessage(e) + ", " + fmtBoundSql(countSql, executeInfo.data), e);
            throw e;
        }

        PageResult<Object> pageResult = new PageResult<>(executeInfo.pageInfo, resultCount);
        pageResult.setData((resultData instanceof List) ? (List<Object>) resultData : Collections.singletonList(resultData));
        return pageResult;
    }

    protected void statementSet(PreparedStatement ps, BoundSql boundSql) throws SQLException {
        List<SqlArg> sqlArgs = toArgs(boundSql);
        for (int i = 0; i < sqlArgs.size(); i++) {
            SqlArg arg = sqlArgs.get(i);
            TypeHandler typeHandler = arg.getTypeHandler();
            typeHandler.setParameter(ps, i + 1, arg.getValue(), arg.getJdbcType());
        }
    }

    protected Object executeQuery(PreparedStatement ps, ExecuteInfo executeInfo, BoundSql boundSql) throws SQLException {

        statementSet(ps, boundSql);
        DalResultSetExtractor extractor = super.buildExtractor(executeInfo);
        boolean retVal = ps.execute();
        List<Object> result = extractor.doResult(retVal, ps);

        return getResult(result, executeInfo);
    }

    protected int executeCount(PreparedStatement ps, BoundSql boundSql) throws SQLException {

        statementSet(ps, boundSql);
        try (ResultSet resultSet = ps.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getInt(1);
            } else {
                return -1;
            }
        }

    }
}
