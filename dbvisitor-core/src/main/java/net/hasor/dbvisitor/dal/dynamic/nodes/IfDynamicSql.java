/*
 * Copyright 2008-2009 the original author or authors.
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
package net.hasor.dbvisitor.dal.dynamic.nodes;
import net.hasor.dbvisitor.dal.dynamic.DynamicContext;
import net.hasor.dbvisitor.dal.dynamic.ognl.OgnlUtils;
import net.hasor.dbvisitor.dialect.SqlBuilder;

import java.sql.SQLException;
import java.util.Map;

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
    public void buildQuery(Map<String, Object> data, DynamicContext context, SqlBuilder sqlBuilder) throws SQLException {
        if (test(data)) {
            this.buildBody(data, context, sqlBuilder);
        }
    }

    public void buildBody(Map<String, Object> data, DynamicContext context, SqlBuilder sqlBuilder) throws SQLException {
        super.buildQuery(data, context, sqlBuilder);
    }

    protected boolean test(Map<String, Object> data) {
        Object testExprResult = OgnlUtils.evalOgnl(this.testExpr, data);
        return Boolean.TRUE.equals(testExprResult);
    }
}