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
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.setting.DefaultSettings;
import net.hasor.cobble.setting.SettingNode;
import net.hasor.cobble.setting.provider.StreamType;
import net.hasor.dbvisitor.dialect.DefaultSqlDialect;
import net.hasor.dbvisitor.faker.FakerConfig;
import net.hasor.dbvisitor.faker.OpsType;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * FakerGenerator
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class FakerRepository {
    private final static Logger           logger = Logger.getLogger(FakerRepository.class);
    private final        String           generatorID;
    private final        FakerConfig      fakerConfig;
    private final        FakerFactory     fakerFactory;
    private final        List<FakerTable> generatorTables;

    public FakerRepository(FakerFactory fakerFactory) {
        this.generatorID = UUID.randomUUID().toString().replace("-", "");
        this.fakerConfig = fakerFactory.getFakerConfig();
        this.fakerFactory = fakerFactory;
        this.generatorTables = new CopyOnWriteArrayList<>();
    }

    public String getGeneratorID() {
        return this.generatorID;
    }

    public FakerConfig getConfig() {
        return this.fakerConfig;
    }

    /** 从生成器中随机选择一张 fakerTable 表，并为这张表生成一个事务的语句。语句类型随机 */
    public List<BoundQuery> generator() throws SQLException {
        return generator(this.fakerConfig.randomOps());
    }

    /** 从生成器中随机选择一张 fakerTable 表，并为这张表生成一个事务的语句。语句类型由 opsType 决定 */
    public List<BoundQuery> generator(OpsType opsType) throws SQLException {
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

    /** 从生成器中随机选择一张 fakerTable 表 */
    protected FakerTable randomTable() {
        if (!CollectionUtils.isEmpty(this.generatorTables)) {
            if (this.generatorTables.size() == 1) {
                return this.generatorTables.get(0);
            } else {
                return this.generatorTables.get(RandomUtils.nextInt(0, this.generatorTables.size() - 1));
            }
        }
        return null;
    }

    /** 为 fakerTable 生成一批 opsType 类型 DML 语句 */
    protected List<BoundQuery> generatorOps(FakerTable fakerTable, OpsType opsType) throws SQLException {
        Objects.requireNonNull(fakerTable, "fakerTable is null.");
        Objects.requireNonNull(opsType, "opsType is null.");

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

    /** 添加一个表到生成器中，表的列信息通过元信息服务来补全。 */
    public FakerTable addTable(String catalog, String schema, String table) throws SQLException {
        try {
            FakerTable fetchTable = this.fakerFactory.fetchTable(catalog, schema, table);
            this.addTable(fetchTable);
            return fetchTable;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("init table failed : " + e.getMessage(), e);
        }
    }

    /** 添加一个表到生成器中 */
    protected FakerTable addTable(FakerTable table) {
        this.generatorTables.add(table);
        return table;
    }

    /** 从生成器中查找某个表 */
    public FakerTable findTable(String catalog, String schema, String table) {
        return this.generatorTables.stream().filter(fakerTable -> {
            return StringUtils.equals(fakerTable.getCatalog(), catalog) && //
                    StringUtils.equals(fakerTable.getSchema(), schema) && //
                    StringUtils.equals(fakerTable.getTable(), table);
        }).findFirst().orElse(null);
    }

    public void loadConfig(String config, StreamType streamType) throws Exception {
        DefaultSettings settings = new DefaultSettings();
        settings.addResource(config, streamType);
        settings.loadSettings();

        SettingNode[] tables = settings.getNodeArray("config.table");
        if (tables != null) {
            for (SettingNode table : tables) {
                FakerTable fakerTable = this.fakerFactory.fetchTable(table);
                if (fakerTable != null) {
                    String tableName = DefaultSqlDialect.DEFAULT.tableName(true, fakerTable.getCatalog(), fakerTable.getSchema(), fakerTable.getTable());
                    this.addTable(fakerTable);
                    logger.info("found table '" + tableName + "'");
                }
            }
        }
    }
}