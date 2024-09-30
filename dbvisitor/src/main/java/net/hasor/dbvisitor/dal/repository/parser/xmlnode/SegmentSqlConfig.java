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
package net.hasor.dbvisitor.dal.repository.parser.xmlnode;
import net.hasor.dbvisitor.dal.repository.QueryType;
import net.hasor.dbvisitor.dynamic.DynamicContext;
import net.hasor.dbvisitor.dynamic.DynamicSql;
import net.hasor.dbvisitor.dynamic.SqlArgSource;
import net.hasor.dbvisitor.dynamic.SqlBuilder;

import java.sql.SQLException;

/**
 * Segment SqlConfig
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-06-19
 */
public class SegmentSqlConfig implements DynamicSql {
    protected final DynamicSql target;

    public SegmentSqlConfig(DynamicSql target) {
        this.target = target;
    }

    public QueryType getDynamicType() {
        return QueryType.Segment;
    }

    @Override
    public boolean isHaveInjection() {
        return this.target.isHaveInjection();
    }

    @Override
    public void buildQuery(SqlArgSource data, DynamicContext context, SqlBuilder sqlBuilder) throws SQLException {
        this.target.buildQuery(data, context, sqlBuilder);
    }
}