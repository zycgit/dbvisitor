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
package net.hasor.dbvisitor.dynamic.logic;
import net.hasor.dbvisitor.dynamic.RegistryManager;
import net.hasor.dbvisitor.dynamic.SqlArgSource;
import net.hasor.dbvisitor.dynamic.SqlBuilder;
import net.hasor.dbvisitor.internal.OgnlUtils;

import java.sql.SQLException;

/**
 * <if> 标签
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-05-24
 */
public class IfDynamicSql extends ArrayDynamicSql {
    private final String testExpr;   // 判断表达式

    public IfDynamicSql(String testExpr) {
        this.testExpr = testExpr;
    }

    @Override
    public void buildQuery(SqlArgSource data, RegistryManager context, SqlBuilder sqlBuilder) throws SQLException {
        if (test(data)) {
            this.buildBody(data, context, sqlBuilder);
        }
    }

    public void buildBody(SqlArgSource data, RegistryManager context, SqlBuilder sqlBuilder) throws SQLException {
        super.buildQuery(data, context, sqlBuilder);
    }

    protected boolean test(SqlArgSource data) {
        Object testExprResult = OgnlUtils.evalOgnl(this.testExpr, data);
        return Boolean.TRUE.equals(testExprResult);
    }
}