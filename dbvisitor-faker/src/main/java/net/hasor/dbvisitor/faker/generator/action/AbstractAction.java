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
package net.hasor.dbvisitor.faker.generator.action;

import net.hasor.cobble.CollectionUtils;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.faker.generator.Action;
import net.hasor.dbvisitor.faker.generator.DataLoader;
import net.hasor.dbvisitor.faker.generator.FakerTable;
import net.hasor.dbvisitor.faker.generator.UseFor;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 公共
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public abstract class AbstractAction implements Action {
    protected final FakerTable tableInfo;
    protected final boolean    useQualifier;
    protected final SqlDialect dialect;

    public AbstractAction(FakerTable tableInfo, SqlDialect dialect) {
        this.tableInfo = tableInfo;
        this.useQualifier = tableInfo.isUseQualifier();
        this.dialect = dialect;
    }

    protected List<Map<String, Object>> retryLoad(DataLoader dataLoader, UseFor useFor, FakerTable fakerTable, List<String> includeColumns, int batchSize) throws SQLException {
        int tryTimes = 0;
        while (true) {
            tryTimes++;
            try {
                List<Map<String, Object>> fetchDataList = dataLoader.loadSomeData(useFor, fakerTable, includeColumns, batchSize);
                if (CollectionUtils.isEmpty(fetchDataList)) {
                    return Collections.emptyList();
                } else {
                    return fetchDataList;
                }
            } catch (SQLException e) {
                if (tryTimes >= 3) {
                    throw e;
                }
            }
        }
    }
}