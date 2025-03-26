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
package net.hasor.dbvisitor.mapper.def;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dynamic.DynamicSql;
import net.hasor.dbvisitor.dynamic.QueryContext;
import net.hasor.dbvisitor.dynamic.SqlArgSource;
import net.hasor.dbvisitor.dynamic.SqlBuilder;
import net.hasor.dbvisitor.dynamic.logic.ArrayDynamicSql;
import net.hasor.dbvisitor.mapper.StatementType;

import java.sql.SQLException;
import java.util.Objects;
import java.util.function.Function;

/**
 * Segment SqlConfig
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-06-19
 */
public abstract class SqlConfig implements DynamicSql, ConfigKeys {
    protected ArrayDynamicSql target;
    private   StatementType   statementType = StatementType.Prepared;
    private   int             timeout       = -1;

    public SqlConfig(ArrayDynamicSql target, Function<String, String> config) {
        this.target = Objects.requireNonNull(target, "target is null.");

        if (config != null) {
            this.statementType = config.andThen(s -> StatementType.valueOfCode(s, StatementType.Prepared)).apply(STATEMENT_TYPE);
            this.timeout = Integer.parseInt(config.andThen(s -> StringUtils.isBlank(s) ? "-1" : s).apply(TIMEOUT));
        }
    }

    public StatementType getStatementType() {
        return this.statementType;
    }

    public void setStatementType(StatementType statementType) {
        this.statementType = statementType;
    }

    public int getTimeout() {
        return this.timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public abstract QueryType getType();

    @Override
    public boolean isHaveInjection() {
        return this.target.isHaveInjection();
    }

    @Override
    public void buildQuery(SqlArgSource data, QueryContext context, SqlBuilder sqlBuilder) throws SQLException {
        this.target.buildQuery(data, context, sqlBuilder);
    }
}