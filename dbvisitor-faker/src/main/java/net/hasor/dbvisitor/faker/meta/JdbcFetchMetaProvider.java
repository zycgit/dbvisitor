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
package net.hasor.dbvisitor.faker.meta;
import net.hasor.cobble.StringUtils;
import net.hasor.cobble.function.ESupplier;
import net.hasor.dbvisitor.jdbc.extractor.ColumnMapResultSetExtractor;
import net.hasor.dbvisitor.jdbc.extractor.RowMapperResultSetExtractor;
import net.hasor.dbvisitor.jdbc.mapper.ColumnMapRowMapper;

import javax.sql.DataSource;
import java.io.Closeable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 基于 JDBC 接口的元信息获取
 * @version : 2020-04-23
 * @author 赵永春 (zyc@hasor.net)
 */
public class JdbcFetchMetaProvider {
    private final ESupplier<Connection, SQLException> connect;
    private final JdbcFetchMeta                       jdbcFetchMeta;

    public JdbcFetchMetaProvider(Connection connection, JdbcFetchMeta jdbcFetchMeta) {
        Connection conn = newProxyConnection(connection, null);
        this.connect = () -> conn;
        this.jdbcFetchMeta = jdbcFetchMeta;
    }

    public JdbcFetchMetaProvider(DataSource dataSource, JdbcFetchMeta jdbcFetchMeta) {
        this.connect = dataSource::getConnection;
        this.jdbcFetchMeta = jdbcFetchMeta;
    }

    public String getVersion() throws SQLException {
        try (Connection conn = this.connect.eGet()) {
            DatabaseMetaData metaData = conn.getMetaData();
            String productName = metaData.getDatabaseProductName();
            String productVersion = metaData.getDatabaseProductVersion();
            int productMajorVersion = metaData.getDatabaseMajorVersion();
            int productMinorVersion = metaData.getDatabaseMinorVersion();
            String dbmsVer = String.format("DBMS: %s (ver. %s, v%s.%s)", productName, productVersion, productMajorVersion, productMinorVersion);

            String driverName = metaData.getDriverName();
            String driverVersion = metaData.getDriverVersion();
            int driverMajorVersion = metaData.getDriverMajorVersion();
            int driverMinorVersion = metaData.getDriverMinorVersion();
            String driverVer = String.format("Driver: %s (ver. %s, v%s.%s)", driverName, driverVersion, driverMajorVersion, driverMinorVersion);

            int jdbcMajorVersion = metaData.getJDBCMajorVersion();
            int jdbcMinorVersion = metaData.getJDBCMinorVersion();
            String jdbcVer = String.format("JDBC (v%s.%s)", jdbcMajorVersion, jdbcMinorVersion);

            return dbmsVer + "\n" + driverVer + "\n" + jdbcVer;
        }
    }

