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
package net.hasor.dbvisitor.dal.dynamic.rule;
import net.hasor.dbvisitor.dal.dynamic.DynamicContext;
import net.hasor.dbvisitor.dialect.SqlBuilder;

import java.util.Map;

/**
 * 动态参数规则，普通文本
 * @version : 2021-06-05
 * @author 赵永春 (zyc@hasor.net)
 */
public class TextRule implements SqlBuildRule {
    public static final SqlBuildRule INSTANCE = new TextRule();

    @Override
    public void executeRule(Map<String, Object> data, DynamicContext context, SqlBuilder sqlBuilder, String activeExpr, String ruleValue) {
        sqlBuilder.appendSql(ruleValue);
    }

    @Override
    public String toString() {
        return "text [" + this.hashCode() + "]";
    }
}
