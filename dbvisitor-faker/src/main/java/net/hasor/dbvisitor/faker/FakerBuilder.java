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
package net.hasor.dbvisitor.faker;
import net.hasor.cobble.BeanUtils;
import net.hasor.cobble.CollectionUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.setting.SettingNode;
import net.hasor.dbvisitor.faker.meta.*;
import net.hasor.dbvisitor.faker.seed.SeedConfig;
import net.hasor.dbvisitor.faker.seed.bool.BooleanSeedConfig;
import net.hasor.dbvisitor.faker.seed.bytes.BytesSeedConfig;
import net.hasor.dbvisitor.faker.seed.date.DateSeedConfig;
import net.hasor.dbvisitor.faker.seed.date.DateType;
import net.hasor.dbvisitor.faker.seed.number.NumberSeedConfig;
import net.hasor.dbvisitor.faker.seed.number.NumberType;
import net.hasor.dbvisitor.faker.seed.string.StringSeedConfig;
import net.hasor.dbvisitor.faker.seed.string.characters.BitCharacters;
import net.hasor.dbvisitor.faker.strategy.ConservativeStrategy;
import net.hasor.dbvisitor.faker.strategy.Strategy;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.*;

import static net.hasor.dbvisitor.faker.seed.string.StandardCharacterSet.*;

