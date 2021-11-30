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
import net.hasor.db.dal.dynamic.SqlArg;
import net.hasor.db.dal.dynamic.SqlMode;
import net.hasor.db.dal.dynamic.ognl.OgnlUtils;
import net.hasor.db.dialect.SqlBuilder;
import net.hasor.db.types.TypeHandler;
import net.hasor.db.types.TypeHandlerRegistry;

import java.sql.Types;
import java.util.Map;

/**
 * 进行 OGNL 求值，并把执行结果加入到 SQL 参数中。
 * @version : 2021-12-04
 * @author 赵永春 (zyc@hasor.net)
 */
public class OgnlRule implements SqlBuildRule {
    public static final  SqlBuildRule   INSTANCE          = new OgnlRule();
    private static final TypeHandler<?> stringTypeHandler = TypeHandlerRegistry.DEFAULT.getTypeHandler(String.class);
 
    @Override
    public void executeRule(Map<String, Object> data, DynamicContext context, SqlBuilder sqlBuilder, String activeExpr, String ruleValue) {
        Object evalOgnl = OgnlUtils.evalOgnl(ruleValue, data);
        SqlArg sqlArg = null;

        if (evalOgnl == null) {
            sqlArg = new SqlArg(ruleValue, null, SqlMode.In, Types.NULL, String.class, stringTypeHandler);
        } else {
            Class<?> argType = evalOgnl.getClass();
            int sqlType = TypeHandlerRegistry.toSqlType(argType);
            TypeHandler<?> typeHandler = context.getTypeRegistry().getTypeHandler(argType);
            sqlArg = new SqlArg(ruleValue, null, SqlMode.In, sqlType, argType, typeHandler);
        }

        sqlBuilder.appendSql("?", sqlArg);

    }

    @Override
    public String toString() {
        return "ognl [" + this.hashCode() + "]";
    }
}
