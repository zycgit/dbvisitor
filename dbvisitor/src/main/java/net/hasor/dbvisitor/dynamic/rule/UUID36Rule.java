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
import net.hasor.dbvisitor.dynamic.SqlMode;
import net.hasor.dbvisitor.types.SqlArg;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import java.sql.Types;
import java.util.UUID;

/**
 * UUID36 规则实现类，产生一个 36 字符长度的 `UUID`，并加入到 SQL 参数中
 * 功能特点：
 * 1. 实现SqlRule接口，提供UUID生成功能
 * 2. 生成标准36位带连字符的UUID字符串
 * 3. 使用VARCHAR类型和字符串类型处理器
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-10-31
 */
public class UUID36Rule implements SqlRule {
    private static final TypeHandler<?> typeHandler = TypeHandlerRegistry.DEFAULT.getTypeHandler(String.class);
    public static final  UUID36Rule     INSTANCE    = new UUID36Rule();

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

    /** 执行 UUID36 规则 */
    @Override
    public void executeRule(SqlArgSource data, QueryContext context, SqlBuilder sqlBuilder, String activeExpr, String ruleValue) {
        String uuidValue = UUID.randomUUID().toString();
        SqlArg sqlArg = new SqlArg(ruleValue, uuidValue, SqlMode.In, Types.VARCHAR, String.class, typeHandler);
        sqlBuilder.appendSql("?", sqlArg);
    }

    @Override
    public String toString() {
        return "uuid36 [" + this.hashCode() + "]";
    }
}
