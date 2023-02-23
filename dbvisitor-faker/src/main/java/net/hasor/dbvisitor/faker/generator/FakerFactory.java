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
import net.hasor.cobble.BeanUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.logging.Logger;
import net.hasor.cobble.setting.SettingNode;
import net.hasor.cobble.setting.data.TreeNode;
import net.hasor.dbvisitor.JdbcUtils;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.dialect.SqlDialectRegister;
import net.hasor.dbvisitor.faker.FakerConfig;
import net.hasor.dbvisitor.faker.FakerConfigEnum;
import net.hasor.dbvisitor.faker.generator.processor.DefaultTypeProcessorFactory;
import net.hasor.dbvisitor.faker.generator.processor.DslTypeProcessorFactory;
import net.hasor.dbvisitor.faker.meta.JdbcColumn;
import net.hasor.dbvisitor.faker.meta.JdbcFetchMeta;
import net.hasor.dbvisitor.faker.meta.JdbcFetchMetaProvider;
import net.hasor.dbvisitor.faker.meta.JdbcTable;
import net.hasor.dbvisitor.faker.provider.mysql.meta.MySqlFetchMeta;
import net.hasor.dbvisitor.faker.seed.SeedConfig;
import net.hasor.dbvisitor.faker.seed.SeedFactory;
import net.hasor.dbvisitor.faker.seed.SeedType;
import net.hasor.dbvisitor.faker.seed.array.ArraySeedFactory;
import net.hasor.dbvisitor.jdbc.ConnectionCallback;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.*;

