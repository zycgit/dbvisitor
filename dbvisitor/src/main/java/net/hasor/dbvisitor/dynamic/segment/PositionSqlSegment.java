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

/**
 * 位置参数 SQL 片段实现类，用于处理按位置绑定的 SQL 参数
 * 功能特点：
 * 1. 实现 {@link SqlSegment} 接口，提供 SQL 片段构建功能
 * 2. 支持按位置绑定参数
 * 3. 使用 {@link ArgRule} 处理参数绑定
 * @author 赵永春 (zyc@hasor.net)
 * @version 2024-09-25
 */
public class PositionSqlSegment implements SqlSegment {
    private final int position;

    /**
     * 构造函数
     * @param position 参数位置
     */
    public PositionSqlSegment(int position) {
        this.position = position;
    }

    /** 获取参数位置 */
    public int getPosition() {
        return this.position;
    }

    /** 构建 SQL 查询 */
    @Override
    public void buildQuery(SqlArgSource data, QueryContext context, SqlBuilder sqlBuilder) throws SQLException {
        ArgRule.INSTANCE.executeRule(data, context, sqlBuilder, "arg" + position, Collections.emptyMap());
    }

    /** 克隆当前对象，返回新的 {@link PositionSqlSegment} 实例 */
    @Override
    public PositionSqlSegment clone() {
        return new PositionSqlSegment(this.position);
    }

    @Override
    public String toString() {
        return "Args [" + this.position + "]";
    }
}
