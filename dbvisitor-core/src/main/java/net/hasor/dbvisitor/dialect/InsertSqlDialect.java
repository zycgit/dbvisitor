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

import java.util.List;

/**
 * SQL 插入数据方言。
 * @version : 2020-10-31
 * @author 赵永春 (zyc@hasor.net)
 */
public interface InsertSqlDialect extends SqlDialect {
    /** 是否支持 insert into */
    boolean supportInsertInto(List<String> primaryKey, List<String> columns);

    String insertWithInto(boolean useQualifier, String catalog, String schema, String table, List<String> primaryKey, List<String> columns);

    /** 是否支持 insert ignore */
    boolean supportInsertIgnore(List<String> primaryKey, List<String> columns);

    String insertWithIgnore(boolean useQualifier, String catalog, String schema, String table, List<String> primaryKey, List<String> columns);

    /** 是否支持 insert replace */
    boolean supportUpsert(List<String> primaryKey, List<String> columns);

    String insertWithUpsert(boolean useQualifier, String catalog, String schema, String table, List<String> primaryKey, List<String> columns);
}
