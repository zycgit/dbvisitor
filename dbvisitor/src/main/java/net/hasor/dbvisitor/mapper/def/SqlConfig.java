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
 * 该抽象类表示 SQL 配置，实现了 {@link DynamicSql} 接口，
 * 为 SQL 相关配置提供基础属性和方法。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-06-19
 */
public abstract class SqlConfig implements DynamicSql, ConfigKeys {
    protected ArrayDynamicSql target;
    private   StatementType   statementType = StatementType.Prepared;
    private   int             timeout       = -1;

    /**
     * 构造函数，用于初始化 SqlConfig 对象。
     * @param target 动态 SQL 目标对象，不能为 null。
     * @param config 一个函数，用于从配置中获取相应的值，可为 null。
     */
    public SqlConfig(ArrayDynamicSql target, Function<String, String> config) {
        this.target = Objects.requireNonNull(target, "target is null.");

        if (config != null) {
            this.statementType = config.andThen(s -> StatementType.valueOfCode(s, StatementType.Prepared)).apply(STATEMENT_TYPE);
            this.timeout = Integer.parseInt(config.andThen(s -> StringUtils.isBlank(s) ? "-1" : s).apply(TIMEOUT));
        }
    }

    /**
     * 用于获取查询类型，具体实现由子类完成。
     * @return 返回查询类型。
     */
    public abstract QueryType getType();

    @Override
    public boolean isHaveInjection() {
        return this.target.isHaveInjection();
    }

    @Override
    public void buildQuery(SqlArgSource data, QueryContext context, SqlBuilder sqlBuilder) throws SQLException {
        this.target.buildQuery(data, context, sqlBuilder);
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

}