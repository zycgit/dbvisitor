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
import java.sql.SQLException;
import net.hasor.dbvisitor.dynamic.DynamicSql;
import net.hasor.dbvisitor.dynamic.QueryContext;
import net.hasor.dbvisitor.dynamic.SqlArgSource;
import net.hasor.dbvisitor.dynamic.SqlBuilder;

/**
 * <choose>、<when>、<otherwise> 标签实现类
 * 功能特点：
 * 1. 继承自ArrayDynamicSql，支持多个条件分支
 * 2. 实现类似switch-case的条件选择逻辑
 * 3. 支持默认分支(otherwise)处理
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-05-24
 */
public class ChooseDynamicSql extends ArrayDynamicSql {
    private DynamicSql defaultDynamicSql;

    /**
     * 添加 when 条件分支
     * @param test 条件表达式
     * @param nodeBlock 条件成立时执行的SQL节点
     */
    public void addThen(String test, DynamicSql nodeBlock) {
        IfDynamicSql whenSqlNode = new IfDynamicSql(test);
        whenSqlNode.addChildNode(nodeBlock);

        this.addChildNode(whenSqlNode);
    }

    /**
     * 重写父类方法，限制只能添加 {@link IfDynamicSql} 节点
     * @param node 要添加的节点
     */
    @Override
    public void addChildNode(DynamicSql node) {
        if (node instanceof IfDynamicSql) {
            this.subNodes.add(node);
        } else {
            throw new IllegalArgumentException("only supports IfDynamicSql nodes.");
        }
    }

    /** 追加子节点 */
    public void setDefaultNode(DynamicSql block) {
        this.defaultDynamicSql = block;
    }

    /** 构建SQL查询 */
    @Override
    public void buildQuery(SqlArgSource data, QueryContext context, SqlBuilder sqlBuilder) throws SQLException {
        boolean useDefault = true;
        try {
            for (DynamicSql dynamicSql : this.subNodes) {
                if (dynamicSql instanceof IfDynamicSql) {

                    boolean test = ((IfDynamicSql) dynamicSql).test(data);
                    if (test) {
                        ((IfDynamicSql) dynamicSql).buildBody(data, context, sqlBuilder);
                        useDefault = false;
                        break;
                    }
                }
            }
        } finally {
            if (useDefault) {
                if (!sqlBuilder.lastSpaceCharacter()) {
                    sqlBuilder.appendSql(" ");
                }
                this.defaultDynamicSql.buildQuery(data, context, sqlBuilder);
            }
        }
    }
}