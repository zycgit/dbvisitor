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

import java.sql.SQLException;

/**
 * SQL片段接口，表示动态SQL中的一个可构建片段。
 * @author 赵永春 (zyc@hasor.net)
 * @version 2024-09-25
 */
public interface SqlSegment extends Cloneable {
    /**
     * 构建SQL查询片段
     * @param data 参数数据源
     * @param context 查询上下文
     * @param sqlBuilder SQL构建器
     */
    void buildQuery(SqlArgSource data, QueryContext context, SqlBuilder sqlBuilder) throws SQLException;

    /** 克隆当前SQL片段 */
    SqlSegment clone();
}
