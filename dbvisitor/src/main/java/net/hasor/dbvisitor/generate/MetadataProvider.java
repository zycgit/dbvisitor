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
package net.hasor.dbvisitor.generate;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * 源信息服务
 * @version : 2020-10-31
 * @author 赵永春 (zyc@hasor.net)
 */
public interface MetadataProvider {
    /**
     * default sensitive type if meta not be quot
     */
    CaseSensitivityType getPlain() throws SQLException;

    /**
     * fetch sensitive type if meta be quot
     */
    CaseSensitivityType getDelimited() throws SQLException;

    Map<String, TableInfo> fetchTables(String catalog, String schema, List<String> tables);

    Map<String, List<ColumnInfo>> fetchColumns(String catalog, String schema, List<String> tables);

    Map<String, List<IndexInfo>> fetchIndexes(String catalog, String schema, List<String> tables);
}