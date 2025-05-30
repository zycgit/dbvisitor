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
import net.hasor.dbvisitor.dynamic.DynamicSql;
import net.hasor.dbvisitor.dynamic.QueryContext;
import net.hasor.dbvisitor.dynamic.SqlArgSource;
import net.hasor.dbvisitor.dynamic.SqlBuilder;
import net.hasor.dbvisitor.dynamic.segment.PlanDynamicSql;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 多个 SQL 节点组合成一个 SqlNode
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-05-24
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
        if (node != null) {
            this.subNodes.add(node);
        }
    }

    /** 最后一个节点是文本 */
    public boolean lastIsText() {
        if (this.subNodes.isEmpty()) {
            return false;
        } else {
            return this.subNodes.get(this.subNodes.size() - 1) instanceof PlanDynamicSql;
        }
    }

    /** 最后一个节点 */
    public DynamicSql lastNode() {
        if (this.subNodes.isEmpty()) {
            return null;
        } else {
            return this.subNodes.get(this.subNodes.size() - 1);
        }
    }

    /** 检查是否包含 SQL 注入片段 */
    @Override
    public boolean isHaveInjection() {
        for (DynamicSql dynamicSql : this.subNodes) {
            if (dynamicSql.isHaveInjection()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 构建SQL查询
     * @param data 参数源
     * @param context 查询上下文
     * @param sqlBuilder SQL构建器
     * @throws SQLException 如果构建过程中出错
     */
    @Override
    public void buildQuery(SqlArgSource data, QueryContext context, SqlBuilder sqlBuilder) throws SQLException {
        for (int i = 0; i < this.subNodes.size(); i++) {
            DynamicSql dynamicSql = this.subNodes.get(i);
            if (visitItem(i, dynamicSql, context, sqlBuilder)) {
                dynamicSql.buildQuery(data, context, sqlBuilder);
            }
        }
    }

    /**
     * 子类可重写的节点访问方法
     * @param i 当前节点索引
     * @param dynamicSql 当前节点
     * @param context 查询上下文
     * @param sqlBuilder SQL构建器
     * @return 是否处理该节点（默认true）
     */
    protected boolean visitItem(int i, DynamicSql dynamicSql, QueryContext context, SqlBuilder sqlBuilder) {
        return true;
    }
}