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
package net.hasor.dbvisitor.dal.dynamic.nodes;
import net.hasor.dbvisitor.dal.dynamic.DynamicContext;
import net.hasor.dbvisitor.dal.dynamic.DynamicSql;
import net.hasor.dbvisitor.dialect.SqlBuilder;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 多个 SQL 节点组合成一个 SqlNode
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-05-24
 */
public class ArrayDynamicSql implements DynamicSql {
    /** 子节点 */
    protected List<DynamicSql> subNodes = new ArrayList<>();

    /** 获取节点 */
    public List<DynamicSql> getSubNodes() {
        return this.subNodes;
    }

    /** 追加子节点 */
    public void addChildNode(DynamicSql node) {
        this.subNodes.add(node);
    }

    /** 最后一个节点是文本 */
    public boolean lastIsText() {
        return this.subNodes.get(this.subNodes.size() - 1) instanceof TextDynamicSql;
    }

    /** 追加文本 */
    public void appendText(String text) {
        if (!this.subNodes.isEmpty()) {
            DynamicSql dynamicSql = this.subNodes.get(this.subNodes.size() - 1);
            if (dynamicSql instanceof TextDynamicSql) {
                ((TextDynamicSql) dynamicSql).appendText(text);
                return;
            }
        }
        this.addChildNode(new TextDynamicSql(text));
    }

    @Override
    public boolean isHavePlaceholder() {
        for (DynamicSql dynamicSql : this.subNodes) {
            if (dynamicSql.isHavePlaceholder()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void buildQuery(Map<String, Object> data, DynamicContext context, SqlBuilder sqlBuilder) throws SQLException {
        for (int i = 0; i < this.subNodes.size(); i++) {
            DynamicSql dynamicSql = this.subNodes.get(i);
            if (visitItem(i, dynamicSql, context, sqlBuilder)) {
                dynamicSql.buildQuery(data, context, sqlBuilder);
            }
        }
    }

    protected boolean visitItem(int i, DynamicSql dynamicSql, DynamicContext context, SqlBuilder sqlBuilder) {
        return true;
    }
}