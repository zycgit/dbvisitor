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
package net.hasor.dbvisitor.dynamic.segment;
import net.hasor.dbvisitor.dynamic.QueryContext;
import net.hasor.dbvisitor.dynamic.SqlArgSource;
import net.hasor.dbvisitor.dynamic.SqlBuilder;
import net.hasor.dbvisitor.dynamic.rule.ArgRule;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;

/**
 * 命名 SQL 片段实现类，用于处理带配置参数的 SQL 片段
 * 功能特点：
 * 1. 实现 {@link SqlSegment} 接口，提供 SQL 片段构建功能
 * 2. 支持带配置参数的 SQL 片段处理
 * 3. 使用 {@link ArgRule} 处理参数绑定
 * @author 赵永春 (zyc@hasor.net)
 * @version 2024-09-25
 */
public class NamedSqlSegment implements SqlSegment {
    private final String              exprString;
    private final Map<String, String> config;

    /**
     * 构造函数
     * @param exprString OGN 表达式字符串
     */
    public NamedSqlSegment(String exprString) {
        this(exprString, Collections.emptyMap());
    }

    /**
     * 构造函数
     * @param exprString OGN 表达式字符串
     * @param config 配置参数映射
     */
    public NamedSqlSegment(String exprString, Map<String, String> config) {
        this.exprString = exprString;
        this.config = config;
    }

    /** 获取表达式字符串 */
    public String getExpr() {
        return this.exprString;
    }

    /** 获取配置参数映射 */
    public Map<String, String> getConfig() {
        return this.config;
    }

    /** 构建 SQL 查询 */
    @Override
    public void buildQuery(SqlArgSource data, QueryContext context, SqlBuilder sqlBuilder) throws SQLException {
        ArgRule.INSTANCE.executeRule(data, context, sqlBuilder, this.exprString, this.config);
    }

    /** 克隆当前对象，返回新的 {@link NamedSqlSegment} 实例 */
    @Override
    public NamedSqlSegment clone() {
        return new NamedSqlSegment(this.exprString, this.config);
    }

    @Override
    public String toString() {
        return "Named [" + this.exprString + "]";
    }
}
