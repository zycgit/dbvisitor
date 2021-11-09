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
package net.hasor.db.dal.dynamic.nodes;
import net.hasor.db.dal.dynamic.DynamicContext;
import net.hasor.db.dal.dynamic.DynamicSql;
import net.hasor.db.dialect.SqlBuilder;

import java.sql.SQLException;
import java.util.Map;

/**
 * <choose>、<when>、<otherwise> 标签
 * @author 赵永春 (zyc@byshell.org)
 * @version : 2021-05-24
 */
public class ChooseDynamicSql extends ArrayDynamicSql {
    private       DynamicSql           defaultDynamicSql;
    private final ThreadLocal<Boolean> useDefault = new ThreadLocal<>();

    public void addWhen(String test, ArrayDynamicSql nodeBlock) {
        WhenDynamicSql whenSqlNode = new WhenDynamicSql(test);
        whenSqlNode.addChildNode(nodeBlock);
        super.addChildNode(whenSqlNode);
    }

    /** 追加子节点 */
    public void setDefaultNode(DynamicSql block) {
        this.defaultDynamicSql = block;
    }

    @Override
    public void buildQuery(Map<String, Object> data, DynamicContext context, SqlBuilder sqlBuilder) throws SQLException {
        try {
            this.useDefault.set(true);
            super.buildQuery(data, context, sqlBuilder);
        } finally {
            if (this.useDefault.get()) {
                if (!sqlBuilder.lastSpaceCharacter()) {
                    sqlBuilder.appendSql(" ");
                }
                this.defaultDynamicSql.buildQuery(data, context, sqlBuilder);
            }
            this.useDefault.remove();
        }
    }

    private class WhenDynamicSql extends IfDynamicSql {
        public WhenDynamicSql(String testExpr) {
            super(testExpr);
        }

        @Override
        public void buildQuery(Map<String, Object> data, DynamicContext context, SqlBuilder sqlBuilder) throws SQLException {
            if (test(data)) {
                if (!sqlBuilder.lastSpaceCharacter()) {
                    sqlBuilder.appendSql(" ");
                }
                super.buildQuery(data, context, sqlBuilder);
            }
        }

        @Override
        protected boolean test(Map<String, Object> data) {
            boolean testResult = super.test(data);
            if (testResult && useDefault.get()) {
                useDefault.set(false);
            }
            return testResult;
        }
    }
}