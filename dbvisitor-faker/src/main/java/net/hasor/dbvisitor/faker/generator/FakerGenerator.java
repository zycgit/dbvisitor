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
package net.hasor.dbvisitor.faker.generator;
import net.hasor.cobble.CollectionUtils;
import net.hasor.cobble.RandomUtils;
import net.hasor.dbvisitor.faker.FakerConfig;
import net.hasor.dbvisitor.faker.OpsType;

import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * FakerGenerator
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class FakerGenerator {
    private final String           generatorID;
    private final FakerConfig      fakerConfig;
    private final FakerFactory     fakerFactory;
    private final List<FakerTable> generatorTables;

    public FakerGenerator(FakerFactory fakerFactory) {
        this.generatorID = UUID.randomUUID().toString().replace("-", "");
        this.fakerConfig = fakerFactory.getFakerConfig();
        this.fakerFactory = fakerFactory;
        this.generatorTables = new CopyOnWriteArrayList<>();
    }

    public String getGeneratorID() {
        return this.generatorID;
    }

    public List<BoundQuery> generator() throws SQLException {
        return generator(null);
    }

    public List<BoundQuery> generator(OpsType opsType) throws SQLException {
        FakerTable table = randomTable();
        if (table == null) {
            return Collections.emptyList();
        }

        List<BoundQuery> events = new LinkedList<>();
        int opsCountPerTransaction = this.fakerConfig.randomOpsCountPerTrans();
        for (int i = 0; i < opsCountPerTransaction; i++) {
            List<BoundQuery> dataSet = this.generatorOps(randomTable(), opsType);
            events.addAll(dataSet);
        }
        return events;
    }

    public List<BoundQuery> generatorOneTable() throws SQLException {
        return generatorOneTable(null);
    }

    public List<BoundQuery> generatorOneTable(OpsType opsType) throws SQLException {
        FakerTable table = randomTable();
        if (table == null) {
            return Collections.emptyList();
        }

        List<BoundQuery> events = new LinkedList<>();
        int opsCountPerTransaction = this.fakerConfig.randomOpsCountPerTrans();
        for (int i = 0; i < opsCountPerTransaction; i++) {
            List<BoundQuery> dataSet = this.generatorOps(table, opsType);
            events.addAll(dataSet);
        }
        return events;
    }

    protected FakerTable randomTable() {
        if (!CollectionUtils.isEmpty(this.generatorTables)) {
            return this.generatorTables.get(RandomUtils.nextInt(0, this.generatorTables.size()));
        }
        return null;
    }

    protected List<BoundQuery> generatorOps(FakerTable fakerTable, OpsType opsType) throws SQLException {
        opsType = opsType != null ? opsType : this.fakerConfig.randomOps();
        if (opsType == null) {
            throw new IllegalStateException("no any boundary were declared, please init one.");
        }

        int batchSize = this.fakerConfig.randomBatchSizePerOps();

        switch (opsType) {
            case Insert:
                return fakerTable.buildInsert(batchSize);
            case Update:
                return fakerTable.buildUpdate(batchSize);
            case Delete:
                return fakerTable.buildDelete(batchSize);
            default:
                return Collections.emptyList();
        }
    }

    public FakerTable addTable(String catalog, String schema, String table) throws SQLException {
        try {
            FakerTable fetchTable = this.fakerFactory.fetchTable(catalog, schema, table);
            this.addTable(fetchTable);
            return fetchTable;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("init table failed : " + e.getMessage(), e);
        }
    }

    public FakerTable addTable(FakerTable table) {
        this.generatorTables.add(table);
        return table;
    }
}
