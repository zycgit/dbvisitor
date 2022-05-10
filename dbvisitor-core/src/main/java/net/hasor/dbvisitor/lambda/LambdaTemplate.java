/*
 * Copyright 2002-2005 the original author or authors.
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
import net.hasor.dbvisitor.dialect.SqlDialect;
import net.hasor.dbvisitor.jdbc.ConnectionCallback;
import net.hasor.dbvisitor.jdbc.DynamicConnection;
import net.hasor.dbvisitor.jdbc.core.JdbcTemplate;
import net.hasor.dbvisitor.lambda.core.BasicLambda;
import net.hasor.dbvisitor.lambda.support.entity.DeleteLambdaForEntity;
import net.hasor.dbvisitor.lambda.support.entity.InsertLambdaForEntity;
import net.hasor.dbvisitor.lambda.support.entity.SelectLambdaForEntity;
import net.hasor.dbvisitor.lambda.support.entity.UpdateLambdaForEntity;
import net.hasor.dbvisitor.lambda.support.map.DeleteLambdaForMap;
import net.hasor.dbvisitor.lambda.support.map.InsertLambdaForMap;
import net.hasor.dbvisitor.lambda.support.map.SelectLambdaForMap;
import net.hasor.dbvisitor.lambda.support.map.UpdateLambdaForMap;
import net.hasor.dbvisitor.mapping.def.ColumnDef;
import net.hasor.dbvisitor.mapping.def.TableDef;
import net.hasor.dbvisitor.mapping.def.TableMapping;
import net.hasor.dbvisitor.mapping.resolve.ClassTableMappingResolve;
import net.hasor.dbvisitor.mapping.resolve.MappingOptions;
import net.hasor.dbvisitor.types.TypeHandler;
import net.hasor.dbvisitor.types.TypeHandlerRegistry;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * 继承自 JdbcTemplate 并提供 lambda 方式生成 SQL。
 * @version : 2022-04-02
 * @author 赵永春 (zyc@hasor.net)
 */
public class LambdaTemplate extends JdbcTemplate implements LambdaOperations {
    protected final Map<Class<?>, TableMapping<?>> entMapping = new HashMap<>();
    protected final Map<String, TableMapping<?>>   mapMapping = new HashMap<>();
    protected       SqlDialect                     dialect    = null;

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

    protected void init() {

    }

