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
package net.hasor.dbvisitor.lambda;
import net.hasor.cobble.BeanUtils;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.function.Property;
import net.hasor.dbvisitor.JdbcUtils;
import net.hasor.dbvisitor.dialect.DefaultSqlDialect;
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.dialect.SqlDialectRegister;
import net.hasor.dbvisitor.jdbc.ConnectionCallback;
import net.hasor.dbvisitor.jdbc.DynamicConnection;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.keyholder.CreateContext;
import net.hasor.dbvisitor.keyholder.KeySeqHolder;
import net.hasor.dbvisitor.lambda.core.BasicLambda;
import net.hasor.dbvisitor.lambda.support.entity.DeleteLambdaForEntity;
import net.hasor.dbvisitor.lambda.support.entity.InsertLambdaForEntity;
import net.hasor.dbvisitor.lambda.support.entity.SelectLambdaForEntity;
import net.hasor.dbvisitor.lambda.support.entity.UpdateLambdaForEntity;
import net.hasor.dbvisitor.lambda.support.map.DeleteLambdaForMap;
import net.hasor.dbvisitor.lambda.support.map.InsertLambdaForMap;
import net.hasor.dbvisitor.lambda.support.map.SelectLambdaForMap;
import net.hasor.dbvisitor.lambda.support.map.UpdateLambdaForMap;
import net.hasor.dbvisitor.mapping.KeyTypeEnum;
import net.hasor.dbvisitor.mapping.def.ColumnDef;
import net.hasor.dbvisitor.mapping.def.TableDef;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.mapping.resolve.ClassTableMappingResolve;
import net.hasor.dbvisitor.mapping.resolve.MappingOptions;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.function.Function;

/**
 * 继承自 JdbcTemplate 并提供 lambda 方式生成 SQL。
 * @version : 2022-04-02
 * @author 赵永春 (zyc@hasor.net)
 */
public class LambdaTemplate extends JdbcTemplate implements LambdaOperations {
    protected final Map<Class<?>, TableMapping<?>> entMapping = new HashMap<>();
    protected final Map<String, TableMapping<?>>   mapMapping = new HashMap<>();
    protected       SqlDialect                     dialect    = null;
    protected       boolean                        useQualifier;

    /**
     * Construct a new JdbcTemplate for bean usage.
     * <p>Note: The DataSource has to be set before using the instance.
     * @see #setDataSource
     */
    public LambdaTemplate() {
        super();
        this.init();
    }

    /**
     * Construct a new JdbcTemplate, given a DataSource to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param dataSource the JDBC DataSource to obtain connections from
     */
    public LambdaTemplate(final DataSource dataSource) {
        super(dataSource);
        this.init();
    }

    /**
     * Construct a new JdbcTemplate, given a DataSource to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param dataSource the JDBC DataSource to obtain connections from
     * @param typeRegistry the TypeHandlerRegistry
     */
    public LambdaTemplate(final DataSource dataSource, TypeHandlerRegistry typeRegistry) {
        super(dataSource, typeRegistry);
        this.init();
    }

    /**
     * Construct a new JdbcTemplate, given a Connection to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param conn the JDBC Connection
     */
    public LambdaTemplate(final Connection conn) {
        super(conn);
        this.init();
    }

    /**
     * Construct a new JdbcTemplate, given a Connection to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param conn the JDBC Connection
     * @param typeRegistry the TypeHandlerRegistry
     */
    public LambdaTemplate(final Connection conn, TypeHandlerRegistry typeRegistry) {
        super(conn, typeRegistry);
    }

    /**
     * Construct a new JdbcTemplate, given a Connection to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param dynamicConn the JDBC Connection of dynamic
     */
    public LambdaTemplate(final DynamicConnection dynamicConn) {
        super(dynamicConn);
        this.init();
    }

    /**
     * Construct a new JdbcTemplate, given a Connection to obtain connections from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param dynamicConn the JDBC Connection of dynamic
     * @param typeRegistry the TypeHandlerRegistry
     */
    public LambdaTemplate(final DynamicConnection dynamicConn, TypeHandlerRegistry typeRegistry) {
        super(dynamicConn, typeRegistry);
    }

