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
package net.hasor.dbvisitor.faker.generator.loader;

import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.faker.FakerConfig;
import net.hasor.dbvisitor.faker.generator.SqlArg;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 反查数据加载器
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class PrecociousDataLoaderFactory implements DataLoaderFactory {
    @Override
    public DataLoader createDataLoader(FakerConfig fakerConfig, JdbcTemplate jdbcTemplate, SqlDialect dialect) {
        final DataLoader defaultDataLoader = new DefaultDataLoaderFactory().createDataLoader(fakerConfig, jdbcTemplate, dialect);
        final BlockingQueue<Map<String, SqlArg>> precociousDataSet = new LinkedBlockingQueue<>();
        final int precociousSize = 4096;

        return (useFor, fakerTable, includeColumns, batchSize) -> {
            if (precociousSize <= 1) {
                return defaultDataLoader.loadSomeData(useFor, fakerTable, includeColumns, batchSize);
            }

            List<Map<String, SqlArg>> result = new ArrayList<>();
            for (int i = 0; i < batchSize; i++) {
                if (precociousDataSet.size() < batchSize) {
                    synchronized (this) {
                        if (precociousDataSet.size() < batchSize) {
                            List<Map<String, SqlArg>> someData = defaultDataLoader.loadSomeData(useFor, fakerTable, Collections.emptyList(), Math.max(precociousSize, batchSize));
                            precociousDataSet.addAll(someData);
                        }
                    }
                }

                Map<String, SqlArg> poll = precociousDataSet.poll();
                if (poll != null && !poll.isEmpty()) {
                    result.add(poll);
                }

            }
            return result;
        };
    }
}