/**
 * FakerTable 构建器
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class FakerBuilder {
    private final JdbcFetchMetaProvider metaProvider;
    private final ClassLoader           classLoader;

    public FakerBuilder(Connection connection, ClassLoader classLoader) {
        this.metaProvider = new JdbcFetchMetaProvider(connection);
        this.classLoader = classLoader == null ? Thread.currentThread().getContextClassLoader() : classLoader;
    }

    public FakerBuilder(DataSource dataSource, ClassLoader classLoader) {
        this.metaProvider = new JdbcFetchMetaProvider(dataSource);
        this.classLoader = classLoader == null ? Thread.currentThread().getContextClassLoader() : classLoader;
    }

    public FakerTable buildFakerTable(SettingNode tableConfig) throws Exception {
        FakerTable fakerTable = buildTable(tableConfig);
        List<FakerColumn> fakerColumns = buildColumns(tableConfig, fakerTable);

        fakerTable.setColumns(fakerColumns);
        return fakerTable;
    }

    protected FakerTable buildTable(SettingNode tableConfig) throws Exception {
        String catalogName = tableConfig.getSubValue("catalog");
        String schemaName = tableConfig.getSubValue("schema");
        String tableName = tableConfig.getSubValue("table");

        JdbcTable table = this.metaProvider.getTable(catalogName, schemaName, tableName);
        JdbcPrimaryKey primaryKey = this.metaProvider.getPrimaryKey(catalogName, schemaName, tableName);
        List<JdbcIndex> indexKeys = this.metaProvider.getIndexes(catalogName, schemaName, tableName);

        FakerTable fakerTable = new FakerTable();
        fakerTable.setSchema(StringUtils.isNotBlank(table.getSchema()) ? table.getSchema() : table.getCatalog());
        fakerTable.setTable(table.getTable());

        if (primaryKey != null) {
            fakerTable.setPeggingCol(primaryKey.getColumns());
        } else if (CollectionUtils.isNotEmpty(indexKeys)) {
            List<List<String>> peggingUnique = new ArrayList<>();
            List<List<String>> peggingIdx = new ArrayList<>();
            for (JdbcIndex idx : indexKeys) {
                if (idx.isUnique()) {
                    peggingUnique.add(idx.getColumns());
                } else {
                    peggingIdx.add(idx.getColumns());
                }
            }

            peggingUnique.sort(Comparator.comparingInt(List::size));
            peggingIdx.sort(Comparator.comparingInt(List::size));

            if (!peggingUnique.isEmpty()) {
                fakerTable.setPeggingCol(peggingUnique.get(0));
            } else if (!peggingIdx.isEmpty()) {
                fakerTable.setPeggingCol(peggingIdx.get(0));
            }
        }

        return fakerTable;
    }

    protected List<FakerColumn> buildColumns(SettingNode tableConfig, FakerTable fakerTable) throws Exception {
        String strategyType = tableConfig.getSubValue("strategy");
        String[] ignoreCols = tableConfig.getSubValues("ignoreCols");
        SettingNode columnsConfig = tableConfig.getSubNode("columns");

        Strategy strategy = new ConservativeStrategy();
        if (StringUtils.isNotBlank(strategyType)) {
            Class<?> seedFactoryType = this.classLoader.loadClass(strategyType);
            strategy = (Strategy) seedFactoryType.newInstance();
        }

        List<JdbcColumn> columns = this.metaProvider.getColumns(fakerTable.getCatalog(), fakerTable.getSchema(), fakerTable.getTable());
        Set<String> ignoreSet = new HashSet<>(Arrays.asList(ignoreCols));
        List<FakerColumn> columnList = new ArrayList<>();
        for (JdbcColumn jdbcColumn : columns) {
            SettingNode columnConfig = columnsConfig == null ? null : columnsConfig.getSubNode(jdbcColumn.getColumnName());
            SeedConfig seedConfig = createSeedConfig(jdbcColumn, columnConfig, strategy);

            FakerColumn fakerColumn = new FakerColumn();
            fakerColumn.setColumn(jdbcColumn.getColumnName());
            fakerColumn.setSqlType(jdbcColumn.getSqlType());
            fakerColumn.setSeedType(seedConfig.getSeedType());
            fakerColumn.setSeedConfig(seedConfig);
            fakerColumn.setIgnore(ignoreSet.contains(jdbcColumn.getColumnName()));

            columnList.add(fakerColumn);
        }

        return columnList;
    }

    private SeedConfig createSeedConfig(JdbcColumn jdbcColumn, SettingNode columnConfig, Strategy strategy) throws Exception {
        // try use setting create it
        SeedConfig seedConfig = this.createSeedConfig(columnConfig);

        // use jdbcColumn create it
        if (seedConfig == null) {
            seedConfig = this.createSeedConfig(jdbcColumn.getSqlType());
            if (seedConfig == null) {
                throw new UnsupportedOperationException("默认不支持，同时没有找到对应的配置。");
            }

            if (Boolean.TRUE.equals(jdbcColumn.getNullable())) {
                seedConfig.setAllowNullable(true);
                seedConfig.setNullableRatio(20f);
            }

            strategy.applyConfig(seedConfig, jdbcColumn); // first apply JdbcColumn strategy
        }

        // final apply form strategy
        strategy.applyConfig(seedConfig, jdbcColumn);

        // final apply form config
        Class<?> configClass = seedConfig.getClass();
        String[] propertySet = columnConfig.getSubKeys();
        for (String property : propertySet) {
            Class<?> propertyType = BeanUtils.getPropertyType(configClass, property);
            Object propertyValue = columnConfig.getSubValue(property);
            if (propertyType == null) {
                continue;
            }
            BeanUtils.writeProperty(seedConfig, property, propertyValue);
        }
        return seedConfig;
    }

    private SeedConfig createSeedConfig(SettingNode settingConfig) throws ReflectiveOperationException {
        String seedFactoryStr = settingConfig == null ? null : settingConfig.getSubValue("seedFactory");
        if (StringUtils.isBlank(seedFactoryStr)) {
            return null;
        }

        Class<?> seedFactoryType = this.classLoader.loadClass(seedFactoryStr);
        SeedConfig seedConfig = (SeedConfig) seedFactoryType.newInstance();

        for (String subKey : settingConfig.getSubKeys()) {
            String[] subValue = settingConfig.getSubValues(subKey);
            if (subValue == null || subValue.length == 0) {
                continue;
            }
            if (subValue.length == 1) {
                seedConfig.getConfigMap().put(subKey, subValue[0]);
            } else {
                seedConfig.getConfigMap().put(subKey, Arrays.asList(subValue));
            }
        }
        return seedConfig;
    }

    private SeedConfig createSeedConfig(JdbcSqlTypes jdbcType) {
        switch (jdbcType) {
            case BOOLEAN: {
                return new BooleanSeedConfig();
            }
            case BIT: {
                StringSeedConfig config = new StringSeedConfig();
                config.setCharacterSet(new HashSet<>(Collections.singletonList(new BitCharacters())));
                return config;
            }
            case TINYINT: {
                NumberSeedConfig config = new NumberSeedConfig();
                config.setNumberType(NumberType.Byte);
                return config;
            }
            case SMALLINT: {
                NumberSeedConfig config = new NumberSeedConfig();
                config.setNumberType(NumberType.Sort);
                return config;
            }
            case INTEGER: {
                NumberSeedConfig config = new NumberSeedConfig();
                config.setNumberType(NumberType.Integer);
                return config;
            }
            case BIGINT: {
                NumberSeedConfig config = new NumberSeedConfig();
                config.setNumberType(NumberType.Long);
                return config;
            }
            case FLOAT:
            case REAL: {
                NumberSeedConfig config = new NumberSeedConfig();
                config.setNumberType(NumberType.Float);
                return config;
            }
            case DOUBLE: {
                NumberSeedConfig config = new NumberSeedConfig();
                config.setNumberType(NumberType.Double);
                return config;
            }
            case NUMERIC:
            case DECIMAL: {
                NumberSeedConfig config = new NumberSeedConfig();
                config.setNumberType(NumberType.Decimal);
                return config;
            }
            case CHAR:
            case NCHAR:
            case VARCHAR:
            case NVARCHAR:
            case LONGVARCHAR:
            case LONGNVARCHAR:
            case CLOB:
            case NCLOB: {
                StringSeedConfig config = new StringSeedConfig();
                config.setCharacterSet(new HashSet<>(Arrays.asList(CAPITAL_LETTER, SMALL_LETTER, NUMERIC)));
                return config;
            }
            case BINARY:
            case VARBINARY:
            case LONGVARBINARY:
            case BLOB: {
                return new BytesSeedConfig();
            }
            case DATE: {
                DateSeedConfig config = new DateSeedConfig();
                config.setDateType(DateType.SqlDate);
                return config;
            }
            case TIME: {
                DateSeedConfig config = new DateSeedConfig();
                config.setDateType(DateType.SqlTime);
                return config;
            }
            case TIMESTAMP: {
                DateSeedConfig config = new DateSeedConfig();
                config.setDateType(DateType.SqlTimestamp);
                return config;
            }
            case TIME_WITH_TIMEZONE: {
                DateSeedConfig config = new DateSeedConfig();
                config.setDateType(DateType.OffsetTime);
                return config;
            }
            case TIMESTAMP_WITH_TIMEZONE: {
                DateSeedConfig config = new DateSeedConfig();
                config.setDateType(DateType.OffsetDateTime);
                return config;
            }
            case SQLXML:
            case STRUCT:
            case ARRAY:
            case DATALINK:
            case NULL:
            case OTHER:
            case JAVA_OBJECT:
            case DISTINCT:
            case REF:
            case ROWID:
            case REF_CURSOR:
            default:
                return null;
        }
    }
}
