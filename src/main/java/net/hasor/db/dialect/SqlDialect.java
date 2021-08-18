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
package net.hasor.db.dialect;
import net.hasor.db.metadata.ColumnDef;
import net.hasor.db.metadata.TableDef;

import java.util.Set;

/**
 * SQL 方言
 * @version : 2020-10-31
 * @author 赵永春 (zyc@hasor.net)
 */
public interface SqlDialect {
    /** Cannot be used as a key for column names. when column name is key words, Generate SQL using Qualifier warp it. */
    public Set<String> keywords();

    public String leftQualifier();

    public String rightQualifier();

    /** 生成 form 后面的表名 */
    public String tableName(boolean useQualifier, TableDef tableDef);

    /** 生成 where 中用到的条件名（包括 group by、order by） */
    public String columnName(boolean useQualifier, TableDef tableDef, ColumnDef columnDef);
}