    /**
     * Construct a new JdbcTemplate, given a Connection to obtain JdbcTemplate from.
     * <p>Note: This will not trigger initialization of the exception translator.
     * @param jdbcTemplate the JdbcTemplate
     */
    public LambdaTemplate(JdbcTemplate jdbcTemplate) {
        super();
        this.setDataSource(jdbcTemplate.getDataSource());
        this.setConnection(jdbcTemplate.getConnection());
        this.setDynamic(jdbcTemplate.getDynamic());

        this.setResultsCaseInsensitive(jdbcTemplate.isResultsCaseInsensitive());
        this.setTypeRegistry(jdbcTemplate.getTypeRegistry());
        this.setFetchSize(jdbcTemplate.getFetchSize());
        this.setMaxRows(jdbcTemplate.getMaxRows());
        this.setQueryTimeout(jdbcTemplate.getQueryTimeout());
        this.setIgnoreWarnings(jdbcTemplate.isIgnoreWarnings());
        this.setPrintStmtError(jdbcTemplate.isPrintStmtError());
    }

    protected void init() {
        this.dialect = fetchDialect();
    }

    protected SqlDialect fetchDialect() {
        if (getConnection() == null && getDynamic() == null && getDataSource() == null) {
            return DefaultSqlDialect.DEFAULT;
        }

        String tmpDbType = "";
        try {
            tmpDbType = this.execute((ConnectionCallback<String>) con -> {
                DatabaseMetaData metaData = con.getMetaData();
                return JdbcUtils.getDbType(metaData.getURL(), metaData.getDriverName());
            });
        } catch (Exception e) {
            tmpDbType = "";
        }

        return SqlDialectRegister.findOrCreate(tmpDbType);
    }

    public SqlDialect getDialect() {
        return this.dialect;
    }

    public void setDialect(SqlDialect dialect) {
        this.dialect = dialect;
    }

    public boolean isUseQualifier() {
        return this.useQualifier;
    }

    public void setUseQualifier(boolean useQualifier) {
        this.useQualifier = useQualifier;
    }

    private List<String> fetchPrimaryKeys(Connection con, TableDef<?> tableDef) throws SQLException {
        try (ResultSet primaryKeys = con.getMetaData().getPrimaryKeys(tableDef.getCatalog(), tableDef.getSchema(), tableDef.getTable())) {
            List<String> keys = new ArrayList<>();
            while (primaryKeys.next()) {
                keys.add(primaryKeys.getString("COLUMN_NAME"));
            }
            return keys;
        }
    }

    private List<String> fetchUniqueKeys(Connection con, TableDef<?> tableDef) throws SQLException {
        try (ResultSet indexInfo = con.getMetaData().getIndexInfo(tableDef.getCatalog(), tableDef.getSchema(), tableDef.getTable(), false, false)) {
            List<String> keys = new ArrayList<>();
            while (indexInfo.next()) {
                boolean nonUnique = indexInfo.getBoolean("NON_UNIQUE");
                if (!nonUnique) {
                    keys.add(indexInfo.getString("COLUMN_NAME"));
                }
            }
            return keys;
        }
    }

    protected List<ColumnDef> fetchColumns(Connection con, TableDef<?> tableDef, MappingOptions options, Function<String, String> fmtName) throws SQLException {
        List<String> primaryKey = fetchPrimaryKeys(con, tableDef);
        //List<String> uniqueKey = fetchUniqueKeys(con, schema, table);
        TypeHandlerRegistry typeRegistry = getTypeRegistry();

        try (ResultSet rs = con.getMetaData().getColumns(tableDef.getCatalog(), tableDef.getSchema(), tableDef.getTable(), null)) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            List<String> colNames = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                colNames.add(metaData.getColumnName(i));
            }

            List<ColumnDef> result = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> confMap = new HashMap<>();
                for (String confName : colNames) {
                    confMap.put(confName, rs.getString(confName));
                }

