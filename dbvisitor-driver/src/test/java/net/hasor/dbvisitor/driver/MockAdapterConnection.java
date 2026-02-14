package net.hasor.dbvisitor.driver;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MockAdapterConnection extends AdapterConnection implements TransactionSupport {
    private boolean autoCommit = true;
    private String  catalog    = "default";
    private String  schema     = "";

    public MockAdapterConnection(String jdbcUrl, String userName) {
        super(jdbcUrl, userName);
    }

    @Override
    public String getCatalog() throws SQLException {
        return this.catalog;
    }

    @Override
    public void setCatalog(String catalog) throws SQLException {
        this.catalog = catalog;
    }

    @Override
    public String getSchema() throws SQLException {
        return this.schema;
    }

    @Override
    public void setSchema(String schema) throws SQLException {
        this.schema = schema;
    }

    @Override
    public AdapterRequest newRequest(String sql) {
        return new MockAdapterRequest(sql);
    }

    @Override
    public void doRequest(AdapterRequest request, AdapterReceive receive) throws SQLException {
        MockAdapterRequest req = (MockAdapterRequest) request;
        String sql = req.getSql();

        if (sql.startsWith("SELECT") || sql.startsWith("select")) {
            if (sql.contains("FROM types") || sql.contains("from types")) {
                List<JdbcColumn> columns = new ArrayList<>();
                columns.add(new JdbcColumn("c_int", "int", "types", "default", ""));
                columns.add(new JdbcColumn("c_string", "varchar", "types", "default", ""));
                columns.add(new JdbcColumn("c_bool", "boolean", "types", "default", ""));
                columns.add(new JdbcColumn("c_float", "float", "types", "default", ""));
                columns.add(new JdbcColumn("c_double", "double", "types", "default", ""));
                columns.add(new JdbcColumn("c_date", "date", "types", "default", ""));
                columns.add(new JdbcColumn("c_timestamp", "timestamp", "types", "default", ""));
                columns.add(new JdbcColumn("c_blob", "blob", "types", "default", ""));
                columns.add(new JdbcColumn("c_clob", "clob", "types", "default", ""));
                columns.add(new JdbcColumn("c_null", "varchar", "types", "default", ""));

                Object[][] data = new Object[][] { { 100, "str", true, 1.1f, 2.2d, java.sql.Date.valueOf("2023-01-01"), java.sql.Timestamp.valueOf("2023-01-01 12:00:00"), new byte[] { 1, 2, 3 }, "clob content", null } };
                AdapterMemoryCursor cursor = new AdapterMemoryCursor(columns, data);
                receive.responseResult(req, cursor);
            } else if (sql.contains("FROM empty") || sql.contains("from empty")) {
                List<JdbcColumn> columns = new ArrayList<>();
                columns.add(new JdbcColumn("id", "int", "empty", "default", ""));
                AdapterMemoryCursor cursor = new AdapterMemoryCursor(columns, new Object[0][]);
                receive.responseResult(req, cursor);
            } else if (sql.contains("FROM multi") || sql.contains("from multi")) {
                List<JdbcColumn> columns = new ArrayList<>();
                columns.add(new JdbcColumn("id", "int", "multi", "default", ""));
                columns.add(new JdbcColumn("val", "varchar", "multi", "default", ""));
                Object[][] data = new Object[][] { { 1, "a" }, { 2, "b" }, { 3, "c" } };
                AdapterMemoryCursor cursor = new AdapterMemoryCursor(columns, data);
                receive.responseResult(req, cursor);
            } else if (sql.contains("FROM nullrow") || sql.contains("from nullrow")) {
                List<JdbcColumn> columns = new ArrayList<>();
                columns.add(new JdbcColumn("c1", "int", "nullrow", "default", ""));
                columns.add(new JdbcColumn("c2", "varchar", "nullrow", "default", ""));
                Object[][] data = new Object[][] { { null, null } };
                AdapterMemoryCursor cursor = new AdapterMemoryCursor(columns, data);
                receive.responseResult(req, cursor);
            } else {
                List<JdbcColumn> columns = new ArrayList<>();
                columns.add(new JdbcColumn("id", "int", "test", "default", ""));
                columns.add(new JdbcColumn("name", "varchar", "test", "default", ""));
                Object[][] data = new Object[][] { { 1, "test user 1" }, { 2, "test user 2" } };
                AdapterMemoryCursor cursor = new AdapterMemoryCursor(columns, data);
                receive.responseResult(req, cursor);
            }
        } else if (sql.startsWith("{call") || sql.startsWith("CALL")) {
            receive.responseParameter(req, "arg1", "int", 42);
            receive.responseParameter(req, "arg2", "varchar", "out_value");
            receive.responseUpdateCount(req, 1);
        } else {
            receive.responseUpdateCount(req, 1);
        }
    }

    @Override
    public void cancelRequest() {
    }

    @Override
    protected void doClose() throws IOException {
    }

    @Override
    public boolean supportIsolation(int value) {
        return true;
    }

    @Override
    public int getIsolation() {
        return java.sql.Connection.TRANSACTION_READ_COMMITTED;
    }

    @Override
    public void setIsolation(int value) throws SQLException {
    }

    @Override
    public boolean isAutoCommit() {
        return this.autoCommit;
    }

    @Override
    public void setAutoCommit(boolean value) throws SQLException {
        this.autoCommit = value;
    }

    @Override
    public void commit() throws SQLException {
    }

    @Override
    public void rollback() throws SQLException {
    }
}