    public void setDialect(SqlDialect dialect) {
        this.dialect = dialect;
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
            return new ClassTableMappingResolve().resolveTableMapping(exampleType, exampleType.getClassLoader(), this.getTypeRegistry(), opt);
        });
        return (TableMapping<T>) mapping;
    }

    private List<String> fetchPrimaryKeys(Connection con, String schema, String table) throws SQLException {
        try (ResultSet primaryKeys = con.getMetaData().getPrimaryKeys(null, schema, table)) {
            List<String> keys = new ArrayList<>();
            while (primaryKeys.next()) {
                keys.add(primaryKeys.getString("COLUMN_NAME"));
            }
            return keys;
        }
    }

    private List<String> fetchUniqueKeys(Connection con, String schema, String table) throws SQLException {
        try (ResultSet indexInfo = con.getMetaData().getIndexInfo(null, schema, table, false, false)) {
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

    protected List<ColumnDef> fetchColumns(Connection con, String schema, String table, MappingOptions options) throws SQLException {
        if (StringUtils.isBlank(schema)) {
            schema = null;
        }

        List<String> primaryKey = fetchPrimaryKeys(con, schema, table);
        List<String> uniqueKey = fetchUniqueKeys(con, schema, table);
        TypeHandlerRegistry typeRegistry = getTypeRegistry();

        try (ResultSet columns = con.getMetaData().getColumns(null, schema, table, null)) {
            List<ColumnDef> result = new ArrayList<>();
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                String propertyName = lineToHump(columnName, options.getMapUnderscoreToCamelCase());
                Integer jdbcType = columns.getInt("DATA_TYPE");
                if (columns.wasNull()) {
                    jdbcType = null;
                }
                boolean generated = StringUtils.equalsIgnoreCase("YES", columns.getString("IS_GENERATEDCOLUMN"));
                boolean primary = primaryKey.contains(columnName);

                TypeHandler<?> typeHandler = (jdbcType == null) ? typeRegistry.getDefaultTypeHandler() : typeRegistry.getTypeHandler(jdbcType);
                Property mapHandler = BeanUtils.createMapPropertyFunc(propertyName);

                result.add(new ColumnDef(columnName, propertyName, jdbcType, Object.class, typeHandler, mapHandler, !generated, !generated, primary));
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

    protected TableMapping<?> getTableMapping(final String schema, final String table, MappingOptions options) throws SQLException {
        if (StringUtils.isBlank(table)) {
            throw new NullPointerException("table is blank.");
        }

        String mappingName = String.format("'%s'.'%s'", schema, table);
        TableMapping<?> mapping = this.mapMapping.get(mappingName);
        if (mapping != null) {
            return mapping;
        }

        MappingOptions opt = new MappingOptions(options);
        opt.setCaseInsensitive(this.isResultsCaseInsensitive());
        boolean caseInsensitive = opt.getCaseInsensitive() == null || Boolean.TRUE.equals(opt.getCaseInsensitive());

        final TableDef<?> defMap = new TableDef<>(schema, table, LinkedHashMap.class, true, true, caseInsensitive, getTypeRegistry());
        List<ColumnDef> columnDefs = execute((ConnectionCallback<List<ColumnDef>>) con -> {
            return fetchColumns(con, schema, table, opt);
        });

        for (ColumnDef cDef : columnDefs) {
            defMap.addMapping(cDef);
        }

        this.mapMapping.put(mappingName, defMap);
        return defMap;
    }

    protected SqlDialect getDefaultDialect() {
        return this.dialect;
    }

    private <E extends BasicLambda<R, T, P>, R, T, P> E configDialect(E execute) {
        SqlDialect dialect = getDefaultDialect();
        if (dialect != null) {
            execute.setDialect(dialect);
        }
        return execute;
    }

    @Override
    public <T> InsertOperation<T> lambdaInsert(Class<T> exampleType, MappingOptions options) {
        return configDialect(new InsertLambdaForEntity<>(exampleType, getTableMapping(exampleType, options), this));
    }

    @Override
    public InsertOperation<Map<String, Object>> lambdaInsert(String schema, String table, MappingOptions options) throws SQLException {
        return configDialect(new InsertLambdaForMap(getTableMapping(schema, table, options), this));
    }

    @Override
    public <T> EntityUpdateOperation<T> lambdaUpdate(Class<T> exampleType, MappingOptions options) {
        return configDialect(new UpdateLambdaForEntity<>(exampleType, getTableMapping(exampleType, options), this));
    }

    @Override
    public MapUpdateOperation lambdaUpdate(String schema, String table, MappingOptions options) throws SQLException {
        return configDialect(new UpdateLambdaForMap(getTableMapping(schema, table, options), this));
    }

    @Override
    public <T> EntityDeleteOperation<T> lambdaDelete(Class<T> exampleType, MappingOptions options) {
        return configDialect(new DeleteLambdaForEntity<>(exampleType, getTableMapping(exampleType, options), this));
    }

    @Override
    public MapDeleteOperation lambdaDelete(String schema, String table, MappingOptions options) throws SQLException {
        return configDialect(new DeleteLambdaForMap(getTableMapping(schema, table, options), this));
    }

    @Override
    public <T> EntityQueryOperation<T> lambdaQuery(Class<T> exampleType, MappingOptions options) {
        return configDialect(new SelectLambdaForEntity<>(exampleType, getTableMapping(exampleType, options), this));
    }

    @Override
    public MapQueryOperation lambdaQuery(String schema, String table, MappingOptions options) throws SQLException {
        return configDialect(new SelectLambdaForMap(getTableMapping(schema, table, options), this));
    }

}
