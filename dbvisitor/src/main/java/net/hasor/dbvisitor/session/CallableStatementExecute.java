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
package net.hasor.dbvisitor.session;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.Page;
import net.hasor.dbvisitor.dynamic.RegistryManager;
import net.hasor.dbvisitor.dynamic.SqlBuilder;
import net.hasor.dbvisitor.mapper.def.DqlConfig;
import net.hasor.dbvisitor.mapper.def.SqlConfig;
import net.hasor.dbvisitor.template.jdbc.extractor.CallableMultipleResultSetExtractor;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import java.sql.*;
import java.util.Map;

/**
 * 负责存储过程调用的执行器
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-07-20
 */
public class CallableStatementExecute extends AbstractStatementExecute<Object> {
    public CallableStatementExecute(RegistryManager registry) {
        super(registry);
    }

    @Override
    protected void doCheck(Connection conn, SqlConfig config, Map<String, Object> data, Page pageInfo) throws SQLException {
        super.doCheck(conn, config, data, pageInfo);
        if (!conn.getMetaData().supportsStoredProcedures()) {
            throw new UnsupportedOperationException("procedure DataSource Unsupported.");
        }
        if (SessionHelper.usingPage(pageInfo)) {
            throw new UnsupportedOperationException("CALLABLE does not support paging query, please using PREPARED.");
        }
    }

    @Override
    protected CallableStatement createStatement(Connection conn, SqlConfig config, BoundSql execSql) throws SQLException {
        if (config instanceof DqlConfig) {
            int resultSetTypeInt = ((DqlConfig) config).getResultSetType().getResultSetType();
            return conn.prepareCall(execSql.getSqlString(), resultSetTypeInt, ResultSet.CONCUR_READ_ONLY);
        } else {
            return conn.prepareCall(execSql.getSqlString());
        }
    }

    @Override
    protected boolean executeQuery(Statement stat, SqlConfig config, BoundSql execSql) throws SQLException {
        try {
            CallableStatement cs = (CallableStatement) stat;
            Object[] args = execSql.getArgs();
            for (int j = 0; j < args.length; j++) {
                TypeHandlerRegistry.DEFAULT.setParameterValue(cs, j + 1, args[j]);
            }

            return cs.execute();
        } catch (SQLException e) {
            logger.error("executeQuery failed, " + SessionHelper.fmtBoundSql(execSql), e);
            throw e;
        }
    }

    @Override
    protected Map<String, Object> multipleResultFetch(SqlBuilder buildSql, Statement stat, boolean retVal) throws SQLException {
        return new StatementCallableMultipleResultSetExtractor(buildSql).fetchResult(retVal, stat);
    }

    private static class StatementCallableMultipleResultSetExtractor extends CallableMultipleResultSetExtractor {
        public StatementCallableMultipleResultSetExtractor(SqlBuilder buildSql) {
            super(buildSql);
        }

        public Map<String, Object> fetchResult(boolean retVal, Statement s) throws SQLException {
            try {
                Map<String, Object> resultsMap = createResultsMap();
                this.beforeFetchResult(s, resultsMap);
                this.fetchResult(retVal, s, resultsMap);
                this.afterFetchResult(s, resultsMap);
                return resultsMap;
            } finally {
                this.afterStatement(s);
            }
        }
    }

}