/**
 * FakerTable 构建器
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class FakerFactory {
    private static final Logger                      logger = Logger.getLogger(FakerFactory.class);
    private final        JdbcTemplate                jdbcTemplate;
    private final        JdbcFetchMetaProvider       metaProvider;
    private final        FakerConfig                 fakerConfig;
    private final        String                      dbType;
    private final        Map<String, Object>         variables;
    private final        SqlDialect                  sqlDialect;
    private final        DefaultTypeProcessorFactory typeDialect;

    public FakerFactory(Connection connection) throws SQLException, IOException {
        this(connection, new FakerConfig());
    }

    public FakerFactory(DataSource dataSource) throws SQLException, IOException {
        this(dataSource, new FakerConfig());
    }

    public FakerFactory(Connection connection, FakerConfig config) throws SQLException, IOException {
        this.jdbcTemplate = new JdbcTemplate(connection);
        this.fakerConfig = config;
        this.dbType = initDbType(config);
        this.metaProvider = new JdbcFetchMetaProvider(connection, initFetchMeta(this.dbType, config));
        this.variables = this.initVariables(this.dbType, config);
        this.sqlDialect = this.initSqlDialect(this.dbType, config);
        this.typeDialect = this.initTypeDialect(this.dbType, config, this.variables);
    }

    public FakerFactory(DataSource dataSource, FakerConfig config) throws SQLException, IOException {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.fakerConfig = config;
        this.dbType = initDbType(config);
        this.metaProvider = new JdbcFetchMetaProvider(dataSource, initFetchMeta(this.dbType, config));
        this.variables = this.initVariables(this.dbType, config);
        this.sqlDialect = this.initSqlDialect(this.dbType, config);
        this.typeDialect = this.initTypeDialect(this.dbType, config, this.variables);
    }

    protected String initDbType(FakerConfig config) throws SQLException {
        return this.jdbcTemplate.execute((ConnectionCallback<String>) con -> {
            String jdbcUrl = con.getMetaData().getURL();
            String jdbcDriverName = con.getMetaData().getDriverName();
            String confDbType = config.getDbType();
            if (StringUtils.isNotBlank(confDbType)) {
                return confDbType;
            } else {
                return JdbcUtils.getDbType(jdbcUrl, jdbcDriverName);
            }
        });
    }

    protected Map<String, Object> initVariables(String dbType, FakerConfig fakerConfig) throws SQLException {
        Map<String, Object> javaVars = new HashMap<>();
        System.getProperties().forEach((k, v) -> {
            javaVars.put(String.valueOf(k), v);
        });

        Map<String, Object> envVars = new HashMap<>();
        System.getenv().forEach((k, v) -> envVars.put(String.valueOf(k), v));

        Map<String, Object> globalVars = new HashMap<>();
        globalVars.put("java", javaVars);
        globalVars.put("env", envVars);
        globalVars.put("dbType", dbType);
        globalVars.put("policy", fakerConfig.getPolicy());

        this.jdbcTemplate.execute((ConnectionCallback<Object>) con -> {
            DatabaseMetaData metaData = con.getMetaData();
            globalVars.put("jdbcUrl", metaData.getURL());
            globalVars.put("driverName", metaData.getDriverName());
            globalVars.put("driverVersion", metaData.getDriverVersion());
            globalVars.put("dbMajorVersion", metaData.getDatabaseMajorVersion());
            globalVars.put("dbMinorVersion", metaData.getDatabaseMinorVersion());
            globalVars.put("dbProductName", metaData.getDatabaseProductName());
            globalVars.put("dbProductVersion", metaData.getDatabaseProductVersion());
            return null;
        });

        return globalVars;
    }

    protected SqlDialect initSqlDialect(String dbType, FakerConfig fakerConfig) {
        if (fakerConfig.getSqlDialect() != null) {
            return fakerConfig.getSqlDialect();
        }
        if (StringUtils.isBlank(dbType)) {
            throw new IllegalArgumentException("SqlDialect missing.");
        } else {
            return SqlDialectRegister.findOrCreate(dbType);
        }
    }

    protected DefaultTypeProcessorFactory initTypeDialect(String dbType, FakerConfig fakerConfig, Map<String, Object> variables) throws IOException {
        if (fakerConfig.getTypeProcessorFactory() != null) {
            return fakerConfig.getTypeProcessorFactory();
        }

        if (StringUtils.isBlank(dbType)) {
            return new DefaultTypeProcessorFactory();
        } else {
            return new DslTypeProcessorFactory(dbType, variables, fakerConfig);
        }
    }

    protected JdbcFetchMeta initFetchMeta(String dbType, FakerConfig config) {
        if (config.getCustomFetchMeta() != null) {
            return config.getCustomFetchMeta();
        }

        switch (dbType) {
            case JdbcUtils.MARIADB:
            case JdbcUtils.MYSQL:
                return new MySqlFetchMeta();
            default:
                return null;
        }
    }

    protected FakerConfig getFakerConfig() {
        return this.fakerConfig;
    }

    protected JdbcTemplate getJdbcTemplate() {
        return this.jdbcTemplate;
    }

    protected SqlDialect getSqlDialect() {
        return this.sqlDialect;
    }

    public FakerTable fetchTable(SettingNode tableConfig) throws SQLException, ReflectiveOperationException {
        String catalog = tableConfig.getSubValue(FakerConfigEnum.TABLE_CATALOG.getConfigKey());
        String schema = tableConfig.getSubValue(FakerConfigEnum.TABLE_SCHEMA.getConfigKey());
        String table = tableConfig.getSubValue(FakerConfigEnum.TABLE_TABLE.getConfigKey());

        return this.buildTable(catalog, schema, table, tableConfig);
    }

    public FakerTable fetchTable(String catalog, String schema, String table) throws SQLException, ReflectiveOperationException {
        return this.buildTable(catalog, schema, table, null);
    }

    public FakerTable buildTable(String catalog, String schema, String table, SettingNode tableConfig) throws SQLException, ReflectiveOperationException {
        JdbcTable jdbcTable = this.metaProvider.getTable(catalog, schema, table);
        if (jdbcTable == null) {
            String tabName = String.format("%s.%s.%s", catalog, schema, table);
            throw new IllegalArgumentException("table '" + tabName + "' is not exist.");
        }

        FakerTable fakerTable = new FakerTable(catalog, schema, table, this);
        fakerTable.setUseQualifier(this.fakerConfig.isUseQualifier());
        fakerTable.setKeyChanges(this.fakerConfig.isKeyChanges());

        tableConfig = tableConfig == null ? new TreeNode() : tableConfig;
        buildColumns(fakerTable, tableConfig);

        String insertPoliticStr = tableConfig.getSubValue(FakerConfigEnum.TABLE_ACT_POLITIC_INSERT.getConfigKey());
        String updatePoliticStr = tableConfig.getSubValue(FakerConfigEnum.TABLE_ACT_POLITIC_UPDATE.getConfigKey());
        String wherePoliticStr = tableConfig.getSubValue(FakerConfigEnum.TABLE_ACT_POLITIC_WHERE.getConfigKey());

        fakerTable.setInsertPolitic(SqlPolitic.valueOfCode(insertPoliticStr, SqlPolitic.RandomCol));
        fakerTable.setUpdateSetPolitic(SqlPolitic.valueOfCode(updatePoliticStr, SqlPolitic.RandomCol));
        fakerTable.setWherePolitic(SqlPolitic.valueOfCode(wherePoliticStr, SqlPolitic.KeyCol));

        fakerTable.apply();
        return fakerTable;
    }

    protected void buildColumns(FakerTable fakerTable, SettingNode tableConfig) throws SQLException, ReflectiveOperationException {
        SettingNode columnsConfig = tableConfig.getSubNode(FakerConfigEnum.TABLE_COLUMNS.getConfigKey());
        String[] ignoreCols = tableConfig.getSubValues(FakerConfigEnum.TABLE_COL_IGNORE_ALL.getConfigKey());
        String[] ignoreInsertCols = tableConfig.getSubValues(FakerConfigEnum.TABLE_COL_IGNORE_INSERT.getConfigKey());
        String[] ignoreUpdateCols = tableConfig.getSubValues(FakerConfigEnum.TABLE_COL_IGNORE_UPDATE.getConfigKey());
        String[] ignoreWhereCols = tableConfig.getSubValues(FakerConfigEnum.TABLE_COL_IGNORE_WHERE.getConfigKey());
        Set<String> ignoreSet = new HashSet<>(Arrays.asList(ignoreCols));
        Set<String> ignoreInsertSet = new HashSet<>(Arrays.asList(ignoreInsertCols));
        Set<String> ignoreUpdateSet = new HashSet<>(Arrays.asList(ignoreUpdateCols));
        Set<String> ignoreWhereSet = new HashSet<>(Arrays.asList(ignoreWhereCols));

        List<JdbcColumn> columns = this.metaProvider.getColumns(fakerTable.getCatalog(), fakerTable.getSchema(), fakerTable.getTable());
        if (columns.isEmpty()) {
            throw new UnsupportedOperationException(fakerTable + " no columns were found in the meta information.");
        }

        for (JdbcColumn jdbcColumn : columns) {
            SettingNode columnConfig = columnsConfig == null ? null : columnsConfig.getSubNode(jdbcColumn.getColumnName());
            FakerColumn fakerColumn = createFakerColumn(fakerTable, jdbcColumn, columnConfig, ignoreSet, ignoreInsertSet, ignoreUpdateSet, ignoreWhereSet);
            if (fakerColumn != null) {
                fakerTable.addColumn(fakerColumn);
            }
        }
    }

    private FakerColumn createFakerColumn(FakerTable fakerTable, JdbcColumn jdbcColumn, SettingNode columnConfig,//
            Set<String> ignoreSet, Set<String> ignoreInsertSet, Set<String> ignoreUpdateSet, Set<String> ignoreWhereSet) throws ReflectiveOperationException {
        columnConfig = (columnConfig == null) ? new TreeNode() : columnConfig;

        // try use setting create it
        SeedFactory seedFactory = this.createSeedFactory(columnConfig);
        SeedConfig seedConfig = null;
        if (seedFactory != null) {
            seedConfig = this.createSeedConfig(seedFactory, columnConfig);
        }

        // use jdbcColumn create it
        TypeProcessor typeProcessor = null;
        if (seedConfig == null) {
            try {
                typeProcessor = this.typeDialect.createSeedFactory(jdbcColumn, columnConfig);
            } catch (UnsupportedOperationException e) {
                logger.error(e.getMessage());
                return null;
            }
        } else {
            typeProcessor = new TypeProcessor(seedFactory, seedConfig, jdbcColumn.getJdbcType());
        }

        // final apply form strategy
        if (Boolean.TRUE.equals(jdbcColumn.getNullable())) {
            SeedConfig config = typeProcessor.getSeedConfig();
            config.setAllowNullable(true);
            if (config.getNullableRatio() == null) {
                config.setNullableRatio(20f);
            }
        }

        // final apply form config
        Class<?> configClass = typeProcessor.getConfigType();
        List<String> properties = BeanUtils.getProperties(configClass);
        Map<String, Class<?>> propertiesMap = BeanUtils.getPropertyType(configClass);

        String[] configProperties = columnConfig.getSubKeys();
        for (String property : configProperties) {
            Object[] propertyValue = columnConfig.getSubValues(property);
            if (propertyValue == null || propertyValue.length == 0) {
                continue;
            }

            Object writeValue = (propertyValue.length == 1) ? propertyValue[0] : propertyValue;
            typeProcessor.putConfig(property, writeValue);

            Class<?> propertyType = propertiesMap.get(property);
            if (propertyType != null && propertyType.isArray()) {
                writeValue = propertyValue;
            }

            if (properties.contains(property)) {
                typeProcessor.writeProperty(property, writeValue);
            }
        }

        Set<UseFor> ignoreAct = new HashSet<>(typeProcessor.getDefaultIgnoreAct());
        ignoreAct.addAll(ignoreSet.contains(jdbcColumn.getColumnName()) ? Arrays.asList(UseFor.values()) : Collections.emptySet());
        ignoreAct.addAll(ignoreInsertSet.contains(jdbcColumn.getColumnName()) ? Collections.singletonList(UseFor.Insert) : Collections.emptySet());
        ignoreAct.addAll(ignoreUpdateSet.contains(jdbcColumn.getColumnName()) ? Collections.singletonList(UseFor.UpdateSet) : Collections.emptySet());
        ignoreAct.addAll(ignoreWhereSet.contains(jdbcColumn.getColumnName()) ? Arrays.asList(UseFor.UpdateWhere, UseFor.DeleteWhere) : Collections.emptySet());

        if (Boolean.TRUE.equals(jdbcColumn.getGeneratedColumn())) {
            ignoreAct.add(UseFor.Insert);
        }

        return new FakerColumn(fakerTable, jdbcColumn, typeProcessor, ignoreAct, this, columnConfig);
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

    private SeedFactory createSeedFactory(SettingNode columnConfig) throws ReflectiveOperationException {
        String seedFactoryStr = columnConfig == null ? null : columnConfig.getSubValue(FakerConfigEnum.COLUMN_SEED_FACTORY.getConfigKey());
        if (StringUtils.isNotBlank(seedFactoryStr)) {
            Class<?> seedFactoryType = this.fakerConfig.getClassLoader().loadClass(seedFactoryStr);
            return (SeedFactory) seedFactoryType.newInstance();
        }

        String array = columnConfig == null ? null : columnConfig.getSubValue(FakerConfigEnum.COLUMN_ARRAY_TYPE.getConfigKey());
        String seedTypeStr = columnConfig == null ? null : columnConfig.getSubValue(FakerConfigEnum.COLUMN_SEED_TYPE.getConfigKey());
        SeedType seedType = SeedType.valueOfCode(seedTypeStr);
        boolean isArray = StringUtils.isNotBlank(array) && Boolean.parseBoolean(array);

        if (seedType == SeedType.Custom) {
            throw new IllegalArgumentException("custom seedType must config seedFactory.");
        } else if (seedType == SeedType.Array) {
            throw new IllegalArgumentException("arrays are specified by config.");
        }

        SeedFactory<? extends SeedConfig> factory = seedType != null ? seedType.newFactory() : null;
        if (isArray) {
            return factory == null ? null : new ArraySeedFactory(factory);
        } else {
            return factory;
        }
    }
}