    /**
     * @param catalog a catalog name; must match the catalog name as it
     *        is stored in the database; "" retrieves those without a catalog;
     *        <code>null</code> means that the catalog name should not be used to narrow the search
     * @param schemaName a schema name; must match the schema name as it is stored in the database;
     *        "" retrieves those without a schema;
     *        <code>null</code> means that the schema name should not be used to narrow the search
     * @param table a table name; must match the table name as it is stored in the database
     */
    public JdbcTable getTable(String catalog, String schemaName, String table) throws SQLException {
        try (Connection conn = this.connect.eGet()) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet resultSet = metaData.getTables(catalog, schemaName, table, null)) {
                final ColumnMapRowMapper rowMapper = new ColumnMapRowMapper();
                List<JdbcTable> jdbcTables = new RowMapperResultSetExtractor<>((rs, rowNum) -> {
                    return convertTable(rowMapper.mapRow(rs, rowNum));
                }).extractData(resultSet);
                if (jdbcTables.isEmpty()) {
                    return null;
                } else {
                    return jdbcTables.get(0);
                }
            }
        }
    }

    /**
     * @param catalog a catalog name; must match the catalog name as it
     *        is stored in the database; "" retrieves those without a catalog;
     *        <code>null</code> means that the catalog name should not be used to narrow the search
     * @param schemaName a schema name; must match the schema name as it is stored in the database;
     *        "" retrieves those without a schema;
     *        <code>null</code> means that the schema name should not be used to narrow the search
     * @param table a table name; must match the table name as it is stored in the database
     */
    public List<JdbcColumn> getColumns(String catalog, String schemaName, String table) throws SQLException {
        if (StringUtils.isBlank(table)) {
            return Collections.emptyList();
        }

        List<JdbcColumn> jdbcColumns = null;
        if (this.jdbcFetchMeta != null) {
            try (Connection conn = this.connect.eGet()) {
                jdbcColumns = this.jdbcFetchMeta.getColumns(conn, catalog, schemaName, table);
            }
        } else {
            JdbcPrimaryKey primaryKey = getPrimaryKey(catalog, schemaName, table);
            List<JdbcIndex> uniqueKey = getUniqueKey(catalog, schemaName, table);
            Set<String> uniqueColumns = uniqueKey.stream().flatMap((Function<JdbcIndex, Stream<String>>) jdbcIndex -> {
                return jdbcIndex.getColumns().stream();
            }).collect(Collectors.toSet());

            try (Connection conn = this.connect.eGet()) {
                DatabaseMetaData metaData = conn.getMetaData();
                try (ResultSet resultSet = metaData.getColumns(catalog, schemaName, table, null)) {
                    final ColumnMapRowMapper rowMapper = new ColumnMapRowMapper();
                    jdbcColumns = new RowMapperResultSetExtractor<>((rs, rowNum) -> {
                        return convertColumn(rowMapper.mapRow(rs, rowNum), primaryKey, uniqueColumns);
                    }).extractData(resultSet);
                }
            }
        }

        return jdbcColumns == null ? Collections.emptyList() : jdbcColumns;
    }

    /**
     * @param catalog a catalog name; must match the catalog name as it is stored in the database; "" retrieves those without a catalog;
     *        <code>null</code> means that the catalog name should not be used to narrow the search
     * @param schemaName a schema name; must match the schema name as it is stored in the database; "" retrieves those without a schema;
     *        <code>null</code> means that the schema name should not be used to narrow the search
     * @param table a table name; must match the table name as it is stored in the database
     */
    public JdbcPrimaryKey getPrimaryKey(String catalog, String schemaName, String table) throws SQLException {
        if (StringUtils.isBlank(table)) {
            return null;
        }
        try (Connection conn = this.connect.eGet()) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet primaryKeys = metaData.getPrimaryKeys(catalog, schemaName, table)) {
                List<Map<String, Object>> mapList = new ColumnMapResultSetExtractor().extractData(primaryKeys);
                if (mapList == null || mapList.isEmpty()) {
                    return null;
                }
                //
                Map<String, Optional<JdbcPrimaryKey>> pkMap = mapList.stream().sorted((o1, o2) -> {
                    Integer o1KeySeq = safeToInteger(o1.get("KEY_SEQ"));
                    Integer o2KeySeq = safeToInteger(o2.get("KEY_SEQ"));
                    if (o1KeySeq != null && o2KeySeq != null) {
                        return Integer.compare(o1KeySeq, o2KeySeq);
                    } else {
                        return 0;
                    }
                }).map(this::convertPrimaryKey).collect(Collectors.groupingBy(o -> {
                    // group by (schema + name)
                    return o.getSchema() + "," + o.getName();
                }, Collectors.reducing((pk1, pk2) -> {
                    // reducing group by data in to one.
                    pk1.getColumns().addAll(pk2.getColumns());
                    return pk1;
                })));
                if (pkMap.size() > 1) {
                    throw new SQLException("Data error encountered multiple primary keys '" + StringUtils.join(pkMap.keySet().toArray(), "','") + "'");
                }
                //
                Optional<JdbcPrimaryKey> primaryKeyOptional = pkMap.values().stream().findFirst().orElse(Optional.empty());
                return primaryKeyOptional.orElse(null);
            }
        }
    }

    /**
     * @param catalog a catalog name; must match the catalog name as it
     *        is stored in the database; "" retrieves those without a catalog;
     *        <code>null</code> means that the catalog name should not be used to narrow the search
     * @param schemaName a schema name; must match the schema name as it is stored in the database;
     *        "" retrieves those without a schema;
     *        <code>null</code> means that the schema name should not be used to narrow the search
     * @param table a table name; must match the table name as it is stored in the database
     */
    public List<JdbcIndex> getIndexes(String catalog, String schemaName, String table) throws SQLException {
        if (StringUtils.isBlank(table)) {
            return Collections.emptyList();
        }
        try (Connection conn = this.connect.eGet()) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet indexInfo = metaData.getIndexInfo(catalog, schemaName, table, false, false)) {
                List<Map<String, Object>> mapList = new ColumnMapResultSetExtractor().extractData(indexInfo);
                if (mapList == null || mapList.isEmpty()) {
                    return Collections.emptyList();
                }
                //
                return mapList.stream().filter(recordMap -> {
                    // Oracle 数据库使用 JDBC，可能出现一个 null 名字的索引。
                    return StringUtils.isNotBlank(safeToString(recordMap.get("INDEX_NAME")));
                }).sorted((o1, o2) -> {
                    // sort by ORDINAL_POSITION
                    Integer o1Index = safeToInteger(o1.get("ORDINAL_POSITION"));
                    Integer o2Index = safeToInteger(o2.get("ORDINAL_POSITION"));
                    if (o1Index != null && o2Index != null) {
                        return Integer.compare(o1Index, o2Index);
                    }
                    return 0;
                }).map(this::convertIndex).collect(Collectors.groupingBy(o -> {
                    // group by (tableName + indexName)
                    return o.getTableName() + "," + o.getName();
                }, Collectors.reducing((idx1, idx2) -> {
                    // reducing group by data in to one.
                    idx1.getColumns().addAll(idx2.getColumns());
                    idx1.getStorageType().putAll(idx2.getStorageType());
                    return idx1;
                }))).values().stream().map(o -> {
                    return o.orElse(null);
                }).filter(Objects::nonNull).collect(Collectors.toList());
            }
        }
    }

    /**
     * @param catalog a catalog name; must match the catalog name as it
     *        is stored in the database; "" retrieves those without a catalog;
     *        <code>null</code> means that the catalog name should not be used to narrow the search
     * @param schemaName a schema name; must match the schema name as it is stored in the database;
     *        "" retrieves those without a schema;
     *        <code>null</code> means that the schema name should not be used to narrow the search
     * @param table a table name; must match the table name as it is stored in the database
     */
    public List<JdbcIndex> getUniqueKey(String catalog, String schemaName, String table) throws SQLException {
        if (StringUtils.isBlank(table)) {
            return null;
        }
        List<JdbcIndex> indices = getIndexes(catalog, schemaName, table);
        if (indices == null || indices.isEmpty()) {
            return Collections.emptyList();
        }
        return indices.stream().filter(JdbcIndex::isUnique).collect(Collectors.toList());
    }

    protected JdbcTable convertTable(Map<String, Object> rs) {
        JdbcTable jdbcSchema = new JdbcTable();
        jdbcSchema.setCatalog(safeToString(rs.get("TABLE_CAT")));
        jdbcSchema.setSchema(safeToString(rs.get("TABLE_SCHEM")));
        jdbcSchema.setTable(safeToString(rs.get("TABLE_NAME")));
        jdbcSchema.setComment(safeToString(rs.get("REMARKS")));
        return jdbcSchema;
    }

    protected JdbcColumn convertColumn(Map<String, Object> rs, JdbcPrimaryKey primaryKey, Set<String> uniqueKey) {
        JdbcColumn jdbcColumn = new JdbcColumn();
        jdbcColumn.setTableCatalog(safeToString(rs.get("TABLE_CAT")));
        jdbcColumn.setTableSchema(safeToString(rs.get("TABLE_SCHEM")));
        jdbcColumn.setTableName(safeToString(rs.get("TABLE_NAME")));
        jdbcColumn.setColumnName(safeToString(rs.get("COLUMN_NAME")));
        //
        String isNullable = safeToString(rs.get("IS_NULLABLE"));
        if ("YES".equals(isNullable)) {
            jdbcColumn.setNullable(true);
        } else if ("NO".equals(isNullable)) {
            jdbcColumn.setNullable(false);
        } else {
            jdbcColumn.setNullable(null);
        }
        jdbcColumn.setNullableType(JdbcNullableType.valueOfCode(safeToInteger(rs.get("NULLABLE"))));
        jdbcColumn.setColumnType(safeToString(rs.get("TYPE_NAME")));
        jdbcColumn.setJdbcType(safeToInteger(rs.get("DATA_TYPE")));
        if (StringUtils.isNotBlank(jdbcColumn.getColumnType())) {
            jdbcColumn.setColumnType(jdbcColumn.getColumnType().toLowerCase());
        }
        //
        jdbcColumn.setColumnSize(safeToInteger(rs.get("COLUMN_SIZE")));
        jdbcColumn.setComment(safeToString(rs.get("REMARKS")));
        //
        String isAutoincrement = safeToString(rs.get("IS_AUTOINCREMENT"));
        if ("YES".equals(isAutoincrement)) {
            jdbcColumn.setAutoincrement(true);
        } else if ("NO".equals(isAutoincrement)) {
            jdbcColumn.setAutoincrement(false);
        } else {
            jdbcColumn.setAutoincrement(null);
        }
        String isGeneratedColumn = safeToString(rs.get("IS_GENERATEDCOLUMN"));
        if ("YES".equals(isGeneratedColumn)) {
            jdbcColumn.setGeneratedColumn(true);
        } else if ("NO".equals(isGeneratedColumn)) {
            jdbcColumn.setGeneratedColumn(false);
        } else {
            jdbcColumn.setGeneratedColumn(null);
        }
        //
        jdbcColumn.setDecimalDigits(safeToInteger(rs.get("DECIMAL_DIGITS")));
        jdbcColumn.setHasDefaultValue(safeToString(rs.get("COLUMN_DEF")) != null);
        jdbcColumn.setCharOctetLength(safeToInteger(rs.get("CHAR_OCTET_LENGTH")));
        jdbcColumn.setIndex(safeToInteger(rs.get("ORDINAL_POSITION")));
        //
        if (primaryKey != null) {
            List<String> pkColumns = primaryKey.getColumns();
            if (pkColumns.contains(jdbcColumn.getColumnName())) {
                jdbcColumn.setPrimaryKey(true);
            }
        }
        jdbcColumn.setUniqueKey(uniqueKey.contains(jdbcColumn.getColumnName()));
        return jdbcColumn;
    }

    protected JdbcPrimaryKey convertPrimaryKey(Map<String, Object> recordMap) {
        JdbcPrimaryKey primaryKey = new JdbcPrimaryKey();
        primaryKey.setCatalog(safeToString(recordMap.get("TABLE_CAT")));
        primaryKey.setSchema(safeToString(recordMap.get("TABLE_SCHEM")));
        primaryKey.setTable(safeToString(recordMap.get("TABLE_NAME")));
        primaryKey.setName(safeToString(recordMap.get("PK_NAME")));
        //
        primaryKey.getColumns().add(safeToString(recordMap.get("COLUMN_NAME")));
        return primaryKey;
    }

    protected JdbcIndex convertIndex(Map<String, Object> recordMap) {
        JdbcIndex jdbcIndex = new JdbcIndex();
        jdbcIndex.setTableCatalog(safeToString(recordMap.get("TABLE_CAT")));
        jdbcIndex.setTableSchema(safeToString(recordMap.get("TABLE_SCHEM")));
        jdbcIndex.setTableName(safeToString(recordMap.get("TABLE_NAME")));
        jdbcIndex.setName(safeToString(recordMap.get("INDEX_NAME")));
        jdbcIndex.setUnique(!safeToBoolean(recordMap.get("NON_UNIQUE")));
        //
        jdbcIndex.setIndexType(JdbcIndexType.valueOfCode(safeToInteger(recordMap.get("TYPE"))));
        jdbcIndex.setIndexQualifier(safeToString(recordMap.get("INDEX_QUALIFIER")));
        jdbcIndex.setCardinality(safeToLong(recordMap.get("CARDINALITY")));
        jdbcIndex.setPages(safeToLong(recordMap.get("PAGES")));
        jdbcIndex.setFilterCondition(safeToString(recordMap.get("FILTER_CONDITION")));
        //
        String columnName = safeToString(recordMap.get("COLUMN_NAME"));
        String ascOrDesc = safeToString(recordMap.get("ASC_OR_DESC"));
        jdbcIndex.getColumns().add(columnName);
        jdbcIndex.getStorageType().put(columnName, ascOrDesc);
        return jdbcIndex;
    }

    protected static String safeToString(Object obj) {
        return (obj == null) ? null : obj.toString();
    }

    protected static Integer safeToInteger(Object obj) {
        return (obj == null) ? null : (obj instanceof Number ? ((Number) obj).intValue() : Integer.parseInt(obj.toString()));
    }

    protected static Long safeToLong(Object obj) {
        return (obj == null) ? null : (obj instanceof Number ? ((Number) obj).longValue() : Long.parseLong(obj.toString()));
    }

    protected static Boolean safeToBoolean(Object obj) {
        return (obj == null) ? null : (obj instanceof Boolean ? ((Boolean) obj) : Boolean.parseBoolean(obj.toString()));
    }

    protected static Connection newProxyConnection(Connection connection, AutoCloseable closeable) {
        ProxyCloseInvocationHandler handler = new ProxyCloseInvocationHandler(connection, closeable);
        return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class[] { Connection.class, Closeable.class }, handler);
    }

    private static class ProxyCloseInvocationHandler implements InvocationHandler {

        private final Connection    connection;
        private final AutoCloseable closeable;

        ProxyCloseInvocationHandler(Connection connection, AutoCloseable closeable) {
            this.connection = connection;
            this.closeable = closeable;
        }

        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            switch (method.getName()) {
                case "getTargetConnection": {
                    return connection;
                }
                case "toString": {
                    return this.connection.toString();
                }
                case "equals": {
                    return proxy == args[0];
                }
                case "hashCode": {
                    return System.identityHashCode(proxy);
                }
                case "close": {
                    if (closeable != null) {
                        this.closeable.close();
                    }
                    return null;
                }
            }

            try {
                return method.invoke(this.connection, args);
            } catch (InvocationTargetException ex) {
                throw ex.getTargetException();
            }
        }
    }
}
