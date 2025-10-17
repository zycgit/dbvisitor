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
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.sql.Types;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.codec.MD5;
import net.hasor.dbvisitor.dynamic.DynamicParsed;
import net.hasor.dbvisitor.dynamic.QueryContext;
import net.hasor.dbvisitor.dynamic.SqlArgSource;
import net.hasor.dbvisitor.dynamic.SqlBuilder;
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

/**
 * MD5 编码规则实现类，用于对 SQL 参数进行 MD5 编码然后加入到 SQL 参数中
 * 功能特点：
 * 1. 实现SqlRule接口，提供参数MD5编码功能
 * 2. 自动将参数值转换为MD5哈希值
 * 3. 使用 VARCHAR 类型和字符串类型处理器
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-10-31
 */
public class MD5Rule implements SqlRule {
    private static final TypeHandler<?> typeHandler = TypeHandlerRegistry.DEFAULT.getTypeHandler(String.class);
    public static final  SqlRule        INSTANCE    = new MD5Rule();

    /**
     * 测试条件是否满足（总是返回true）
     * @param data 参数源
     * @param context 查询上下文
     * @param activeExpr 活动表达式
     * @return 总是返回true
     */
    @Override
    public boolean test(SqlArgSource data, QueryContext context, String activeExpr) {
        return true;
    }

    /** 执行宏规则 */
    @Override
    public void executeRule(SqlArgSource data, QueryContext context, SqlBuilder sqlBuilder, String activeExpr, String ruleValue) throws SQLException {
        String expr = "";
        if (activeExpr != null) {
            expr += activeExpr;
            expr += ",";
        }
        expr += (StringUtils.isBlank(ruleValue) ? "" : ruleValue);

        SqlBuilder builder = DynamicParsed.getParsedSql(expr).buildQuery(data, context);
        Object[] args = builder.getArgs();
        if (args.length != 1) {
            throw new SQLException("role MD5 args error, require 1, but " + args.length);
        }

        try {
            String argValue = args[0] == null ? "" : args[0].toString();
            sqlBuilder.appendSql("?", new SqlArg(MD5.getMD5(argValue), Types.VARCHAR, typeHandler));
        } catch (NoSuchAlgorithmException e) {
            throw new SQLException(e);
        }
    }

    @Override
    public String toString() {
        return "md5 [" + this.hashCode() + "]";
    }
}
