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
package net.hasor.dbvisitor.dialect.provider;
import java.util.Collections;
import java.util.Set;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.ConditionSqlDialect;
import net.hasor.dbvisitor.dialect.PageSqlDialect;
import net.hasor.dbvisitor.dialect.builder.CommandBuilder;
import net.hasor.dbvisitor.dialect.builder.Es6CommandBuilder;

/**
 * ES6 方言
 * @author 赵永春 (zyc@hasor.net)
 * @version 2025-12-31
 */
public class Es6Dialect extends AbstractDialect implements PageSqlDialect, ConditionSqlDialect {
    @Override
    public Set<String> keywords() {
        return Collections.emptySet();
    }

    @Override
    public String tableName(boolean useQualifier, String catalog, String schema, String table) {
        return table;
    }

    @Override
    public String fmtName(boolean useQualifier, String name) {
        return name;
    }

    @Override
    protected String defaultQualifier() {
        return "";
    }

    @Override
    public String aliasSeparator() {
        return ":";
    }

    @Override
    public String like(SqlLike likeType, Object value, String valueTerm) {
        if (StringUtils.isNotBlank(valueTerm)) {
            return valueTerm;
        }

        String strVal = value == null ? "" : value.toString();
        switch (likeType) {
            case LEFT:
                return "*" + strVal;
            case RIGHT:
                return strVal + "*";
            default:
                return "*" + strVal + "*";
        }
    }

    @Override
    public BoundSql countSql(BoundSql boundSql) {
        return new BoundSql.BoundSqlObj("/*+overwrite_find_as_count*/" + boundSql.getSqlString(), boundSql.getArgs());
    }

    @Override
    public BoundSql pageSql(BoundSql boundSql, long start, long limit) {
        StringBuilder sqlBuilder = new StringBuilder("/*+");

        if (start <= 0) {
            sqlBuilder.append("overwrite_find_limit=" + limit);
        } else {
            sqlBuilder.append("overwrite_find_skip=" + start + ",overwrite_find_limit=" + limit);
        }

        sqlBuilder.append("*/");
        return new BoundSql.BoundSqlObj(sqlBuilder + boundSql.getSqlString(), boundSql.getArgs());
    }

    @Override
    public CommandBuilder newBuilder() {
        return new Es6CommandBuilder();
    }
}
