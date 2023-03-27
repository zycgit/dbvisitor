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
package net.hasor.dbvisitor.generate.provider;
import net.hasor.dbvisitor.generate.ColumnInfo;
import net.hasor.dbvisitor.generate.IndexInfo;
import net.hasor.dbvisitor.generate.TableInfo;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

/**
 * 针对 JDBC 的元信息获取
 * @version : 2023-03-04
 * @author 赵永春 (zyc@hasor.net)
 */
public class JdbcTableGenerate extends AbstractMetadataProvider {
    public JdbcTableGenerate(Connection connection) {
        super(connection);
    }

    public JdbcTableGenerate(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public Map<String, TableInfo> fetchTables(String catalog, String schema, List<String> tables) {
        return null;
    }

    @Override
    public Map<String, List<ColumnInfo>> fetchColumns(String catalog, String schema, List<String> tables) {
        return null;
    }

    @Override
    public Map<String, List<IndexInfo>> fetchIndexes(String catalog, String schema, List<String> tables) {
        return null;
    }
}