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
package net.hasor.dbvisitor.dynamic.rule;
import net.hasor.dbvisitor.dynamic.QueryContext;
import net.hasor.dbvisitor.dynamic.SqlArgSource;
import net.hasor.dbvisitor.dynamic.SqlBuilder;

import java.sql.SQLException;

/**
 * 动态SQL规则接口
 * 定义动态SQL处理规则，用于在SQL构建过程中执行特定逻辑
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-06-05
 */
public interface SqlRule {
    /**
     * 测试规则是否应该执行
     * @param data 参数数据源
     * @param context 查询上下文
     * @param activeExpr 规则表达式
     * @return 如果规则应该执行返回true，否则返回false
     */
    boolean test(SqlArgSource data, QueryContext context, String activeExpr);

    /**
     * 执行规则逻辑
     * @param data 参数数据源
     * @param context 查询上下文
     * @param sqlBuilder SQL构建器
     * @param activeExpr 规则表达式
     * @param ruleValue 规则值
     */
    void executeRule(SqlArgSource data, QueryContext context, SqlBuilder sqlBuilder, String activeExpr, String ruleValue) throws SQLException;
}
