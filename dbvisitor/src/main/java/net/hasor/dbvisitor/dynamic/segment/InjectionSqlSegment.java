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
import net.hasor.dbvisitor.internal.OgnlUtils;

import java.sql.SQLException;

/**
 * 注入式 SQL 片段实现类，用于动态 SQL 中的 OGNL 表达式注入
 * 功能特点：
 * 1. 实现 {@link SqlSegment} 接口，提供 SQL 片段构建功能
 * 2. 支持 OGNL 表达式动态求值
 * 3. 将表达式结果直接注入到 SQL 中
 * 4. 实现了克隆功能
 * @author 赵永春 (zyc@hasor.net)
 * @version 2024-09-25
 */
public class InjectionSqlSegment implements SqlSegment {
    private final String exprString;

    /**
     * 构造函数
     * @param exprString OGN 表达式字符串
     */
    public InjectionSqlSegment(String exprString) {
        this.exprString = exprString;
    }

    /** 获取表达式字符串 */
    public String getExpr() {
        return this.exprString;
    }

    /** 构建 SQL 查询 */
    @Override
    public void buildQuery(SqlArgSource data, QueryContext context, SqlBuilder sqlBuilder) throws SQLException {
        sqlBuilder.appendSql(String.valueOf(OgnlUtils.evalOgnl(this.exprString, data)));
    }

    /** 克隆当前对象，返回新的 {@link InjectionSqlSegment} 实例 */
    @Override
    public InjectionSqlSegment clone() {
        return new InjectionSqlSegment(this.exprString);
    }

    @Override
    public String toString() {
        return "Injection [" + this.exprString + "]";
    }
}
