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
package net.hasor.dbvisitor.dynamic.logic;
import net.hasor.cobble.ArrayUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.dbvisitor.dynamic.QueryContext;
import net.hasor.dbvisitor.dynamic.SqlArgSource;
import net.hasor.dbvisitor.dynamic.SqlBuilder;

import java.sql.SQLException;
import java.util.Arrays;

/**
 * 对应XML中 <trim>
 * @author zhangxu
 * @author 赵永春 (zyc@hasor.net)
 * @version 2021-05-24
 */
public class TrimDynamicSql extends ArrayDynamicSql {
    /** 前缀  prefix */
    private final String   prefix;
    /** 后缀  suffix */
    private final String   suffix;
    /** 前缀 prefixOverrides */
    private final String[] prefixOverrides;
    /** 后缀 suffixOverrides */
    private final String[] suffixOverrides;

    public TrimDynamicSql(String prefix, String suffix, String prefixOverrides, String suffixOverrides) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.prefixOverrides = StringUtils.isBlank(prefixOverrides) ? ArrayUtils.EMPTY_STRING_ARRAY : //
                Arrays.stream(prefixOverrides.split("\\|")).map(String::trim).toArray(String[]::new);
        this.suffixOverrides = StringUtils.isBlank(suffixOverrides) ? ArrayUtils.EMPTY_STRING_ARRAY : //
                Arrays.stream(suffixOverrides.split("\\|")).map(String::trim).toArray(String[]::new);
    }

    private static boolean startsWith(String test, String prefix) {
        return StringUtils.startsWithIgnoreCase(test.trim(), prefix);
    }

    private static boolean endsWith(String test, String suffix) {
        return StringUtils.endsWithIgnoreCase(test.trim(), suffix);
    }

    @Override
    public void buildQuery(SqlArgSource data, QueryContext context, SqlBuilder sqlBuilder) throws SQLException {
        SqlBuilder tempDalSqlBuilder = new SqlBuilder();
        super.buildQuery(data, context, tempDalSqlBuilder);
        //
        String childrenSql = tempDalSqlBuilder.getSqlString().trim();
        if (StringUtils.isNotBlank(childrenSql)) {
            if (!sqlBuilder.lastSpaceCharacter()) {
                sqlBuilder.appendSql(" ");
            }
            sqlBuilder.appendSql(StringUtils.defaultString(this.prefix) + " "); // 开始拼接SQL
            //
            // 去掉prefixOverrides
            for (String override : this.prefixOverrides) {
                override = override.trim();
                if (StringUtils.isBlank(override)) {
                    continue;
                }
                if (startsWith(childrenSql, override)) {
                    childrenSql = childrenSql.substring(childrenSql.indexOf(override) + override.length());
                    break;
                }
            }
            // 去掉 suffixOverrides
            for (String override : this.suffixOverrides) {
                if (endsWith(childrenSql, override)) {
                    childrenSql = childrenSql.substring(0, childrenSql.lastIndexOf(override));
                    break;
                }
            }
            sqlBuilder.appendSql(childrenSql);
            sqlBuilder.appendSql(" " + StringUtils.defaultString(this.suffix)); // 拼接结束SQL
        }

        sqlBuilder.appendArgs(tempDalSqlBuilder);
    }
}