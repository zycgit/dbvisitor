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
import net.hasor.dbvisitor.dynamic.RegistryManager;
import net.hasor.dbvisitor.dynamic.SqlArgSource;
import net.hasor.dbvisitor.dynamic.SqlBuilder;

import java.sql.SQLException;

/**
 * Result 规则，是一个特殊规则。在正常查询中不会产生任何效果，但通过 call 方法执行数据库 存储过程/函数调用 时可以用于决定结果集如何获取。
 * @author 赵永春 (zyc@hasor.net)
 * @version : 2021-06-05
 */
public final class ResultRule implements SqlBuildRule {
    public static final String       FUNC_RESULT         = "result";
    public static final String       FUNC_DEFAULT_RESULT = "defaultResult";
    public static final SqlBuildRule INSTANCE            = new ResultRule();

    private ResultRule() {
    }

    @Override
    public String toString() {
        return "result [" + this.hashCode() + "]";
    }

    @Override
    public boolean test(SqlArgSource data, RegistryManager context, String activeExpr) {
        return false;
    }

    @Override
    public void executeRule(SqlArgSource data, RegistryManager context, SqlBuilder sqlBuilder, String activeExpr, String ruleValue) throws SQLException {

    }
}