                String columnName = rs.getString("COLUMN_NAME");
                String propertyName = lineToHump(columnName, options.getMapUnderscoreToCamelCase());
                Integer jdbcType = rs.getInt("DATA_TYPE");
                if (rs.wasNull()) {
                    jdbcType = null;
                }
                boolean isAuto = StringUtils.equalsIgnoreCase("YES", rs.getString("IS_AUTOINCREMENT"));
                boolean isVirtual = StringUtils.equalsIgnoreCase("YES", rs.getString("IS_GENERATEDCOLUMN"));
                boolean isPrimary = primaryKey.contains(columnName);

                TypeHandler<?> typeHandler = (jdbcType == null) ? typeRegistry.getDefaultTypeHandler() : typeRegistry.getTypeHandler(jdbcType);
                Property mapHandler = BeanUtils.createMapPropertyFunc(propertyName);

                ColumnDef colDef = new ColumnDef(columnName, propertyName, jdbcType, Object.class, typeHandler, mapHandler, !isVirtual, !isVirtual, isPrimary);

                // init KeySeqHolder
                if (isAuto) {
                    KeySeqHolder sequenceHolder = KeyTypeEnum.Auto.createHolder(new CreateContext(options, this.getTypeRegistry(), tableDef, colDef, confMap));
                    colDef.setKeySeqHolder(sequenceHolder);
                }
                result.add(colDef);
            }
            return result;
        }
    }

    private String lineToHump(String str, Boolean mapUnderscoreToCamelCase) {
        if (StringUtils.isBlank(str) || mapUnderscoreToCamelCase == null || !mapUnderscoreToCamelCase) {
            return str;
        } else {
            return StringUtils.lineToHump(str);
        }
    }

    protected <T> TableMapping<T> getTableMapping(Class<T> exampleType, MappingOptions options) {
        if (exampleType == null) {
            throw new NullPointerException("exampleType is null.");
        }
        if (exampleType == Map.class) {
            throw new UnsupportedOperationException("Map cannot be used as lambda exampleType.");
        }

        TableMapping<?> mapping = this.entMapping.get(exampleType);
        if (mapping != null) {
            if (exampleType == mapping.entityType() || exampleType.isAssignableFrom(mapping.entityType())) {
                return (TableMapping<T>) mapping;
            } else {
                throw new ClassCastException("exampleType is incompatible with TableMapping.");
            }
        }

        mapping = this.entMapping.computeIfAbsent(exampleType, key -> {
            MappingOptions opt = new MappingOptions(options);
            opt.setCaseInsensitive(this.isResultsCaseInsensitive());
            if (this.getDialect() != null) {
                opt.setDefaultDialect(this.getDialect());
            }

            return new ClassTableMappingResolve(opt).resolveTableMapping(exampleType, exampleType.getClassLoader(), this.getTypeRegistry());
        });
        return (TableMapping<T>) mapping;
    }

    protected TableMapping<?> getTableMapping(final String catalog, final String schema, final String table, MappingOptions opt) throws SQLException {
        if (StringUtils.isBlank(table)) {
            throw new NullPointerException("table is blank.");
        }

        String mappingKey = String.format("'%s'.'%s'.'%s'", catalog, schema, table); // mapping is map key
        TableMapping<?> mapping = this.mapMapping.get(mappingKey);
        if (mapping != null) {
            return mapping;
        }

        MappingOptions copyOpt = MappingOptions.buildNew(opt);
        copyOpt.setCaseInsensitive(this.isResultsCaseInsensitive());
        copyOpt.setUseDelimited(Boolean.TRUE.equals(copyOpt.getUseDelimited()));
        copyOpt.setDefaultDialect(this.getDialect());

        Function<String, String> fmtNameFoo = fmtNameFoo(copyOpt);

        final String finalCatalog = StringUtils.isBlank(catalog) ? null : fmtNameFoo.apply(catalog);
        final String finalSchema = StringUtils.isBlank(schema) ? null : fmtNameFoo.apply(schema);
        final String finalTable = StringUtils.isBlank(table) ? null : fmtNameFoo.apply(table);
        boolean useDelimited = copyOpt.getUseDelimited();
        boolean caseInsensitive = copyOpt.getCaseInsensitive();

        final TableDef<?> defMap = new TableDef<>(finalCatalog, finalSchema, finalTable, LinkedHashMap.class, //
                true, useDelimited, caseInsensitive, copyOpt.getDefaultDialect(), getTypeRegistry());
        List<ColumnDef> columnDefs = execute((ConnectionCallback<List<ColumnDef>>) con -> {
            return fetchColumns(con, defMap, copyOpt, fmtNameFoo);
        });

        for (ColumnDef cDef : columnDefs) {
            defMap.addMapping(cDef);
        }

        this.mapMapping.put(mappingKey, defMap);
        return defMap;
    }

    protected <E extends BasicLambda<R, T, P>, R, T, P> E configLambda(E execute) {
        if (this.useQualifier) {
            execute.useQualifier();
        }
        return execute;
    }

    @Override
    public <T> InsertOperation<T> lambdaInsert(Class<T> exampleType, MappingOptions options) {
        return configLambda(new InsertLambdaForEntity<>(exampleType, getTableMapping(exampleType, options), this));
    }

    @Override
    public InsertOperation<Map<String, Object>> lambdaInsert(String catalog, String schema, String table, MappingOptions options) throws SQLException {
        return configLambda(new InsertLambdaForMap(getTableMapping(catalog, schema, table, options), this));
    }

    @Override
    public <T> EntityUpdateOperation<T> lambdaUpdate(Class<T> exampleType, MappingOptions options) {
        return configLambda(new UpdateLambdaForEntity<>(exampleType, getTableMapping(exampleType, options), this));
    }

    @Override
    public MapUpdateOperation lambdaUpdate(String catalog, String schema, String table, MappingOptions options) throws SQLException {
        return configLambda(new UpdateLambdaForMap(getTableMapping(catalog, schema, table, options), this));
    }

    @Override
    public <T> EntityDeleteOperation<T> lambdaDelete(Class<T> exampleType, MappingOptions options) {
        return configLambda(new DeleteLambdaForEntity<>(exampleType, getTableMapping(exampleType, options), this));
    }

    @Override
    public MapDeleteOperation lambdaDelete(String catalog, String schema, String table, MappingOptions options) throws SQLException {
        return configLambda(new DeleteLambdaForMap(getTableMapping(catalog, schema, table, options), this));
    }

    @Override
    public <T> EntityQueryOperation<T> lambdaQuery(Class<T> exampleType, MappingOptions options) {
        return configLambda(new SelectLambdaForEntity<>(exampleType, getTableMapping(exampleType, options), this));
    }

    @Override
    public MapQueryOperation lambdaQuery(String catalog, String schema, String table, MappingOptions options) throws SQLException {
        return configLambda(new SelectLambdaForMap(getTableMapping(catalog, schema, table, options), this));
    }

    public void resetMapping() {
        this.entMapping.clear();
        this.mapMapping.clear();
    }

    public void resetMapping(String catalog, String schema, String table) {
        if (StringUtils.isBlank(table)) {
            throw new NullPointerException("table is blank.");
        }
        this.mapMapping.remove(String.format("'%s'.'%s'.'%s'", catalog, schema, table));
    }

    public void resetMapping(Class<?> exampleType) {
        this.entMapping.remove(exampleType);
    }

    private Function<String, String> fmtNameFoo(MappingOptions options) throws SQLException {
        if (!options.getCaseInsensitive()) {
            return s -> s;
        }
        return execute((ConnectionCallback<Function<String, String>>) con -> {
            DatabaseMetaData metaData = con.getMetaData();
            if (options.getUseDelimited()) {
                if (metaData.storesUpperCaseQuotedIdentifiers()) {
                    return (Function<String, String>) String::toUpperCase;
                } else if (metaData.storesLowerCaseQuotedIdentifiers()) {
                    return (Function<String, String>) String::toLowerCase;
                }
            } else {
                if (metaData.storesUpperCaseIdentifiers()) {
                    return (Function<String, String>) String::toUpperCase;
                } else if (metaData.storesLowerCaseIdentifiers()) {
                    return (Function<String, String>) String::toLowerCase;
                }
            }
            return (Function<String, String>) s -> s;
        });
    }
}