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
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.setting.SettingNode;
import net.hasor.cobble.setting.data.TreeNode;
import net.hasor.dbvisitor.JdbcUtils;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.dialect.SqlDialectRegister;
import net.hasor.dbvisitor.faker.generator.FakerColumn;
import net.hasor.dbvisitor.faker.generator.FakerTable;
import net.hasor.dbvisitor.faker.generator.GeneratorTable;
import net.hasor.dbvisitor.faker.generator.UseFor;
import net.hasor.dbvisitor.faker.meta.JdbcColumn;
import net.hasor.dbvisitor.faker.meta.JdbcFetchMetaProvider;
import net.hasor.dbvisitor.faker.meta.JdbcSqlTypes;
import net.hasor.dbvisitor.faker.meta.JdbcTable;
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
import net.hasor.dbvisitor.faker.strategy.Strategy;
import net.hasor.dbvisitor.jdbc.ConnectionCallback;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static net.hasor.dbvisitor.faker.FakerConfigEnum.*;
import static net.hasor.dbvisitor.faker.seed.string.StandardCharacterSet.*;

/**
 * FakerTable 构建器
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class FakerFactory {
    private final JdbcTemplate          jdbcTemplate;
    private final JdbcFetchMetaProvider metaProvider;
    private final FakerConfig           fakerConfig;
    private final SqlDialect            dialect;

    public FakerFactory(Connection connection) throws SQLException {
        this(connection, new FakerConfig());
    }

    public FakerFactory(DataSource dataSource) throws SQLException {
        this(dataSource, new FakerConfig());
    }

    public FakerFactory(Connection connection, FakerConfig fakerConfig) throws SQLException {
        this.jdbcTemplate = new JdbcTemplate(connection);
        this.metaProvider = new JdbcFetchMetaProvider(connection);
        this.fakerConfig = fakerConfig;
        this.dialect = this.initDialect(fakerConfig);
    }

    public FakerFactory(DataSource dataSource, FakerConfig fakerConfig) throws SQLException {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.metaProvider = new JdbcFetchMetaProvider(dataSource);
        this.fakerConfig = fakerConfig;
        this.dialect = this.initDialect(fakerConfig);
    }

    protected SqlDialect initDialect(FakerConfig fakerConfig) throws SQLException {
        if (fakerConfig.getDialect() != null) {
            return fakerConfig.getDialect();
        }
        return this.jdbcTemplate.execute((ConnectionCallback<SqlDialect>) con -> {
            String jdbcUrl = con.getMetaData().getURL();
            String jdbcDriverName = con.getMetaData().getDriverName();
            String dbType = JdbcUtils.getDbType(jdbcUrl, jdbcDriverName);
            if (StringUtils.isBlank(dbType)) {
                throw new IllegalArgumentException("SqlDialect missing.");
            }

            return SqlDialectRegister.findOrCreate(dbType);
        });
    }

    public JdbcTemplate getJdbcTemplate() {
        return this.jdbcTemplate;
    }

    public GeneratorTable fetchTable(SettingNode tableConfig) throws Exception {
        String catalog = tableConfig.getSubValue(TABLE_CATALOG.getConfigKey());
        String schema = tableConfig.getSubValue(TABLE_SCHEMA.getConfigKey());
        String table = tableConfig.getSubValue(TABLE_TABLE.getConfigKey());

        FakerTable tableInfo = this.buildTable(catalog, schema, table, tableConfig);
        return new GeneratorTable(tableInfo, this.dialect, this.jdbcTemplate);
    }

    public GeneratorTable fetchTable(String catalog, String schema, String table) throws Exception {
        FakerTable tableInfo = this.buildTable(catalog, schema, table, null);
        return new GeneratorTable(tableInfo, this.dialect, this.jdbcTemplate);
    }

    public FakerTable buildTable(String catalog, String schema, String table, SettingNode tableConfig) throws Exception {
        JdbcTable jdbcTable = this.metaProvider.getTable(catalog, schema, table);
        if (jdbcTable == null) {
            String tabName = String.format("%s.%s.%s", catalog, schema, table);
            throw new IllegalArgumentException("table '" + tabName + "' is not exist.");
        }

        FakerTable fakerTable = new FakerTable();
        fakerTable.setCatalog(catalog);
        fakerTable.setSchema(schema);
        fakerTable.setTable(table);
        fakerTable.setColumns(buildColumns(new TreeNode(), fakerTable));
        fakerTable.setFakerConfig(this.fakerConfig);
        fakerTable.setDataLoader(this.fakerConfig.getDataLoader());
        fakerTable.setInsertPolitic(SqlPolitic.FullCol);
        fakerTable.setUpdateSetPolitic(SqlPolitic.FullCol);
        fakerTable.setWherePolitic(SqlPolitic.KeyCol);

        if (tableConfig != null) {
            String insertPoliticStr = tableConfig.getSubValue(TABLE_ACT_POLITIC_INSERT.getConfigKey());
            String updatePoliticStr = tableConfig.getSubValue(TABLE_ACT_POLITIC_UPDATE.getConfigKey());
            String wherePoliticStr = tableConfig.getSubValue(TABLE_ACT_POLITIC_WHERE.getConfigKey());

            fakerTable.setInsertPolitic(SqlPolitic.valueOf(insertPoliticStr, SqlPolitic.FullCol));
            fakerTable.setUpdateSetPolitic(SqlPolitic.valueOf(updatePoliticStr, SqlPolitic.FullCol));
            fakerTable.setWherePolitic(SqlPolitic.valueOf(wherePoliticStr, SqlPolitic.KeyCol));
        }

        return fakerTable;
    }

    protected List<FakerColumn> buildColumns(SettingNode tableConfig, FakerTable fakerTable) throws Exception {
        SettingNode columnsConfig = tableConfig.getSubNode(TABLE_COLUMNS.getConfigKey());
        String[] ignoreCols = tableConfig.getSubValues(TABLE_COL_IGNORE_ALL.getConfigKey());
        String[] ignoreInsertCols = tableConfig.getSubValues(TABLE_COL_IGNORE_INSERT.getConfigKey());
        String[] ignoreUpdateCols = tableConfig.getSubValues(TABLE_COL_IGNORE_UPDATE.getConfigKey());
        String[] ignoreWhereCols = tableConfig.getSubValues(TABLE_COL_IGNORE_WHERE.getConfigKey());
        Set<String> ignoreSet = new HashSet<>(Arrays.asList(ignoreCols));
        Set<String> ignoreInsertSet = new HashSet<>(Arrays.asList(ignoreInsertCols));
        Set<String> ignoreUpdateSet = new HashSet<>(Arrays.asList(ignoreUpdateCols));
        Set<String> ignoreWhereSet = new HashSet<>(Arrays.asList(ignoreWhereCols));

        List<JdbcColumn> columns = this.metaProvider.getColumns(fakerTable.getCatalog(), fakerTable.getSchema(), fakerTable.getTable());
        List<FakerColumn> columnList = new ArrayList<>();
        Strategy strategy = this.fakerConfig.getStrategy();

        for (JdbcColumn jdbcColumn : columns) {
            SettingNode columnConfig = columnsConfig == null ? null : columnsConfig.getSubNode(jdbcColumn.getColumnName());
            FakerColumn fakerColumn = createFakerColumn(fakerTable, jdbcColumn, columnConfig, strategy, ignoreSet, ignoreInsertSet, ignoreUpdateSet, ignoreWhereSet);
            columnList.add(fakerColumn);
        }

        return columnList;
    }

    private FakerColumn createFakerColumn(FakerTable fakerTable, JdbcColumn jdbcColumn, SettingNode columnConfig, Strategy strategy,//
            Set<String> ignoreSet, Set<String> ignoreInsertSet, Set<String> ignoreUpdateSet, Set<String> ignoreWhereSet) throws ReflectiveOperationException {
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
        strategy.applyConfig(fakerTable, seedConfig, jdbcColumn);

        // final apply form config
        Class<?> configClass = seedConfig.getClass();
        if (columnConfig != null) {
            String[] propertySet = columnConfig.getSubKeys();
            for (String property : propertySet) {
                Class<?> propertyType = BeanUtils.getPropertyType(configClass, property);
                Object propertyValue = columnConfig.getSubValue(property);
                if (propertyType == null) {
                    continue;
                }
                BeanUtils.writeProperty(seedConfig, property, propertyValue);
            }
        }

        Set<UseFor> ignoreAct = new HashSet<>();
        ignoreAct.addAll(ignoreSet.contains(jdbcColumn.getColumnName()) ? Arrays.asList(UseFor.values()) : Collections.emptySet());
        ignoreAct.addAll(ignoreInsertSet.contains(jdbcColumn.getColumnName()) ? Collections.singletonList(UseFor.Insert) : Collections.emptySet());
        ignoreAct.addAll(ignoreUpdateSet.contains(jdbcColumn.getColumnName()) ? Collections.singletonList(UseFor.UpdateSet) : Collections.emptySet());
        ignoreAct.addAll(ignoreWhereSet.contains(jdbcColumn.getColumnName()) ? Arrays.asList(UseFor.UpdateWhere, UseFor.DeleteWhere) : Collections.emptySet());

        if (Boolean.TRUE.equals(jdbcColumn.getGeneratedColumn())) {
            ignoreAct.add(UseFor.Insert);
        }

        FakerColumn fakerColumn = new FakerColumn();
        fakerColumn.setColumn(jdbcColumn.getColumnName());
        fakerColumn.setKey(jdbcColumn.isPrimaryKey() || jdbcColumn.isUniqueKey());
        fakerColumn.setSqlType(jdbcColumn.getJdbcNumber());
        fakerColumn.setJavaType(confirmJavaType(seedConfig));
        fakerColumn.setSeedType(seedConfig.getSeedType());
        fakerColumn.setSeedConfig(seedConfig);
        fakerColumn.setSeedFactory(seedFactory);
        fakerColumn.setCanBeCut(StringUtils.isNotBlank(jdbcColumn.getDefaultValue()) || Boolean.TRUE.equals(jdbcColumn.getNullable()));
        fakerColumn.setIgnoreAct(ignoreAct);
        return fakerColumn;
    }

    private SeedConfig createSeedConfig(SeedFactory seedFactory, SettingNode columnConfig) {
        SeedConfig seedConfig = seedFactory.newConfig();
        for (String subKey : columnConfig.getSubKeys()) {
            String[] subValue = columnConfig.getSubValues(subKey);
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

    private SeedFactory createSeedFactory(SettingNode columnConfig) throws ReflectiveOperationException {
        String seedFactoryStr = columnConfig == null ? null : columnConfig.getSubValue(COLUMN_SEED_FACTORY.getConfigKey());
        if (StringUtils.isBlank(seedFactoryStr)) {
            return null;
        }

        Class<?> seedFactoryType = this.fakerConfig.getClassLoader().loadClass(seedFactoryStr);
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