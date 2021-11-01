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
import net.hasor.cobble.CommonCodeUtils;
import net.hasor.db.dal.dynamic.DynamicContext;
import net.hasor.db.dal.dynamic.SqlArg;
import net.hasor.db.dal.dynamic.SqlMode;
import net.hasor.db.dal.dynamic.ognl.OgnlUtils;
import net.hasor.db.dialect.SqlBuilder;
import net.hasor.db.types.TypeHandler;
import net.hasor.db.types.TypeHandlerRegistry;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Map;

/**
 * 对表达式结果进行 md5 加密。
 * @version : 2021-10-31
 * @author 赵永春 (zyc@hasor.net)
 */
public class Md5Rule implements SqlBuildRule {
    public static final  Md5Rule        INSTANCE    = new Md5Rule();
    private static final TypeHandler<?> typeHandler = TypeHandlerRegistry.DEFAULT.getTypeHandler(String.class);

    @Override
    public void executeRule(Map<String, Object> data, DynamicContext context, SqlBuilder sqlBuilder, String ruleValue) throws SQLException {
        Object argValue = OgnlUtils.evalOgnl(ruleValue, data);

        if (argValue == null) {
            argValue = "";
        }

        try {
            argValue = CommonCodeUtils.MD5.getMD5(argValue.toString());
        } catch (NoSuchAlgorithmException e) {
            throw new SQLException(e);
        }

        SqlArg sqlArg = new SqlArg(null, ruleValue, argValue, SqlMode.In, Types.VARCHAR, String.class, typeHandler);
        sqlBuilder.appendSql("?", sqlArg);
    }

    @Override
    public String toString() {
        return "MD5 [" + this.hashCode() + "]";
    }
}
