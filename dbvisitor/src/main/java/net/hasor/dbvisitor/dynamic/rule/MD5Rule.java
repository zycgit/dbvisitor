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
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.codec.MD5;
import net.hasor.dbvisitor.dynamic.DynamicContext;
import net.hasor.dbvisitor.dynamic.DynamicParsed;
import net.hasor.dbvisitor.dynamic.SqlArgSource;
import net.hasor.dbvisitor.dynamic.SqlBuilder;
import net.hasor.dbvisitor.types.MappedArg;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.sql.Types;

import static net.hasor.dbvisitor.internal.OgnlUtils.evalOgnl;

/**
 * 进行 OGNL 求值，值结果用 MD5 进行编码然后加入到 SQL 参数中
 * @version : 2021-10-31
 * @author 赵永春 (zyc@hasor.net)
 */
public class MD5Rule implements SqlBuildRule {
    private static final TypeHandler<?> typeHandler = TypeHandlerRegistry.DEFAULT.getTypeHandler(String.class);
    public static final  SqlBuildRule   INSTANCE    = new MD5Rule(false);
    private final        boolean        usingIf;

    public MD5Rule(boolean usingIf) {
        this.usingIf = usingIf;
    }

    @Override
    public boolean test(SqlArgSource data, DynamicContext context, String activeExpr) {
        if (this.usingIf) {
            return StringUtils.isBlank(activeExpr) || Boolean.TRUE.equals(evalOgnl(activeExpr, data));
        } else {
            return true;
        }
    }

    @Override
    public void executeRule(SqlArgSource data, DynamicContext context, SqlBuilder sqlBuilder, String activeExpr, String ruleValue) throws SQLException {
        String expr;
        if (this.usingIf) {
            expr = (StringUtils.isBlank(ruleValue) ? "" : ruleValue);
        } else {
            expr = "";
            if (activeExpr != null) {
                expr += activeExpr;
                expr += ",";
            }
            expr += (StringUtils.isBlank(ruleValue) ? "" : ruleValue);
        }

        SqlBuilder builder = DynamicParsed.getParsedSql(expr).buildQuery(data, context);
        Object[] args = builder.getArgs();
        if (args.length != 1) {
            String inName = usingIf ? "IFMD5" : "MD5";
            throw new SQLException("role " + inName + " args error, require 1, but " + args.length);
        }

        try {
            String argValue = args[0] == null ? "" : args[0].toString();
            sqlBuilder.appendSql("?", new MappedArg(MD5.getMD5(argValue), Types.VARCHAR, typeHandler));
        } catch (NoSuchAlgorithmException e) {
            throw new SQLException(e);
        }
    }

    @Override
    public String toString() {
        return (this.usingIf ? "ifmd5 [" : "md5 [") + this.hashCode() + "]";
    }
}
