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
import net.hasor.dbvisitor.faker.generator.provider.DefaultTypeSrwFactory;
import net.hasor.dbvisitor.faker.generator.provider.carefully.MySqlCarefullyTypeSrwFactory;
import net.hasor.dbvisitor.faker.generator.provider.carefully.OracleCarefullyTypeSrwFactory;
import net.hasor.dbvisitor.faker.generator.provider.carefully.PostgresCarefullyTypeSrwFactory;
import net.hasor.dbvisitor.faker.generator.provider.carefully.SqlServerCarefullyTypeSrwFactory;
import net.hasor.dbvisitor.faker.generator.provider.radical.MySqlRadicalTypeSrwFactory;
import net.hasor.dbvisitor.faker.generator.provider.radical.OracleRadicalTypeSrwFactory;
import net.hasor.dbvisitor.faker.generator.provider.radical.PostgresRadicalTypeSrwFactory;
import net.hasor.dbvisitor.faker.generator.provider.radical.SqlServerRadicalTypeSrwFactory;
import net.hasor.dbvisitor.faker.meta.JdbcColumn;
import net.hasor.dbvisitor.faker.meta.JdbcFetchMetaProvider;
import net.hasor.dbvisitor.faker.meta.JdbcTable;
import net.hasor.dbvisitor.faker.seed.SeedConfig;
import net.hasor.dbvisitor.faker.seed.SeedFactory;
import net.hasor.dbvisitor.faker.seed.SeedType;
import net.hasor.dbvisitor.jdbc.ConnectionCallback;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * FakerTable 构建器
 * @version : 2022-07-25
 * @author 赵永春 (zyc@hasor.net)
 */
public class FakerFactory {
    private static final Logger                logger = Logger.getLogger(FakerFactory.class);
    private final        JdbcTemplate          jdbcTemplate;
    private final        JdbcFetchMetaProvider metaProvider;
    private final        FakerConfig           fakerConfig;
    private final        String                dbType;
    private final        SqlDialect            sqlDialect;
    private final        DefaultTypeSrwFactory typeDialect;

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
        this.dbType = initDbType();
        this.sqlDialect = this.initSqlDialect(this.dbType, fakerConfig);
        this.typeDialect = this.initTypeDialect(this.dbType, fakerConfig);
    }

    public FakerFactory(DataSource dataSource, FakerConfig fakerConfig) throws SQLException {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.metaProvider = new JdbcFetchMetaProvider(dataSource);
        this.fakerConfig = fakerConfig;
        this.dbType = initDbType();
        this.sqlDialect = this.initSqlDialect(this.dbType, fakerConfig);
        this.typeDialect = this.initTypeDialect(this.dbType, fakerConfig);
    }

    protected String initDbType() throws SQLException {
        return this.jdbcTemplate.execute((ConnectionCallback<String>) con -> {
            String jdbcUrl = con.getMetaData().getURL();
            String jdbcDriverName = con.getMetaData().getDriverName();
            return JdbcUtils.getDbType(jdbcUrl, jdbcDriverName);
        });
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

    protected DefaultTypeSrwFactory initTypeDialect(String dbType, FakerConfig fakerConfig) {
        if (fakerConfig.getTypeDialect() != null) {
            return fakerConfig.getTypeDialect();
        }
        if (StringUtils.isBlank(dbType)) {
            throw new IllegalArgumentException("TypeDialect missing.");
        } else {
            switch (dbType) {
                case JdbcUtils.JTDS:
                case JdbcUtils.SQL_SERVER:
                    return fakerConfig.isUseRadical() ? new SqlServerRadicalTypeSrwFactory() : new SqlServerCarefullyTypeSrwFactory();
                case JdbcUtils.MARIADB:
                case JdbcUtils.MYSQL:
                    return fakerConfig.isUseRadical() ? new MySqlRadicalTypeSrwFactory() : new MySqlCarefullyTypeSrwFactory();
                case JdbcUtils.ORACLE:
                    return fakerConfig.isUseRadical() ? new OracleRadicalTypeSrwFactory() : new OracleCarefullyTypeSrwFactory();
                case JdbcUtils.POSTGRESQL:
                    return fakerConfig.isUseRadical() ? new PostgresRadicalTypeSrwFactory() : new PostgresCarefullyTypeSrwFactory();
                default:
                    return new DefaultTypeSrwFactory();
            }
        }
    }

    public FakerConfig getFakerConfig() {
        return this.fakerConfig;
    }

    public JdbcTemplate getJdbcTemplate() {
        return this.jdbcTemplate;
    }

    public SqlDialect getSqlDialect() {
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
        TypeSrw typeSrw = null;
        if (seedConfig == null) {
            try {
                typeSrw = this.typeDialect.createSeedFactory(jdbcColumn, columnConfig);
            } catch (UnsupportedOperationException e) {
                logger.error(e.getMessage());
                return null;
            }
        } else {
            typeSrw = new TypeSrw(seedFactory, seedConfig, jdbcColumn.getJdbcType());
        }

        // final apply form strategy
        if (Boolean.TRUE.equals(jdbcColumn.getNullable())) {
            typeSrw.getSeedConfig().setAllowNullable(true);
            typeSrw.getSeedConfig().setNullableRatio(20f);
        }

        // final apply form config
        Class<?> configClass = typeSrw.getConfigType();
        List<String> properties = BeanUtils.getProperties(configClass);
        Map<String, Class<?>> propertiesMap = BeanUtils.getPropertyType(configClass);

        String[] configProperties = columnConfig.getSubKeys();
        for (String property : configProperties) {
            Object[] propertyValue = columnConfig.getSubValues(property);
            if (propertyValue == null || propertyValue.length == 0) {
                continue;
            }

            Object writeValue = (propertyValue.length == 1) ? propertyValue[0] : propertyValue;
            typeSrw.putConfig(property, writeValue);

            Class<?> propertyType = propertiesMap.get(property);
            if (propertyType != null && propertyType.isArray()) {
                writeValue = propertyValue;
            }

            if (properties.contains(property)) {
                typeSrw.writeProperty(property, writeValue);
            }
        }

        Set<UseFor> ignoreAct = new HashSet<>(typeSrw.getDefaultIgnoreAct());
        ignoreAct.addAll(ignoreSet.contains(jdbcColumn.getColumnName()) ? Arrays.asList(UseFor.values()) : Collections.emptySet());
        ignoreAct.addAll(ignoreInsertSet.contains(jdbcColumn.getColumnName()) ? Collections.singletonList(UseFor.Insert) : Collections.emptySet());
        ignoreAct.addAll(ignoreUpdateSet.contains(jdbcColumn.getColumnName()) ? Collections.singletonList(UseFor.UpdateSet) : Collections.emptySet());
        ignoreAct.addAll(ignoreWhereSet.contains(jdbcColumn.getColumnName()) ? Arrays.asList(UseFor.UpdateWhere, UseFor.DeleteWhere) : Collections.emptySet());

        if (Boolean.TRUE.equals(jdbcColumn.getGeneratedColumn())) {
            ignoreAct.add(UseFor.Insert);
        }

        return new FakerColumn(fakerTable, jdbcColumn, typeSrw, ignoreAct, this, columnConfig);
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

        String seedTypeStr = columnConfig == null ? null : columnConfig.getSubValue(FakerConfigEnum.COLUMN_SEED_TYPE.getConfigKey());
        SeedType seedType = SeedType.valueOfCode(seedTypeStr);
        if (seedType == SeedType.Custom) {
            throw new IllegalArgumentException("custom seedType must config seedFactory.");
        }

        return seedType != null ? seedType.getSupplier() : null;
    }
}