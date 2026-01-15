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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dialect.BoundSql;
import net.hasor.dbvisitor.dialect.SqlCommandBuilder;
import net.hasor.dbvisitor.dialect.features.PageSqlDialect;
import net.hasor.dbvisitor.dialect.features.SeqSqlDialect;

/**
 * H2 的 SqlDialect 实现
 * @author 赵永春 (zyc@hasor.net)
 * @version 2020-10-31
 */
public class H2Dialect extends AbstractSqlDialect implements PageSqlDialect, SeqSqlDialect {
    @Override
    protected String keyWordsResource() {
        return "/META-INF/db-keywords/h2.keywords";
    }

    @Override
    protected String defaultQualifier() {
        return "\"";
    }

    @Override
    public SqlCommandBuilder newBuilder() {
        return new H2Dialect();
    }

    // --- PageSqlDialect impl ---

    @Override
    public BoundSql pageSql(BoundSql boundSql, long start, long limit) {
        StringBuilder sb = new StringBuilder(boundSql.getSqlString());
        List<Object> paramArrays = new ArrayList<>(Arrays.asList(boundSql.getArgs()));

        if (limit > 0) {
            sb.append(" LIMIT ?");
            paramArrays.add(limit);
        }
        if (start > 0) {
            sb.append(" OFFSET ?");
            paramArrays.add(start);
        }

        return new BoundSql.BoundSqlObj(sb.toString(), paramArrays.toArray());
    }

    // --- SeqSqlDialect impl ---

    @Override
    public String selectSeq(boolean useQualifier, String catalog, String schema, String seqName) {
        StringBuilder sb = new StringBuilder("values next value for ");
        if (StringUtils.isNotBlank(schema)) {
            sb.append(fmtName(useQualifier, schema)).append(".");
        }
        sb.append(fmtName(useQualifier, seqName));
        return sb.toString();
    }
}