/*
 * Copyright 2008-2009 the original author or authors.
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
package net.hasor.db.dal.dynamic.nodes;
import net.hasor.db.dal.dynamic.DynamicContext;
import net.hasor.db.dialect.SqlBuilder;

import java.sql.SQLException;
import java.util.Map;

/**
 * <selectKey> 标签
 * @author 赵永春 (zyc@byshell.org)
 * @version : 2021-10-04
 */
public class SelectKeyDynamicSql extends ArrayDynamicSql {
    private final String statementType;
    private final int    timeout;
    private final String resultMap;
    private final String resultType;
    private final int    fetchSize;
    private final String resultSetType;
    private final String multipleResultType;
    private final String keyProperty;
    private final String keyColumn;
    private final String order;

    public SelectKeyDynamicSql(String statementType, int timeout, String resultMap, String resultType, int fetchSize,//
            String resultSetType, String multipleResultType, String keyProperty, String keyColumn, String order) {
        this.statementType = statementType;
        this.timeout = timeout;
        this.resultMap = resultMap;
        this.resultType = resultType;
        this.fetchSize = fetchSize;
        this.resultSetType = resultSetType;
        this.multipleResultType = multipleResultType;
        this.keyProperty = keyProperty;
        this.keyColumn = keyColumn;
        this.order = order;
    }

    public String getStatementType() {
        return this.statementType;
    }

    public int getTimeout() {
        return this.timeout;
    }

    public String getResultMap() {
        return this.resultMap;
    }

    public String getResultType() {
        return this.resultType;
    }

    public int getFetchSize() {
        return this.fetchSize;
    }

    public String getResultSetType() {
        return this.resultSetType;
    }

    public String getMultipleResultType() {
        return this.multipleResultType;
    }

    public String getKeyProperty() {
        return this.keyProperty;
    }

    public String getKeyColumn() {
        return this.keyColumn;
    }

    public String getOrder() {
        return this.order;
    }

    @Override
    public void buildQuery(Map<String, Object> data, DynamicContext context, SqlBuilder sqlBuilder) throws SQLException {
        // ignore
    }

    public void buildSqlQuery(Map<String, Object> data, DynamicContext context, SqlBuilder sqlBuilder) throws SQLException {
        super.buildQuery(data, context, sqlBuilder);
    }
}