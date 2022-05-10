/*
 * Copyright 2002-2010 the original author or authors.
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
package net.hasor.dbvisitor.dialect;
import java.util.Set;

/**
 * SQL 方言
 * @version : 2020-10-31
 * @author 赵永春 (zyc@hasor.net)
 */
public interface SqlDialect {
    /** Cannot be used as a key for column names. when column name is key words, Generate SQL using Qualifier warp it. */
    Set<String> keywords();

    String leftQualifier();

    String rightQualifier();

    /** 生成 form 后面的表名 */
    String tableName(boolean useQualifier, String schema, String table);

    /** 生成 where 中用到的条件名（包括 group by、order by） */
    String columnName(boolean useQualifier, String schema, String table, String column);
}
