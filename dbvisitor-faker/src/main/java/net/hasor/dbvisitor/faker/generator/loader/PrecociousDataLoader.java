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
import net.hasor.dbvisitor.faker.generator.FakerTable;
import net.hasor.dbvisitor.faker.generator.UseFor;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;

import java.sql.SQLException;
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
public class PrecociousDataLoader extends DefaultDataLoader {
    private final BlockingQueue<Map<String, Object>> precociousDataSet = new LinkedBlockingQueue<>();
    private       int                                precociousSize    = 4096;

    public PrecociousDataLoader(JdbcTemplate jdbcTemplate, SqlDialect dialect) {
        super(jdbcTemplate, dialect);
    }

    /** 超前缓存的数据数量 */
    public int getPrecociousSize() {
        return precociousSize;
    }

    /** 超前缓存的数据数量 */
    public void setPrecociousSize(int precociousSize) {
        this.precociousSize = precociousSize;
    }

    @Override
    public List<Map<String, Object>> loadSomeData(UseFor useFor, FakerTable fakerTable, List<String> includeColumns, int batchSize) throws SQLException {
        if (this.precociousSize <= 1) {
            return super.loadSomeData(useFor, fakerTable, includeColumns, batchSize);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < batchSize; i++) {
            if (this.precociousDataSet.size() < batchSize) {
                synchronized (this) {
                    if (this.precociousDataSet.size() < batchSize) {
                        List<Map<String, Object>> someData = super.loadSomeData(useFor, fakerTable, Collections.emptyList(), Math.max(this.precociousSize, batchSize));
                        this.precociousDataSet.addAll(someData);
                    }
                }
            }

            Map<String, Object> poll = this.precociousDataSet.poll();
            if (poll != null) {
                result.add(poll);
            }

        }
        return result;
    }
}
