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

import net.hasor.dbvisitor.dynamic.args.MapSqlArgSource;

import java.sql.SQLException;
import java.util.Map;

/**
 * 本处理器，兼容 @{...}、#{...}、${...} 三种写法。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-03-28
 */
public interface DynamicSql {
    /** 是否包含替换占位符，如果包含替换占位符那么不能使用批量模式 */
    boolean isHaveInjection();

    void buildQuery(SqlArgSource data, QueryContext context, SqlBuilder sqlBuilder) throws SQLException;

    default SqlBuilder buildQuery(SqlArgSource data, QueryContext context) throws SQLException {
        SqlBuilder fxBuilder = new SqlBuilder();
        this.buildQuery(data, context, fxBuilder);
        return fxBuilder;
    }

    default SqlBuilder buildQuery(Map<String, Object> data, QueryContext context) throws SQLException {
        SqlBuilder fxBuilder = new SqlBuilder();
        this.buildQuery(new MapSqlArgSource(data), context, fxBuilder);
        return fxBuilder;
    }
}
