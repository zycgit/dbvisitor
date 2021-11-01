/*
 * Copyright 2002-2005 the original author or authors.
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
package net.hasor.db.dal.dynamic.rule;
import net.hasor.db.dal.dynamic.DynamicContext;
import net.hasor.db.dal.dynamic.ognl.OgnlUtils;
import net.hasor.db.dialect.SqlBuilder;

import java.util.Map;

import static net.hasor.db.dal.dynamic.ognl.OgnlUtils.evalOgnl;

/**
 * 动态参数规则，非空
 * @version : 2021-06-05
 * @author 赵永春 (zyc@hasor.net)
 */
public class NotnullRule implements SqlBuildRule {
    public static final SqlBuildRule INSTANCE = new NotnullRule();

    @Override
    public boolean test(Map<String, Object> data, DynamicContext context, String activateExpr) {
        Object evalObject = evalOgnl(activateExpr, data);
        return evalObject != null;
    }

    @Override
    public void executeRule(Map<String, Object> data, DynamicContext context, SqlBuilder sqlBuilder, String ruleValue) {
        Object evalOgnl = OgnlUtils.evalOgnl(ruleValue, data);
        sqlBuilder.appendSql(evalOgnl.toString());
    }
}
