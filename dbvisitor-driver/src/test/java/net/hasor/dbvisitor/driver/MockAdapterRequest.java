package net.hasor.dbvisitor.driver;

public class MockAdapterRequest extends AdapterRequest {
    private final String sql;

    public MockAdapterRequest(String sql) {
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }
}
