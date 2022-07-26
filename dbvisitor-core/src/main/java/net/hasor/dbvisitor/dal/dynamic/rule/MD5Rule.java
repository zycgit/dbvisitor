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
import net.hasor.cobble.codec.MD5;
import net.hasor.dbvisitor.dal.dynamic.DynamicContext;
import net.hasor.dbvisitor.dal.dynamic.SqlArg;
import net.hasor.dbvisitor.dal.dynamic.SqlMode;
import net.hasor.dbvisitor.dal.dynamic.ognl.OgnlUtils;
import net.hasor.dbvisitor.dialect.SqlBuilder;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

/**
 * 进行 OGNL 求值，值结果用 MD5 进行编码然后加入到 SQL 参数中
 * @version : 2021-10-31
 * @author 赵永春 (zyc@hasor.net)
 */
public class MD5Rule implements SqlBuildRule {
    public static final  MD5Rule        INSTANCE    = new MD5Rule();
    private static final TypeHandler<?> typeHandler = TypeHandlerRegistry.DEFAULT.getTypeHandler(String.class);

    @Override
    public void executeRule(Map<String, Object> data, DynamicContext context, SqlBuilder sqlBuilder, String activeExpr, String ruleValue) throws SQLException {
        Object argValue = OgnlUtils.evalOgnl(ruleValue, data);

        if (argValue == null) {
            argValue = "";
        }

        try {
            argValue = MD5.getMD5(argValue.toString());
        } catch (NoSuchAlgorithmException e) {
            throw new SQLException(e);
        }

        SqlArg sqlArg = new SqlArg(ruleValue, argValue, SqlMode.In, Types.VARCHAR, String.class, typeHandler);
        sqlBuilder.appendSql("?", sqlArg);
    }

    @Override
    public String toString() {
        return "md5 [" + this.hashCode() + "]";
    }
}
