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
package net.hasor.dbvisitor.faker;
import net.hasor.cobble.BeanUtils;
import net.hasor.cobble.CollectionUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.setting.SettingNode;
import net.hasor.dbvisitor.faker.meta.*;
import net.hasor.dbvisitor.faker.seed.SeedConfig;
import net.hasor.dbvisitor.faker.seed.SeedFactory;
import net.hasor.dbvisitor.faker.seed.bool.BooleanSeedConfig;
import net.hasor.dbvisitor.faker.seed.bool.BooleanSeedFactory;
import net.hasor.dbvisitor.faker.seed.bytes.BytesSeedConfig;
import net.hasor.dbvisitor.faker.seed.bytes.BytesSeedFactory;
import net.hasor.dbvisitor.faker.seed.date.DateSeedConfig;
import net.hasor.dbvisitor.faker.seed.date.DateSeedFactory;
import net.hasor.dbvisitor.faker.seed.date.DateType;
import net.hasor.dbvisitor.faker.seed.number.NumberSeedConfig;
import net.hasor.dbvisitor.faker.seed.number.NumberSeedFactory;
import net.hasor.dbvisitor.faker.seed.number.NumberType;
import net.hasor.dbvisitor.faker.seed.string.StringSeedConfig;
import net.hasor.dbvisitor.faker.seed.string.StringSeedFactory;
import net.hasor.dbvisitor.faker.seed.string.characters.BitCharacters;
import net.hasor.dbvisitor.faker.strategy.ConservativeStrategy;
import net.hasor.dbvisitor.faker.strategy.Strategy;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

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
    private       TypeHandlerRegistry   typeHandlerRegistry;

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
        fakerTable.setSchema(table.getCatalog());
        fakerTable.setSchema(table.getSchema());
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
            Class<?> strategyClass = this.classLoader.loadClass(strategyType);
            strategy = (Strategy) strategyClass.newInstance();
        }

        List<JdbcColumn> columns = this.metaProvider.getColumns(fakerTable.getCatalog(), fakerTable.getSchema(), fakerTable.getTable());
        Set<String> ignoreSet = new HashSet<>(Arrays.asList(ignoreCols));
        List<FakerColumn> columnList = new ArrayList<>();
        for (JdbcColumn jdbcColumn : columns) {
            SettingNode columnConfig = columnsConfig == null ? null : columnsConfig.getSubNode(jdbcColumn.getColumnName());
            FakerColumn fakerColumn = createFakerColumn(jdbcColumn, columnConfig, strategy, ignoreSet);
            columnList.add(fakerColumn);
        }

        return columnList;
    }

    private FakerColumn createFakerColumn(JdbcColumn jdbcColumn, SettingNode columnConfig, Strategy strategy, Set<String> ignoreSet) throws Exception {
        // try use setting create it
        SeedFactory seedFactory = this.createSeedFactory(columnConfig);
        SeedConfig seedConfig = null;
        if (seedFactory != null) {
            seedConfig = this.createSeedConfig(seedFactory, columnConfig);
        }

        // use jdbcColumn create it
        if (seedConfig == null) {
            seedFactory = this.createSeedFactory(jdbcColumn.getSqlType());
            seedConfig = this.createSeedConfig(seedFactory, jdbcColumn.getSqlType());
            if (seedConfig == null) {
                throw new UnsupportedOperationException("SeedFactory not specified, or SqlType(" + jdbcColumn.getJdbcNumber() + ") Unsupported.");
            }
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

        FakerColumn fakerColumn = new FakerColumn();
        fakerColumn.setColumn(jdbcColumn.getColumnName());
        fakerColumn.setSqlType(jdbcColumn.getJdbcNumber());
        fakerColumn.setJavaType(confirmJavaType(seedConfig));
        fakerColumn.setSeedType(seedConfig.getSeedType());
        fakerColumn.setSeedConfig(seedConfig);
        fakerColumn.setSeedFactory(seedFactory);
        fakerColumn.setIgnore(ignoreSet.contains(jdbcColumn.getColumnName()));
        return fakerColumn;
    }

    private SeedConfig createSeedConfig(SeedFactory seedFactory, SettingNode settingConfig) {
        SeedConfig seedConfig = seedFactory.newConfig();
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

    private SeedConfig createSeedConfig(SeedFactory seedFactory, JdbcSqlTypes jdbcType) {
        if (jdbcType == null) {
            return null;
        }
        switch (jdbcType) {
            case BOOLEAN: {
                return (BooleanSeedConfig) seedFactory.newConfig();
            }
            case BIT: {
                StringSeedConfig config = (StringSeedConfig) seedFactory.newConfig();
                config.setCharacterSet(new HashSet<>(Collections.singletonList(new BitCharacters())));
                return config;
            }
            case TINYINT: {
                NumberSeedConfig config = (NumberSeedConfig) seedFactory.newConfig();
                config.setNumberType(NumberType.Byte);
                return config;
            }
            case SMALLINT: {
                NumberSeedConfig config = (NumberSeedConfig) seedFactory.newConfig();
                config.setNumberType(NumberType.Short);
                return config;
            }
            case INTEGER: {
                NumberSeedConfig config = (NumberSeedConfig) seedFactory.newConfig();
                config.setNumberType(NumberType.Integer);
                return config;
            }
            case BIGINT: {
                NumberSeedConfig config = (NumberSeedConfig) seedFactory.newConfig();
                config.setNumberType(NumberType.Long);
                return config;
            }
            case FLOAT:
            case REAL: {
                NumberSeedConfig config = (NumberSeedConfig) seedFactory.newConfig();
                config.setNumberType(NumberType.Float);
                return config;
            }
            case DOUBLE: {
                NumberSeedConfig config = (NumberSeedConfig) seedFactory.newConfig();
                config.setNumberType(NumberType.Double);
                return config;
            }
            case NUMERIC:
            case DECIMAL: {
                NumberSeedConfig config = (NumberSeedConfig) seedFactory.newConfig();
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
                StringSeedConfig config = (StringSeedConfig) seedFactory.newConfig();
                config.setCharacterSet(new HashSet<>(Arrays.asList(CAPITAL_LETTER, SMALL_LETTER, NUMERIC)));
                return config;
            }
            case BINARY:
            case VARBINARY:
            case LONGVARBINARY:
            case BLOB: {
                return (BytesSeedConfig) seedFactory.newConfig();
            }
            case DATE: {
                DateSeedConfig config = (DateSeedConfig) seedFactory.newConfig();
                config.setDateType(DateType.SqlDate);
                return config;
            }
            case TIME: {
                DateSeedConfig config = (DateSeedConfig) seedFactory.newConfig();
                config.setDateType(DateType.SqlTime);
                return config;
            }
            case TIMESTAMP: {
                DateSeedConfig config = (DateSeedConfig) seedFactory.newConfig();
                config.setDateType(DateType.SqlTimestamp);
                return config;
            }
            case TIME_WITH_TIMEZONE: {
                DateSeedConfig config = (DateSeedConfig) seedFactory.newConfig();
                config.setDateType(DateType.OffsetTime);
                return config;
            }
            case TIMESTAMP_WITH_TIMEZONE: {
                DateSeedConfig config = (DateSeedConfig) seedFactory.newConfig();
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

    private SeedFactory createSeedFactory(SettingNode settingConfig) throws ReflectiveOperationException {
        String seedFactoryStr = settingConfig == null ? null : settingConfig.getSubValue("seedFactory");
        if (StringUtils.isBlank(seedFactoryStr)) {
            return null;
        }

        Class<?> seedFactoryType = this.classLoader.loadClass(seedFactoryStr);
        return (SeedFactory) seedFactoryType.newInstance();
    }

    private SeedFactory createSeedFactory(JdbcSqlTypes jdbcType) {
        switch (jdbcType) {
            case BOOLEAN:
                return new BooleanSeedFactory();
            case TINYINT:
            case SMALLINT:
            case INTEGER:
            case BIGINT:
            case FLOAT:
            case REAL:
            case DOUBLE:
            case NUMERIC:
            case DECIMAL:
                return new NumberSeedFactory();
            case BIT:
            case CHAR:
            case NCHAR:
            case VARCHAR:
            case NVARCHAR:
            case LONGVARCHAR:
            case LONGNVARCHAR:
            case CLOB:
            case NCLOB:
                return new StringSeedFactory();
            case BINARY:
            case VARBINARY:
            case LONGVARBINARY:
            case BLOB:
                return new BytesSeedFactory();
            case DATE:
            case TIME:
            case TIMESTAMP:
            case TIME_WITH_TIMEZONE:
            case TIMESTAMP_WITH_TIMEZONE:
                return new DateSeedFactory();
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

    private Class<?> confirmJavaType(SeedConfig seedConfig) {
        switch (seedConfig.getSeedType()) {
            case Bytes:
                return byte[].class;
            case Date:
                DateSeedConfig dateSeedConfig = (DateSeedConfig) seedConfig;
                return dateSeedConfig.getDateType().getDateType();
            case Number:
                NumberSeedConfig numberSeedConfig = (NumberSeedConfig) seedConfig;
                return numberSeedConfig.getNumberType().getDateType();
            case String:
            case Enums:
                return String.class;
            case Boolean:
                return Boolean.class;
            case Custom:
            default:
                return null;
        }
    }
}