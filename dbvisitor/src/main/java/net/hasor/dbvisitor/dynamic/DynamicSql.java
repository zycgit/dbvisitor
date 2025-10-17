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
package net.hasor.dbvisitor.dynamic;
import java.sql.SQLException;
import java.util.Map;
import net.hasor.dbvisitor.dynamic.args.MapSqlArgSource;

/**
 * 本处理器，支持处理 @{...}、#{...}、${...} 三种写法。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-03-28
 */
public interface DynamicSql {
    /**
     * 检查是否包含 SQL 注入占位符
     * @return 如果包含替换占位符返回 true，否则返回 false
     */
    boolean isHaveInjection();

    /**
     * 构建动态 SQL 查询
     * @param data 参数数据源
     * @param context 查询上下文
     * @param sqlBuilder SQL构建器
     */
    void buildQuery(SqlArgSource data, QueryContext context, SqlBuilder sqlBuilder) throws SQLException;

    /**
     * 构建动态 SQL 查询（简化版）
     * @param data 参数数据源
     * @param context 查询上下文
     * @return 构建完成的SQL构建器
     */
    default SqlBuilder buildQuery(SqlArgSource data, QueryContext context) throws SQLException {
        SqlBuilder fxBuilder = new SqlBuilder();
        this.buildQuery(data, context, fxBuilder);
        return fxBuilder;
    }

    /**
     * 构建动态 SQL 查询（Map参数版）
     * @param data Map类型参数
     * @param context 查询上下文
     * @return 构建完成的SQL构建器
     */
    default SqlBuilder buildQuery(Map<String, Object> data, QueryContext context) throws SQLException {
        SqlBuilder fxBuilder = new SqlBuilder();
        this.buildQuery(new MapSqlArgSource(data), context, fxBuilder);
        return fxBuilder;
    }
}
