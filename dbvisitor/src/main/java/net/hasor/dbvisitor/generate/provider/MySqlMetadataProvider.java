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
import net.hasor.cobble.ref.LinkedCaseInsensitiveMap;
import net.hasor.dbvisitor.generate.CaseSensitivityType;
import net.hasor.dbvisitor.generate.ColumnInfo;
import net.hasor.dbvisitor.generate.IndexInfo;
import net.hasor.dbvisitor.generate.TableInfo;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 针对 MySQL 的表结构生成器
 * <li>https://dev.mysql.com/doc/refman/5.7/en/data-types.html</li>
 * <li>https://dev.mysql.com/doc/refman/8.0/en/data-types.html</li>
 * @version : 2023-03-04
 * @author 赵永春 (zyc@hasor.net)
 */
public class MySqlMetadataProvider extends AbstractMetadataProvider {
    public MySqlMetadataProvider(Connection connection) {
        super(connection);
    }

    public MySqlMetadataProvider(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public CaseSensitivityType getPlain() throws SQLException {
        if (this.plainCaseSensitivityType == null) {
            this.plainCaseSensitivityType = this.jdbcTemplate.query("show global variables like 'lower_case_table_names'", rs -> {
                // https://dev.mysql.com/doc/refman/5.7/en/identifier-case-sensitivity.html
                Map<String, Object> varMap = null;
                while (rs.next()) {
                    varMap = new LinkedCaseInsensitiveMap<>();
                    varMap.put(rs.getString(1), rs.getString(2));
                }

                if (varMap == null || !varMap.containsKey("lower_case_table_names")) {
                    return MySqlMetadataProvider.super.getPlain();
                }

                Integer mode = safeToInteger(varMap.get("lower_case_table_names"));
                if (mode == null) {
                    return MySqlMetadataProvider.super.getPlain();
                }
                switch (mode) {
                    case 0:
                        return CaseSensitivityType.Exact;
                    case 1:
                        return CaseSensitivityType.Lower;
                    case 2:
                        return CaseSensitivityType.Fuzzy;
                    default:
                        return MySqlMetadataProvider.super.getPlain();
                }
            });
        }
        return this.plainCaseSensitivityType;
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