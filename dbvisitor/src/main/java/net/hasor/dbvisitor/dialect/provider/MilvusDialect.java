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
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.SqlCommandBuilder;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.dialect.features.PageSqlDialect;

/**
 * Milvus 的 SqlDialect 实现
 * @author 赵永春 (zyc@hasor.net)
 * @version 2024-02-02
 */
public class MilvusDialect extends AbstractSqlDialect implements PageSqlDialect {
    public static final SqlDialect DEFAULT = new MilvusDialect();

    @Override
    public SqlCommandBuilder newBuilder() {
        return new MilvusDialect();
    }

    @Override
    protected String keyWordsResource() {
        return "/META-INF/db-keywords/milvus.keywords";
    }

    @Override
    public String tableName(boolean useQualifier, String catalog, String schema, String table) {
        if (StringUtils.isBlank(schema)) {
            return fmtName(useQualifier, table);
        } else {
            return fmtName(useQualifier, schema) + "." + fmtName(useQualifier, table);
        }
    }

    // --- PageSqlDialect impl ---

    @Override
    public BoundSql countSql(BoundSql boundSql) {
        return new BoundSql.BoundSqlObj("/*+ overwrite_find_as_count=true */ " + boundSql.getSqlString(), boundSql.getArgs());
    }

    @Override
    public BoundSql pageSql(BoundSql boundSql, long start, long limit) {
        StringBuilder sb = new StringBuilder(boundSql.getSqlString());
        if (limit > 0) {
            sb.append(" LIMIT ").append(limit);
        }
        if (start > 0) {
            sb.append(" OFFSET ").append(start);
        }
        return new BoundSql.BoundSqlObj(sb.toString(), boundSql.getArgs());
    }
}
