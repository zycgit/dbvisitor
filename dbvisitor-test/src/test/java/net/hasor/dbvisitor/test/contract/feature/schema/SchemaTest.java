package net.hasor.dbvisitor.test.contract.feature.schema;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import net.hasor.dbvisitor.test.nxn.capability.Capability;
import net.hasor.dbvisitor.test.nxn.capability.CapabilityId;
import net.hasor.dbvisitor.test.nxn.junit.AbstractNxnContractTest;

import static org.junit.Assert.assertTrue;

public abstract class SchemaTest extends AbstractNxnContractTest {
    private static final Map<String, List<String>> STANDARD_SCHEMA = new LinkedHashMap<>();

    static {
        STANDARD_SCHEMA.put("user_info", Arrays.asList("id", "name", "age", "email", "create_time"));
        STANDARD_SCHEMA.put("user_role", Arrays.asList("user_id", "role_id", "role_name", "create_time"));
        STANDARD_SCHEMA.put("user_order", Arrays.asList("id", "user_id", "order_no", "amount", "create_time"));
        STANDARD_SCHEMA.put("complex_order", Arrays.asList("id", "order_no", "address", "items"));
        STANDARD_SCHEMA.put("product_vector", Arrays.asList("id", "name", "embedding"));
        STANDARD_SCHEMA.put("basic_types_test", Arrays.asList(//
                "id", "byte_value", "short_value", "int_value", "long_value", "float_value", "double_value", "decimal_value", "big_int_value", "bool_value", "string_value", "char_value"));
        STANDARD_SCHEMA.put("basic_types_explicit_test", Arrays.asList(//
                "id", "byte_value", "short_value", "int_value", "long_value", "float_value", "double_value", "decimal_value", "big_int_value", "bool_bit", "bool_boolean", "char_value", "varchar_value", "nvarchar_value"));
        STANDARD_SCHEMA.put("array_types_test", Arrays.asList("id", "int_array", "string_array", "float_array"));
        STANDARD_SCHEMA.put("array_types_explicit_test", Arrays.asList("id", "int_array", "varchar_array"));
        STANDARD_SCHEMA.put("array_types_annotation_test", Arrays.asList(//
                "id", "array_no_annotation", "array_jdbc_type", "array_type_handler", "array_number_special", "array_full_annotated"));
        STANDARD_SCHEMA.put("test_special_types", Arrays.asList("id", "json_map", "json_list", "json_set", "int_array"));
        STANDARD_SCHEMA.put("binary_types_explicit_test", Arrays.asList("id", "binary_value", "varbinary_value", "longvarbinary_value", "blob_value"));
        STANDARD_SCHEMA.put("enum_types_explicit_test", Arrays.asList("id", "status_string", "status_enum_code", "status_ordinal", "status_code"));
        STANDARD_SCHEMA.put("json_types_explicit_test", Arrays.asList("id", "json_varchar", "json_mysql", "nested_json"));
        STANDARD_SCHEMA.put("time_types_explicit_test", Arrays.asList(//
                "id", "date_value", "time_value", "timestamp_value", "local_date_ts", "local_time_ts", "local_datetime_ts", "julian_day"));
    }

    @Test
    @Capability(CapabilityId.SCHEMA_STANDARD_TABLES)
    public void schemaAllStandardTablesForCurDatasource() throws SQLException {
        Set<String> tables = readTables();
        for (String table : STANDARD_SCHEMA.keySet()) {
            assertTrue("Expected standard table '" + table + "' in " + profile().env() + ", actual=" + tables, tables.contains(normalize(table)));
        }
    }

    @Test
    @Capability(CapabilityId.SCHEMA_STANDARD_COLUMNS)
    public void schemaRequiredColsForStandardTables() throws SQLException {
        for (Map.Entry<String, List<String>> entry : STANDARD_SCHEMA.entrySet()) {
            Set<String> columns = readColumns(entry.getKey());
            for (String column : entry.getValue()) {
                assertTrue("Expected column '" + entry.getKey() + "." + column + "' in " + profile().env() + ", actual=" + columns, columns.contains(normalize(column)));
            }
        }
    }

    private Set<String> readTables() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            Set<String> tables = new LinkedHashSet<>();
            readTables(metaData, conn.getCatalog(), tables);
            readTables(metaData, null, tables);
            return tables;
        }
    }

    private void readTables(DatabaseMetaData metaData, String catalog, Set<String> tables) throws SQLException {
        try (ResultSet rs = metaData.getTables(catalog, null, "%", new String[] { "TABLE" })) {
            while (rs.next()) {
                tables.add(normalize(rs.getString("TABLE_NAME")));
            }
        }
    }

    private Set<String> readColumns(String tableName) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            Set<String> columns = new LinkedHashSet<>();
            for (String pattern : tablePatterns(tableName)) {
                readColumns(metaData, conn.getCatalog(), pattern, columns);
                readColumns(metaData, null, pattern, columns);
            }
            return columns;
        }
    }

    private void readColumns(DatabaseMetaData metaData, String catalog, String tablePattern, Set<String> columns) throws SQLException {
        try (ResultSet rs = metaData.getColumns(catalog, null, tablePattern, "%")) {
            while (rs.next()) {
                columns.add(normalize(rs.getString("COLUMN_NAME")));
            }
        }
    }

    private List<String> tablePatterns(String tableName) {
        return Arrays.asList(tableName, tableName.toUpperCase(Locale.ROOT), tableName.toLowerCase(Locale.ROOT));
    }

    private String normalize(String name) {
        return name == null ? "" : name.toLowerCase(Locale.ROOT);
    }
}
